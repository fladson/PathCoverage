package br.ufrn.ppgsc.pac.wala;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import br.ufrn.ppgsc.pac.util.PropertiesUtil;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.strings.Atom;

public class CallGraphWALA {

	private CallGraph cg = null;
	private AnalysisScope analysisScope = null;
	private ClassHierarchy classHierarchy = null;
	private final static String PDF_FILE = "cg.pdf";
	protected Graph<CGNode> graph_pruned = null;
	FileWriter out = null;

	public void saveWalaCallGraphToFile(String application) throws UnsupportedOperationException,
			InvalidClassFileException, ClassHierarchyException {
		if (cg == null) {
			try {
				System.out.println("-|Initializing WALA CallGraph...");
				loadAnalysisScope(application);
				loadClassHierarchy();

				Iterable<Entrypoint> entrypoints = null;
				System.out.println("  -|Getting EntryPoints...");
				entrypoints = Util.makeMainEntrypoints(this.analysisScope,this.classHierarchy);
				if (!entrypoints.iterator().hasNext()) {
					// a aplicacao nao possui main, tentando pegar entrypoint
					// TODO Ver se essa e a melhor opcao
					entrypoints = makeLibraryEntrypoints(analysisScope,classHierarchy);
				}

				AnalysisOptions options = new AnalysisOptions(
						this.analysisScope, entrypoints);
				// AnalysisCache cache = new AnalysisCache();
				options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);

				SSAPropagationCallGraphBuilder builder = null;

				IClassHierarchy cha = this.classHierarchy;
				AnalysisScope scope = this.analysisScope;

				builder = Util.makeZeroOneCFABuilder(options,
						new AnalysisCache(), cha, scope);
				if(!entrypoints.iterator().hasNext()){
					System.out.println("  -|Nao foi possivel gerar um entrypoint para o sistema");
					return;
				}
				this.cg = builder.makeCallGraph(options, null);
				System.out.println("  -|CallGraph created.");
				
				out = new FileWriter("callEntries.txt");

				System.out.println("    -|Filtering the application nodes" );
				Collection<CGNode> entries = cg.getEntrypointNodes();	    
				for ( Iterator<CGNode> i = entries.iterator() ; i.hasNext() ;)
				{			
					CGNode entrypoint = i.next();			
					printCallGraphNode(cg, entrypoint, 0);
				}
				
				System.out.println("    -|Application entries saved to file: callEntries.txt" );	
				out.close();
			} catch (IOException | WalaException | IllegalArgumentException | CallGraphBuilderCancelException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Metodo que captura entrie points de metodos publicos e protegidos
	 * So e executado se o projeto nao possuir um Main como ponto de entrada
	 */
	private Iterable<Entrypoint> makeLibraryEntrypoints(AnalysisScope scope,
			IClassHierarchy cha) {
		if (cha == null) {
			throw new IllegalArgumentException("cha is null");
		}
		final HashSet<Entrypoint> entryPoints = HashSetFactory.make();
		for (IClass klass : cha) {
			if (!scope.isApplicationLoader(klass.getClassLoader()))
				continue;
			for (IMethod m : klass.getDeclaredMethods()) {
				if (m.isPublic() || m.isProtected()) {
					entryPoints.add(new DefaultEntrypoint(m, cha));
				}
			}
		}
		return entryPoints;
	}

	private static void addClassPathToScope(String classPath,
			AnalysisScope scope, ClassLoaderReference loader) {
		if (classPath == null) {
			throw new IllegalArgumentException("null classPath");
		}
		try {
			StringTokenizer paths = new StringTokenizer(classPath,
					File.pathSeparator);
			while (paths.hasMoreTokens()) {
				String path = paths.nextToken();
				if (path.endsWith(".jar")) {
					JarFile jar = new JarFile(path);
					scope.addToScope(loader, jar);
					try {
						if (jar.getManifest() != null) {
							String cp = jar.getManifest().getMainAttributes()
									.getValue("Class-Path");
							if (cp != null) {
								for (String cpEntry : cp.split(" ")) {
									addClassPathToScope(
											new File(path).getParent()
													+ File.separator + cpEntry,
											scope, loader);
								}
							}
						}
					} catch (RuntimeException e) {
						System.err
								.println("warning: trouble processing class path of "
										+ path);
					}
				} else {
					File f = new File(path);
					if (f.isDirectory()) {
						scope.addToScope(loader, new BinaryDirectoryTreeModule(
								f));
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

	private void loadAnalysisScope(String application_path) throws IOException {
		System.out.println("  -|Loading analysis scope...");
		this.analysisScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(application_path, new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		analysisScope.addToScope(ClassLoaderReference.Primordial,new JarFile(PropertiesUtil.getPropertieValueOf("config", "PRIMORDIAL_LIB")));

		ClassLoaderReference loader = analysisScope.getLoader(AnalysisScope.APPLICATION);
		addClassPathToScope(application_path, analysisScope, loader);
	}

	private void loadClassHierarchy() throws ClassHierarchyException {
		System.out.println("  -|Loading class hierarchy...");
		this.classHierarchy = ClassHierarchy.make(this.analysisScope);
	}

	/**
	 * Classe que representa os metogos da aplicao para filtragem, e geracao do pdf com os nodes
	 * So e usado para gerar a representacao do grafo para um documento pdf
	 * Retirado de PDFCallGraph.java
	 * 
	 * @author fladson
	 *
	 */
	private static class ApplicationLoaderFilter extends Predicate<CGNode> {
		@Override
		public boolean test(CGNode o) {
			if (o instanceof CGNode) {
				CGNode n = (CGNode) o;
				return n.getMethod().getDeclaringClass().getClassLoader()
						.getReference()
						.equals(ClassLoaderReference.Application);
			} else if (o instanceof LocalPointerKey) {
				LocalPointerKey l = (LocalPointerKey) o;
				return test(l.getNode());
			} else {
				return false;
			}
		}
	}

	private static Graph<CGNode> pruneForAppLoader(CallGraph g)
			throws WalaException {
		return PDFTypeHierarchy.pruneGraph(g, new ApplicationLoaderFilter());
	}
	
	/*
	 * Print de Felipe
	 */
	private void printCallGraphNode(CallGraph cg, CGNode currNode, int level) throws IOException {
		if (getAtomLoaderReference(currNode) != AnalysisScope.PRIMORDIAL) {
			printLevelTabs(level);
//			System.out.println(currNode.getMethod().getSignature());
			out.write(getStandartMethodSignature(currNode.getMethod())+"\n");
			for (Iterator<CGNode> sucessores = cg.getSuccNodes(currNode); sucessores.hasNext();) {
				printCallGraphNode(cg, sucessores.next(), level + 1);
			}
		}
	}
	
	private void printLevelTabs(int level) throws IOException {
		for (int i = 0; i < level; i++){
//			System.out.print("\t");
			out.write(">");
		}
	}

	private Atom getAtomLoaderReference(CGNode node) {
		return node.getMethod().getReference().getDeclaringClass()
				.getClassLoader().getName();
	}

	/*
	 * Format the WALA method signature to the Standard signature pattern
	 */
	private static String getStandartMethodSignature(IMethod method) {
		StringBuffer result = new StringBuffer();
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
	
	private void printMethods() {
		for (IClass cl : this.classHierarchy) {
			if (cl.getClassLoader().getReference()
					.equals(ClassLoaderReference.Application)) {
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

	public Graph<CGNode> getGrap_pruned() {
		return graph_pruned;
	}

	public void setGrap_pruned(Graph<CGNode> grap_pruned) {
		this.graph_pruned = grap_pruned;
	}
}
