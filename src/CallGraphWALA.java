import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.scope.JUnitEntryPoints;
import com.ibm.wala.util.strings.Atom;

public class CallGraphWALA {

	private CallGraph cg = null;

	private AnalysisScope analysisScope = null;
	private ClassHierarchy classHierarchy = null;	


	// retorna os entrypoints que correspondem a uma library
	public Iterable<Entrypoint> makeLibraryEntrypoints(AnalysisScope scope,
			IClassHierarchy cha) {
		if (cha == null) {
			throw new IllegalArgumentException("cha is null");
		}

		final HashSet<Entrypoint> entryPoints = HashSetFactory.make();

		for (IClass klass : cha) {
			if (!scope.isApplicationLoader(klass.getClassLoader()))
				continue;

			for (IMethod m : klass.getDeclaredMethods()) {
				if (m.isPublic() || m.isProtected()) { // this method is public
														// or protected
					entryPoints.add(new DefaultEntrypoint(m, cha));
				}
			}
		}
		return entryPoints;
	}
	
	  public static void addClassPathToScope(String classPath, AnalysisScope scope, ClassLoaderReference loader) {
		    if (classPath == null) {
		      throw new IllegalArgumentException("null classPath");
		    }
		    try {
		      StringTokenizer paths = new StringTokenizer(classPath, File.pathSeparator);
		      while (paths.hasMoreTokens()) {
		        String path = paths.nextToken();
		        if (path.endsWith(".jar")) {
		          JarFile jar = new JarFile(path);
		          scope.addToScope(loader, jar);
		          try {
		            if (jar.getManifest() != null) {
		              String cp = jar.getManifest().getMainAttributes().getValue("Class-Path");
		              if (cp != null) {
		                for(String cpEntry : cp.split(" ")) { 
		                  addClassPathToScope(new File(path).getParent() + File.separator + cpEntry, scope, loader);
		                }
		              }
		            }
		          } catch (RuntimeException e) {
		            System.err.println("warning: trouble processing class path of " + path);
		          }
		        } else {
		          File f = new File(path);
		          if (f.isDirectory()) {
		            scope.addToScope(loader, new BinaryDirectoryTreeModule(f));
		          } else {
		            scope.addClassFileToScope(loader, f);
		          }
		        }
		      }
		    } catch (IOException e) {
		      Assertions.UNREACHABLE(e.toString());
		    } catch (InvalidClassFileException e) {
		      Assertions.UNREACHABLE(e.toString());
		    }
		  }

	public void loadAnalysisScope (String application_path) throws IOException
	{					
		this.analysisScope = AnalysisScope.createJavaAnalysisScope();
//		analysisScope.addToScope(ClassLoaderReference.Primordial, new JarFile(
//				"/Volumes/Beta/Mestrado/workspace_luna/jsdg-stubs-jre1.5.jar"));
		analysisScope.addToScope(ClassLoaderReference.Primordial, new JarFile("C:/Users/Fladson Gomes/Downloads/libs/jsdg-stubs-jre1.5.jar"));
		ClassLoaderReference loader = analysisScope.getLoader(AnalysisScope.APPLICATION);	
		addClassPathToScope(application_path, analysisScope, loader);
	}

	public void loadClassHierarchy() throws ClassHierarchyException 
	{
		this.classHierarchy = ClassHierarchy.make(this.analysisScope);
	}
	
	public void init(String application)
			throws UnsupportedOperationException, InvalidClassFileException, ClassHierarchyException {
		if (cg == null) {
			try {
				loadAnalysisScope(application);
				loadClassHierarchy();
//				printMethods();
				
				// insere o main como entryPoint.
				Iterable<Entrypoint> entrypoints = null;
				entrypoints = Util.makeMainEntrypoints(this.analysisScope,this.classHierarchy);
				if(!entrypoints.iterator().hasNext()){
					// a aplicação não possui main, tentando pegar entrypoint dos testes
//					O WALA não suporta o Junit 4, ficando inviável pegar o entrypoint dos testes visto que as anotações são necessárias
//					para rodar o framework de Felipe
//					entrypoints = JUnitEntryPoints.make(classHierarchy);
					
					entrypoints = makeLibraryEntrypoints(analysisScope, classHierarchy);
				}
				System.out.println((new Date()).toString() + " - EntryPoints created.");
				System.out.println("============= Entry points ================");
				
				Iterator<Entrypoint> it_entrypoints = entrypoints.iterator();
				while (it_entrypoints.hasNext()) {
					System.out.println(it_entrypoints.next());
				}
				System.out.println("===========================================");

				AnalysisOptions options = new AnalysisOptions(this.analysisScope, entrypoints);
				AnalysisCache cache = new AnalysisCache();

				/**
				 * mais informações:
				 * http://wala.sourceforge.net/wiki/index.php/UserGuide
				 * :PointerAnalysis#Improving_Scalability
				 */
				options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);

				SSAPropagationCallGraphBuilder builder = null;

				// if(this.loader.isZeroCFA()){ // configuração da análise
				// 0-CFA.
				IClassHierarchy cha = this.classHierarchy;
				AnalysisScope scope = this.analysisScope;

				builder = Util.makeZeroOneCFABuilder(options, cache, cha,scope, null, null);
				this.cg = builder.makeCallGraph(options, null);
				System.out.println((new Date()).toString() + " - CallGraph created.");
				System.out.println(CallGraphStats.getStats(cg));

				// imprime todas as ramificações a partir dos entries points até um cgnode primordial.
				Collection<CGNode> entries = cg.getEntrypointNodes();
				for (Iterator<CGNode> i = entries.iterator(); i.hasNext();) {
					CGNode entrypoint = i.next();
					printCallGraphNode(cg, entrypoint, 0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (CallGraphBuilderCancelException e) {
				e.printStackTrace();
			}
		}
	}

	private void printCallGraphNode(CallGraph cg, CGNode currNode, int level) {

		if (getAtomLoaderReference(currNode) != AnalysisScope.PRIMORDIAL) {
			printLevelTabs(level);
			System.out.println(currNode.getMethod().getSignature());
			for (Iterator<CGNode> preds = cg.getPredNodes(currNode); preds
					.hasNext();) {
				printCallGraphNode(cg, preds.next(), level + 1);
			}
		}
	}

	private void printLevelTabs(int level) {
		for (int i = 0; i < level; i++)
			System.out.print("\t");
	}

	private Atom getAtomLoaderReference(CGNode node) {
		return node.getMethod().getReference().getDeclaringClass()
				.getClassLoader().getName();
	}
	
	private void printMethods(){
		for (IClass cl : this.classHierarchy) {
			if (cl.getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
				for (IMethod m : cl.getAllMethods()) {
					if (!m.getReference().toString().contains("< Primordial,")) {
						if (!m.getSignature().contains("Test")) {
							if (!m.getSignature().contains(".test.")) {
								 System.out.println(getStandartMethodSignature(m));
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * Format the WALA method signature to the Standard signature pattern
	 */
	private static String getStandartMethodSignature(IMethod method) {
		StringBuffer result = new StringBuffer();

		// O pacote do mÃ©todo serÃ¡ null se ele estiver no pacote padrÃ£o
		Atom methodPackage = method.getDeclaringClass().getName().getPackage();
		if (methodPackage != null) {
			result.append(method.getDeclaringClass().getName().getPackage()
					.toString().replaceAll("/", "."));
			result.append(".");
		}

		result.append(method.getDeclaringClass().getName().getClassName());
		result.append(".");

		if (method.isInit())
			result.append(method.getDeclaringClass().getName().getClassName());
		else
			result.append(method.getName());

		result.append("(");

		for (int i = 0; i < method.getSelector().getDescriptor()
				.getNumberOfParameters(); i++) {
			TypeName type = method.getSelector().getDescriptor()
					.getParameters()[i];

			if (type.getPackage() != null) {
				result.append(type.getPackage().toString().replaceAll("/", "."));
				result.append(".");
			}

			result.append(convertTypeSignatureToName(type.getClassName()
					.toString()));

			if (type.isArrayType()) {
				int j = 0;

				while (type.toString().charAt(j++) == '[')
					result.append("[]");
			}

			result.append(",");
		}

		if (result.charAt(result.length() - 1) == ',')
			result.deleteCharAt(result.length() - 1);

		return result + ")";
	}

	private static String convertTypeSignatureToName(String type) {
		switch (type) {
		case "Z":
			return "boolean";
		case "B":
			return "byte";
		case "C":
			return "char";
		case "D":
			return "double";
		case "F":
			return "float";
		case "I":
			return "int";
		case "J":
			return "long";
		case "S":
			return "short";
		case "V":
			return "void";
		default:
			return type;
		}
	}
}
