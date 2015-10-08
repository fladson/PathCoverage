package br.ufrn.ppgsc.fc.main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import com.ibm.wala.shrikeCT.InvalidClassFileException;

import br.ufrn.ppgsc.fc.connectors.ConnectorFactory;
import br.ufrn.ppgsc.fc.connectors.EvolutionConnector;
import br.ufrn.ppgsc.fc.connectors.SCMConnector;
import br.ufrn.ppgsc.fc.database.DatabaseService;
import br.ufrn.ppgsc.fc.database.GenericDAO;
import br.ufrn.ppgsc.fc.database.PostgreSQLJDBC;
import br.ufrn.ppgsc.fc.dynamic.DynamicFlowExport;
import br.ufrn.ppgsc.fc.graph_analysis.GraphAnalysis;
import br.ufrn.ppgsc.fc.jdt.ChangedMethodUtil;
import br.ufrn.ppgsc.fc.model.Node;
import br.ufrn.ppgsc.fc.model.RuntimeScenario;
import br.ufrn.ppgsc.fc.util.MemberUtil;
import br.ufrn.ppgsc.fc.util.PropertiesUtil;
import br.ufrn.ppgsc.fc.wala.CallGraphWALA;

public class Main {
	public static void main(String[] args) throws Exception {
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.INFO); 

		System.out.println("|Path Coverage Analysis|");
		
		SCMConnector gitConnector = new ConnectorFactory().getSCMConnector("GIT");
		gitConnector.performSetup();
		
		Properties propConnections = PropertiesUtil.getPropertieFile("connections");
		if(!propConnections.getProperty("HAVING_CALL_FILE").equals("TRUE")){
			CallGraphWALA cg = new CallGraphWALA();
			cg.saveWalaCallGraphToFile(gitConnector.getRepositoryLocalPath());
		}else{
			System.out.println("-|CallEntries files loaded...");
		}
		
		if(!propConnections.getProperty("HAVING_COVERED_FILE").equals("TRUE")){
			System.out.println("\n-|Recovering covered paths from the database...");
		    java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.WARNING);
			GenericDAO<RuntimeScenario> dao = new DatabaseService<RuntimeScenario>().getGenericDAO();
			List<RuntimeScenario> scenarios = dao.readAll(RuntimeScenario.class);
			Appendable buffer = new StringBuffer();
			for (RuntimeScenario runtimeScenario : scenarios) {
				DynamicFlowExport.printScenarioTree(runtimeScenario, buffer);
			}
			FileWriter out = new FileWriter("coveredPaths.txt");;
			out.write(buffer.toString());
			out.close();
			System.out.println("\t-|Covered paths saved to file: coveredPaths.txt");
		}else{
			System.out.println("-|CoveredPaths file loaded...");
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
		
//		System.out.println("-| Result");
//		for (String line : Files.readAllLines(Paths.get("resultado.txt"))) {
//			System.out.println(line);
//		}
//		System.out.println("-| Stats");
//		System.out.println("\t -| Paths on static analysis: " + MemberUtil.getPathCountFromFile("callEntries.txt"));
//		System.out.println("\t -| Paths on dynamic analysis: " + MemberUtil.getPathCountFromFile("coveredPaths.txt"));
		System.out.println("|End of analysis|");
	}
}
