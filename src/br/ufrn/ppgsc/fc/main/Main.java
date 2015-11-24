package br.ufrn.ppgsc.fc.main;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import br.ufrn.ppgsc.fc.connectors.ConnectorFactory;
import br.ufrn.ppgsc.fc.connectors.EvolutionConnector;
import br.ufrn.ppgsc.fc.connectors.SCMConnector;
import br.ufrn.ppgsc.fc.database.DatabaseService;
import br.ufrn.ppgsc.fc.database.GenericDAO;
import br.ufrn.ppgsc.fc.dynamic.DynamicFlowExport;
import br.ufrn.ppgsc.fc.graph_analysis.GraphAnalysis;
import br.ufrn.ppgsc.fc.jdt.ChangedMethodUtil;
import br.ufrn.ppgsc.fc.model.RuntimeNode;
import br.ufrn.ppgsc.fc.model.RuntimeScenario;
import br.ufrn.ppgsc.fc.util.PropertiesUtil;
import br.ufrn.ppgsc.fc.util.RuntimeCallGraphPrintUtil;
import br.ufrn.ppgsc.fc.wala.CallGraphWALA;

public class Main {
	public static void main(String[] args) throws Exception {
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.INFO); 

		System.out.println("|Path Coverage Analysis|");
		
		SCMConnector gitConnector = new ConnectorFactory().getSCMConnector("GIT");
		gitConnector.performSetup();
		
		Properties propConnections = PropertiesUtil.getPropertieFile("connections");
		if(propConnections.getProperty("HAVING_CALL_FILE").equals("TRUE")){
			System.out.println("-|CallEntries files loaded...");
		}else{
			CallGraphWALA cg = new CallGraphWALA();
			cg.saveWalaCallGraphToFile(gitConnector.getRepositoryLocalPath());
		}
		
		if(propConnections.getProperty("HAVING_COVERED_FILE").equals("TRUE")){
			System.out.println("-|CoveredPaths file loaded...");
		}else{
			System.out.println("\n-|Recovering covered paths from the database...");
		    java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.WARNING);
			GenericDAO<RuntimeScenario> dao = new DatabaseService<RuntimeScenario>().getGenericDAO();
			GenericDAO<RuntimeNode> nodeDAO = new DatabaseService<RuntimeNode>().getGenericDAO();
			
			List<RuntimeScenario> scenarios = dao.readAll(RuntimeScenario.class);
//			List<RuntimeNode> nodes = nodeDAO.readAll(RuntimeNode.class);
			
//			System.out.println("nodes all");
//			nodes.clear();
			PrintStream ps = new PrintStream("coveredPathsTree.txt");
			
//			Appendable buffer = new StringBuffer();
			int count = 0;
			for (RuntimeScenario runtimeScenario : scenarios) {
				System.out.println(count++ + runtimeScenario.getName());
//				DynamicFlowExport.printScenarioTree(runtimeScenario, buffer);
				RuntimeCallGraphPrintUtil.logScenarioTree(runtimeScenario, ps);
			}
			
			ps.close();
			
//			FileWriter out = new FileWriter("coveredPaths.txt");;
//			out.write(buffer.toString());
//			out.close();
			System.out.println("\t-|Covered paths saved to file: coveredPaths.txt");
		}
		
		if(propConnections.getProperty("HAVING_CHANGED_METHODS_FILE").equals("TRUE")){
			// Solução para passar metodos modificados via aquivo de texto, para quando tiver erro e poucos métodos, passar apenas CLasse.methodo
			System.out.println("-|Change methods file loaded...");
			ChangedMethodUtil.formatChangedMehodsFromFile(false);
			Runtime.getRuntime().exec("python tree_parser.py", null, new File(System.getProperty("user.dir")));
			
		}else{
			EvolutionConnector githubConnector = new ConnectorFactory().getEvolutionConnector("GITHUB");
			githubConnector.performSetup();
			
			List<String> changedMethods = githubConnector.getChangedMethodsFromEvolution();

			FileWriter out2 = new FileWriter("changedMethods.txt");
			for (String method : changedMethods) {
				out2.write(method.replace("/", ".")+"\n");
			}
			out2.close();
			
			File changedMethodsFile = new File("changedMethods.txt");
			if(changedMethodsFile.length() == 0){
				System.out.println("-|Change methods file empty");
			}else{
				ChangedMethodUtil.formatChangedMehodsFromFile(true);
				Runtime.getRuntime().exec("python tree_parser.py", null, new File(System.getProperty("user.dir")));
			}
		}
		
		GraphAnalysis.analyseGraphsfromTextFile("", "", "");
		
		System.out.println("|End of analysis|");
	}
}
