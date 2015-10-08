package br.ufrn.ppgsc.fc.wala;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

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
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
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
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphIntegrity;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;
import com.ibm.wala.util.graph.GraphPrint;
import com.ibm.wala.util.graph.traverse.DFS;
import com.ibm.wala.util.graph.traverse.DFSAllPathsFinder;
import com.ibm.wala.util.strings.Atom;

public class CallGraphWALA {

	private CallGraph cg = null;
	private AnalysisScope analysisScope = null;
	private ClassHierarchy classHierarchy = null;
	protected Graph<CGNode> graph_pruned = null;
	FileWriter out = null;
	protected String node_string = "node";
	protected String previous_node_string = "previous";
	protected StringBuffer buffer = new StringBuffer();

	public LinkedHashMap<String, ArrayList<String>> nodes;
	private Set<String> cgset;

	public void saveWalaCallGraphToFile(String application)
			throws UnsupportedOperationException, InvalidClassFileException,
			ClassHierarchyException {
		if (cg == null) {
			try {
				System.out.println("-|Initializing WALA CallGraph...");
				loadAnalysisScope(application);
				loadClassHierarchy();

				Iterable<Entrypoint> entrypoints = null;
				System.out.println("  -|Getting EntryPoints...");
				entrypoints = Util.makeMainEntrypoints(this.analysisScope,
						this.classHierarchy);
				if (!entrypoints.iterator().hasNext()) {
					// Demostenes Metodo
					// entrypoints =
					// makeLibraryEntrypoints(analysisScope,classHierarchy);
					// Metodo WAlA para gerar entrypoints da aplicação
					entrypoints = new AllApplicationEntrypoints(analysisScope,
							classHierarchy);
				}

				AnalysisOptions options = new AnalysisOptions(
						this.analysisScope, entrypoints);

				// options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);
				options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);

//				 SSAPropagationCallGraphBuilder builder = null;
//				PropagationCallGraphBuilder builder = null;
				CallGraphBuilder builder = null;

				IClassHierarchy cha = this.classHierarchy;
				AnalysisScope scope = this.analysisScope;

				/*
				 * opcoes: makeRTABuilder = Rapid Type Analysis
				 * makeZeroCFABuilder = context-insensitive, class-based heap
				 * makeZeroOneCFABuilder = context-insensitive, allocation-site-based heap 
				 * makeZeroOneContainerCFABuilder = 0-1-CFA with object-sensitive containers
				 * 
				 * makeZeroContainerCFABuilder = 0-CFA Call Graph Builder augmented with extra logic for containers 
				 * makeNCFABuilder =uses call-string context sensitivity, with call-string length limited to n, and a context-sensitive allocation-site-based
				 * heap abstraction.
				 * 
				 * makeVanillaZeroOneCFABuilder
				 * makeVanillaZeroOneContainerCFABuilder 
				 * makeVanillaNCFABuilder
				 */
				builder = Util.makeRTABuilder(options,
						new AnalysisCache(), cha, scope);

				// Adicionei para tentar resolver a recursao
				// System.out.println("Context Selector: " +
				// builder.getContextSelector());
				// DefaultContextSelector contextSelector = new
				// DefaultContextSelector(options, cha);
				// builder.setContextSelector(contextSelector);

				if (!entrypoints.iterator().hasNext()) {
					System.out
							.println("  -|Nao foi possivel gerar um entrypoint para o sistema");
					return;
				}

				System.out.println("  -|CallGraph is being created.");
				this.cg = builder.makeCallGraph(options, null);
				System.out.println("  -|CallGraph created.");

				out = new FileWriter("callEntries.txt");

				System.out.println("    -|Filtering the application nodes");
				Collection<CGNode> entries = cg.getEntrypointNodes();

				System.out.println("\tENTRY POINTS: " + entries.size());

				try {
					GraphIntegrity.check(cg);
				} catch (UnsoundGraphException e) {
					e.printStackTrace();
				}
				// DFSAllPathsFinder<String> paths = makeFinder((CallGraph)cg,
				// "A", "L");
				//
				printNodes(cg);
//				out.write("=========================================" + "\n");
				
//				printCallGraph(cg,cg.getFakeRootNode(),0);
				


				System.out
						.println("    -|Application entries saved to file: callEntries.txt");
				out.close();
			} catch (IOException | WalaException | IllegalArgumentException
					| CallGraphBuilderCancelException e) {
				e.printStackTrace();
			}
		}
	}

	private void printCallGraph(CallGraph cg, CGNode currentNode, int level) throws IOException {
		String indent = "";
		for (int i = 0; i < level; i++) {
			indent += ">";
		}
		if (level == 0)
			this.cgset = new HashSet<String>();

		String methodSig = getStandartMethodSignature(currentNode.getMethod());
		out.write(indent + methodSig+"\n");
//		System.out.println(indent + methodSig);

		this.cgset.add(methodSig);

		IClassHierarchy cha = cg.getClassHierarchy();
		Iterator<CallSiteReference> callsiteIter = currentNode
				.iterateCallSites();

		while (callsiteIter.hasNext()) {
			CallSiteReference callsite = callsiteIter.next();
			IMethod calledMethod = cha.resolveMethod(callsite
					.getDeclaredTarget());

			if (cg.getPossibleTargets(currentNode, callsite).isEmpty()) {
				methodSig = callsite.getDeclaredTarget().getSignature();
				out.write(indent + ">" + methodSig+"\n");
//				System.out.println(indent + ">" + methodSig);
			} else {
				for (CGNode targetNode : cg.getPossibleTargets(currentNode,callsite)) {
					methodSig = getStandartMethodSignature(targetNode.getMethod());
					if (targetNode.getMethod().getDeclaringClass()
							.getClassLoader().getReference()
							.equals(ClassLoaderReference.Application)
							&& !this.cgset.contains(methodSig)) {
						printCallGraph(cg, targetNode, level + 1);
					} else {
						out.write(indent + ">" + methodSig+"\n");
//						System.out.println(indent + ">" + methodSig);
					}
				}
			}
		}
	}

	private static boolean valid(String sig) {
		return !((sig.startsWith("java.") || sig.startsWith("com.ibm")
				|| sig.startsWith("javax.")
				|| sig.startsWith("java.lang.StringBuilder.") || sig
					.startsWith("org.junit")));

	}

	private void fromCG(CallGraph cg) {
		Iterator<CGNode> it = cg.iterator();
		while (it.hasNext()) {
			CGNode node = it.next();
			Context x = node.getContext();
			String sig = getStandartMethodSignature(node.getMethod());
			if (!valid(sig))
				continue;
			ArrayList<String> arr = new ArrayList<String>();
			Iterator<CallSiteReference> it2 = node.iterateCallSites();
			while (it2.hasNext()) {
				CallSiteReference ref = it2.next();
				String adjSig = ref.getDeclaredTarget().getSignature();
				if (!valid(adjSig))
					continue;
				arr.add(adjSig);
			}
			this.nodes.put(sig, arr);
		}
	}

	private void cgStringToFile() throws IOException {
		Set<String> set = this.nodes.keySet();
		for (String no : set) {
			out.write(no + "\n");
			ArrayList<String> edges = this.nodes.get(no);
			if (!edges.isEmpty()) {
				// str += "Edges to:";
				for (int i = 1; i < edges.size(); i++) {
					printLevelTabs(i);
					out.write(edges.get(i) + "\n");
				}
			}
		}
	}

	public String CGtoString() {
		String str = "";
		Set<String> set = this.nodes.keySet();
		for (String no : set) {
			str += "[Node: " + no + "]\n    ";
			ArrayList<String> edges = this.nodes.get(no);
			if (edges.isEmpty())
				str += "Has no output edges";
			else {
				str += "Edges to:";
				for (String e : edges) {
					str += " [" + e + "]";
				}
			}
			str += "\n";
		}
		return str;
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
		this.analysisScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(
				application_path, new File(
						CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		// analysisScope.addToScope(ClassLoaderReference.Primordial,new
		// JarFile(PropertiesUtil.getPropertieValueOf("config",
		// "PRIMORDIAL_LIB")));
		
		analysisScope.addToScope(CallGraphTestUtil
						.makeJ2SEAnalysisScope(
								"/Users/fladson/git/PathCoverage/src/properties/primordial.txt","/Users/fladson/git/PathCoverage/src/properties/applicationExclusions.txt"));

		ClassLoaderReference loader = analysisScope
				.getLoader(AnalysisScope.APPLICATION);
		addClassPathToScope(application_path, analysisScope, loader);
	}
	

	private void loadClassHierarchy() throws ClassHierarchyException {
		System.out.println("  -|Loading class hierarchy...");
		this.classHierarchy = ClassHierarchy.make(this.analysisScope);
	}

	public void printNodes(CallGraph cg) throws IOException {
		for (Iterator<CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it
				.hasNext();)
			printNodes(cg, it.next(), new HashSet<CGNode>(), 0);
	}

	private void printNodes(CallGraph cg, CGNode root, Set<CGNode> visited,
			int level) throws IOException {
		if (root.getMethod().getDeclaringClass().getClassLoader().toString()
				.equals("Primordial"))
			return;

		if (visited.contains(root)) {
			printLevelTabs(level);
			out.write("[*]" + getStandartMethodSignature(root.getMethod())
					+ "\n");
			// System.out.println(str + "[*]" +
			// root.getMethod().getSignature());
			return;
		} else {
			printLevelTabs(level);
			out.write(getStandartMethodSignature(root.getMethod()) + "\n");
			// System.out.println(str + root.getMethod().getSignature());
		}

		visited.add(root);

		for (Iterator<CallSiteReference> it = root.iterateCallSites(); it
				.hasNext();)
			for (CGNode cgNode : cg.getPossibleTargets(root, it.next()))
				printNodes(cg, cgNode, visited, level + 1);

		visited.remove(root);
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

	private void printLevelTabs(int level) throws IOException {
		for (int i = 0; i < level; i++) {
			out.write(">");
		}
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
