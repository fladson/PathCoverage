package br.ufrn.ppgsc.fc.wala;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import br.ufrn.ppgsc.fc.util.MethodUtil;
import br.ufrn.ppgsc.fc.util.SpecificApplicationEntryPoints;

import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.ArgumentTypeEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphIntegrity;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;
import com.ibm.wala.util.strings.Atom;

public class CallGraphWALA {

	private CallGraph cg = null;
	private AnalysisScope analysisScope = null;
	private ClassHierarchy classHierarchy = null;
	private ClassLoaderReference loader = null;
	Iterable<Entrypoint> entrypoints = null;
	IClassLoader loaderAppClass = null;
	protected Graph<CGNode> graph_pruned = null;
	FileWriter out = null;
	protected StringBuffer buffer = new StringBuffer();

	public void saveWalaCallGraphToFile(String application) throws UnsupportedOperationException, InvalidClassFileException,
			ClassHierarchyException {
		if (cg == null) {
			try {
				System.out.println("-|Initializing WALA CallGraph...");
				loadAnalysisScope(application);
				loadClassHierarchy();
				List<String> st = new ArrayList<String>();
				// sites.put("Lsrc/C", "met_C2");
				// sites.put("Lsrc/B", "met_B1");
				// sites.put("Lcom/google/auth/oauth2/UserAuthorizer",
				// "getAndStoreCredentialsFromCode");
				// sites.put("Lio/netty/channel/udt/DefaultUdtChannelConfig","apply");
				// sites.put("TipoDocumentoMBean","entrarCadastro");
				// sites.put("Lbr/ufrn/sigrh/tests/arq/dao/AposentadoriaDaoTest","setUp");
				// br.ufrn.sigrh.tests.arq.dao.AposentadoriaDaoTest setUp
				st.add("RespostaFormularioLNCMBean.reset");
				st.add("RespostaFormularioLNCMBean.iniciarLNC");
				st.add("RespostaFormularioLNCMBean.entrarLNCLogin");
				st.add("RespostaFormularioLNCMBean.entrarLNCMenuCapacitacao");
				st.add("RespostaFormularioLNCMBean.entrarLNCMenuChefia");
				st.add("RespostaFormularioLNCMBean.formularioLNCByLogin");
				st.add("RespostaFormularioLNCMBean.formularioLNCByMenuChefia");
				st.add("RespostaFormularioLNCMBean.formularioLNCByMenuCapacitacao");
				st.add("RespostaFormularioLNCMBean.formularioLNCTecnico");
				st.add("RespostaFormularioLNCMBean.formularioLNCDocente");
				st.add("RespostaFormularioLNCMBean.formularioLNCGestor");
				st.add("RespostaFormularioLNCMBean.entrarTelaAvisoLNC");
				st.add("RespostaFormularioLNCMBean.carregarInformacoesInicioLNC");




				if(st.size()>0){
					this.entrypoints = new SpecificApplicationEntryPoints(analysisScope, classHierarchy, st);
				}else{
					this.entrypoints = new AllApplicationEntrypoints(analysisScope, classHierarchy);
				}
				
				if(this.entrypoints == null || !this.entrypoints.iterator().hasNext()){
					System.out.println("\t-| Entrypoints not found!");
					System.exit(0);
				}
				
				AnalysisOptions options = new AnalysisOptions(this.analysisScope, entrypoints);

				// options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);
				options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);

				// SSAPropagationCallGraphBuilder builder = null;
				// PropagationCallGraphBuilder builder = null;
				CallGraphBuilder builder = null;

				/*
				 * opcoes: makeRTABuilder = Rapid Type Analysis makeZeroCFABuilder = context-insensitive, class-based heap
				 * makeZeroOneCFABuilder = context-insensitive, allocation-site-based heap makeZeroOneContainerCFABuilder =
				 * 0-1-CFA with object-sensitive containers
				 * 
				 * makeZeroContainerCFABuilder = 0-CFA Call Graph Builder augmented with extra logic for containers
				 * makeNCFABuilder =uses call-string context sensitivity, with call-string length limited to n, and a
				 * context-sensitive allocation-site-based heap abstraction.
				 * 
				 * makeVanillaZeroOneCFABuilder makeVanillaZeroOneContainerCFABuilder makeVanillaNCFABuilder
				 */
				builder = Util.makeRTABuilder(options, new AnalysisCache(), classHierarchy, analysisScope);

				System.out.println("  -|CallGraph is being created.");
				this.cg = builder.makeCallGraph(options, null);
				System.out.println("  -|CallGraph created.");

				out = new FileWriter("callEntries.txt");

				System.out.println("    -|Filtering the application nodes");
				Collection<CGNode> entries = cg.getEntrypointNodes();

				System.out.println("\tENTRY POINTS: " + entries.size());

				printNodes(cg);
				System.out.println("    -|Application entries saved to file: callEntries.txt");
				out.close();
			} catch (IOException | WalaException | IllegalArgumentException | CallGraphBuilderCancelException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadAnalysisScope(String application_path) throws IOException {
		System.out.println("  -|Loading analysis scope...");
		if(application_path.contains("SIGRH")){
			analysisScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("/Users/fladson/dev/sinfo/repos/arquitetura-2.7.9.jar:/Users/fladson/git/SIGRH/", 
					new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS));

		}else{
			analysisScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(application_path, new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		}
		
//		AnalysisScopeReader.addClassPathToScope(application_path, analysisScope, loader);
		
		
		//
		// this.analysisScope = AnalysisScope.createJavaAnalysisScope();
		// this.analysisScope = AnalysisScopeReader.makePrimordialScope(new
		// File(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		// AnalysisScopeReader.addClassPathToScope("/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home",
		// this.analysisScope,
		// this.analysisScope.getLoader(AnalysisScope.PRIMORDIAL));

		// this.loader =
		// this.analysisScope.getLoader(AnalysisScope.APPLICATION);
		// AnalysisScopeReader.addClassPathToScope(application_path,
		// this.analysisScope, this.loader);

		// this.analysisScope =
		// CallGraphTestUtil.makeJ2SEAnalysisScope(application_path,
		// CallGraphTestUtil.REGRESSION_EXCLUSIONS);

		// analysisScope.addToScope(ClassLoaderReference.Primordial,new
		// JarFile(PropertiesUtil.getPropertieValueOf("config",
		// "PRIMORDIAL_LIB")));
	}

	private void loadClassHierarchy() throws ClassHierarchyException {
		System.out.println("  -|Loading class hierarchy...");
		this.classHierarchy = ClassHierarchy.make(this.analysisScope);
//		int count = 0;
//		for(IClass klass:classHierarchy){
//			if(klass.getName().toString().contains("UsuarioMBean")){
//				count++;
//				System.out.println(count + ": " + klass.getName());
//			}
//		}
	}

	public void printNodes(CallGraph cg) throws IOException {
		for (Iterator<CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();)
			printNodes(cg, it.next(), new HashSet<CGNode>(), 0);
	}

	private void printNodes(CallGraph cg, CGNode root, Set<CGNode> visited, int level) throws IOException {
		if (root.getMethod().getDeclaringClass().getClassLoader().toString().equals("Primordial"))
			return;
		// System.out.println(getStandartMethodSignature(root.getMethod()));
		if (visited.contains(root)) {
			printLevelTabs(level);
			out.write("[*]" + MethodUtil.getStandartMethodSignature(root.getMethod()) + "\n");
			// System.out.println(str + "[*]" +
			// root.getMethod().getSignature());
			return;
		} else {
			printLevelTabs(level);
			out.write(MethodUtil.getStandartMethodSignature(root.getMethod()) + "\n");
			// System.out.println(str + root.getMethod().getSignature());
		}

		visited.add(root);

		for (Iterator<CallSiteReference> it = root.iterateCallSites(); it.hasNext();)
			for (CGNode cgNode : cg.getPossibleTargets(root, it.next()))
				printNodes(cg, cgNode, visited, level + 1);

		visited.remove(root);
	}

	private void printLevelTabs(int level) throws IOException {
		for (int i = 0; i < level; i++) {
			out.write(">");
		}
	}

}
