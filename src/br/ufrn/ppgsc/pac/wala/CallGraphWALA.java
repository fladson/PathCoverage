package br.ufrn.ppgsc.pac.wala;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import br.ufrn.ppgsc.pac.util.PropertiesUtil;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ide.ui.SWTTreeViewer;
import com.ibm.wala.ide.ui.ViewIRAction;
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
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
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
import com.ibm.wala.util.graph.GraphPrint;
import com.ibm.wala.util.graph.GraphUtil;
import com.ibm.wala.util.graph.InferGraphRoots;
import com.ibm.wala.util.graph.traverse.DFS;
import com.ibm.wala.util.scope.JUnitEntryPoints;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.PDFViewUtil;
import com.ibm.wala.viz.viewer.WalaViewer;

public class CallGraphWALA {

	private CallGraph cg = null;
	protected Graph<CGNode> graph_pruned = null;

	private AnalysisScope analysisScope = null;
	private ClassHierarchy classHierarchy = null;
	private final static String PDF_FILE = "cg.pdf";

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

	public static void addClassPathToScope(String classPath,
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

	public void loadAnalysisScope(String application_path) throws IOException {
		// this.analysisScope = AnalysisScope.createJavaAnalysisScope();
		this.analysisScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(
				application_path, new File(
						CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		analysisScope
				.addToScope(
						ClassLoaderReference.Primordial,
						new JarFile(PropertiesUtil.getPropertieValueOf(
								"config", "PRIMORDIAL_LIB")));
		// analysisScope.addToScope(ClassLoaderReference.Primordial, new
		// JarFile("C:/Users/Fladson Gomes/Downloads/libs/jsdg-stubs-jre1.5.jar"));
		ClassLoaderReference loader = analysisScope
				.getLoader(AnalysisScope.APPLICATION);
		addClassPathToScope(application_path, analysisScope, loader);
	}

	public void loadClassHierarchy() throws ClassHierarchyException {
		this.classHierarchy = ClassHierarchy.make(this.analysisScope);
	}

	public void init(String application) throws UnsupportedOperationException,
			InvalidClassFileException, ClassHierarchyException {
		if (cg == null) {
			try {
				loadAnalysisScope(application);
				loadClassHierarchy();
				// printMethods();

				// insere o main como entryPoint.
				Iterable<Entrypoint> entrypoints = null;
				entrypoints = Util.makeMainEntrypoints(this.analysisScope,
						this.classHierarchy);
				if (!entrypoints.iterator().hasNext()) {
					// a aplicacao nao possui main, tentando pegar entrypoint
					// dos testes
					// O WALA nao suporta o Junit 4, ficando inviavel pegar o
					// entrypoint dos testes visto que as anotacoes sao
					// necessarias
					// para rodar o framework de Felipe
					// entrypoints = JUnitEntryPoints.make(classHierarchy);

					// TODO Ver se essa é a melhor opção
					entrypoints = makeLibraryEntrypoints(analysisScope,
							classHierarchy);
				}
//				System.out.println((new Date()).toString()
//						+ " - EntryPoints created.");
				// System.out.println("============= Entry points ================");
				//
				// Iterator<Entrypoint> it_entrypoints = entrypoints.iterator();
				// while (it_entrypoints.hasNext()) {
				// System.out.println(it_entrypoints.next());
				// }
				// System.out.println("===========================================");

				AnalysisOptions options = new AnalysisOptions(
						this.analysisScope, entrypoints);
				// AnalysisCache cache = new AnalysisCache();

				/**
				 * mais informa��es:
				 * http://wala.sourceforge.net/wiki/index.php/UserGuide
				 * :PointerAnalysis#Improving_Scalability
				 */
				options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);

				SSAPropagationCallGraphBuilder builder = null;

				// if(this.loader.isZeroCFA()){ // configura��o da an�lise
				// 0-CFA.
				IClassHierarchy cha = this.classHierarchy;
				AnalysisScope scope = this.analysisScope;

				builder = Util.makeZeroOneCFABuilder(options,
						new AnalysisCache(), cha, scope);
				this.cg = builder.makeCallGraph(options, null);
				System.out.println((new Date()).toString()
						+ " - CallGraph created.");
				// System.out.println(CallGraphStats.getStats(cg));

				// imprime todas as ramifica��es a partir dos entries points at�
				// um cgnode primordial.
				// Collection<CGNode> entries = cg.getEntrypointNodes();
				// for (Iterator<CGNode> i = entries.iterator(); i.hasNext();) {
				// CGNode entrypoint = i.next();
				// printCallGraphNode(cg, entrypoint, 0);
				// }

				// retirando métodos do Java core e deixando apenas os métodos
				// da aplicação
				graph_pruned = pruneForAppLoader(cg);
				
				
//				printNodes(cg);
//				treeSearch(head, graph);
				System.out.println(GraphPrint.genericToString(graph_pruned));
				// formatando as assinaturas dos métodos
//				this.parsePrunedGraphToStandardMethodSignature(graph_pruned);
				// System.out.println("Graph pruned edges: " +
				// GraphUtil.countEdges(g));
				String pdfFile = "/Volumes/Beta/Mestrado/workspace_luna"
						+ File.separatorChar + PDF_FILE;
				String dotExe = "/usr/local/bin/dot";
				DotUtil.dotify(graph_pruned, null, PDFTypeHierarchy.DOT_FILE,
						pdfFile, dotExe);
				System.out.println("Call Graph pruned at: " + pdfFile);
				// String gvExe =
				// "/Applications/Preview.app/Contents/MacOS/Preview";
				// PDFViewUtil.launchPDFView(pdfFile, gvExe);
				
//				PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
//			    new WalaViewer(cg, pa);
				

			} catch (IOException | WalaException | IllegalArgumentException | CallGraphBuilderCancelException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void treeSearch(CGNode head, Graph<CGNode> graph) {
	    Stack<CGNode> preStack = new Stack<CGNode>();
	    Stack<CGNode> postStack = new Stack<CGNode>();
	    preStack.add(head);
	    while (!preStack.isEmpty()) {
	    	CGNode currentNode = preStack.pop();
	        // do whatever i want to on current node.
	    	System.out.println( getStandartMethodSignature(currentNode.getMethod()));
	        postStack.add(currentNode);
	        Set<CGNode> reachableNodes = DFS.getReachableNodes(graph, Collections.singleton(currentNode));
	        for (Iterator<CGNode> it = reachableNodes.iterator(); it.hasNext();) {
	        	 preStack.add(it.next());
			}
	    }

	    while (!postStack.isEmpty()) {
	    	CGNode vertex = postStack.pop();
	    }

	}

	/**
	 * Classe que representa os métogos da aplicação para filtragem Retirado de
	 * PDFCallGraph.java
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

	public static Graph<CGNode> pruneForAppLoader(CallGraph g)
			throws WalaException {
		return PDFTypeHierarchy.pruneGraph(g, new ApplicationLoaderFilter());
	}
	// Print retirado da ferramenta de Demóstenes
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

	private void parseGraphtoListOfCalls(Graph<CGNode> gr) {
		for (Iterator<CGNode> it = gr.iterator(); it.hasNext();) {
			
		}
	}
	
	
//	List<String> calls = null;
//	String call = "";
//	
	// Print retirado da ferramenta de Felipe
	public void printNodes(CallGraph cg) {
//		calls = new ArrayList<String>();
		for (Iterator<CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();)
			printNodes(cg, it.next(), new HashSet<CGNode>(), "");
		
//		System.out.println(calls.size());
	}
	
	// Print retirado da ferramenta de Felipe
	private void printNodes(CallGraph cg, CGNode root, Set<CGNode> visited, String str) {
		if (root.getMethod().getDeclaringClass().getClassLoader().toString().equals("Primordial"))
			return;

		if (visited.contains(root)) {
			System.out.println(str + "[*]" + getStandartMethodSignature(root.getMethod()));
//			call = str + getStandartMethodSignature(root.getMethod());
			return;
		} else{
			System.out.println(str + getStandartMethodSignature(root.getMethod()));
//			call = str + getStandartMethodSignature(root.getMethod());
//			calls.add(call);
//			call = "";
		}

		visited.add(root);

		for (Iterator<CallSiteReference> it = root.iterateCallSites(); it.hasNext();)
			for (CGNode cgNode : cg.getPossibleTargets(root, it.next()))
				printNodes(cg, cgNode, visited, str + "\t");

		visited.remove(root);
	}
	
	private void getListOfCalls(){
		
	}

	/*
	 * Format the WALA method signature to the Standard signature pattern
	 */
	private static String getStandartMethodSignature(IMethod method) {
		StringBuffer result = new StringBuffer();

		// O pacote do método será null se ele estiver no pacote padrão
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
								System.out
										.println(getStandartMethodSignature(m));
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
