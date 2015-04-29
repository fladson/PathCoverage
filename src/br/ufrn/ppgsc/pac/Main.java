package br.ufrn.ppgsc.pac;

import java.io.FileWriter;
import java.util.List;

import com.ibm.wala.shrikeCT.InvalidClassFileException;

import br.ufrn.ppgsc.pac.connectors.EvolutionConnector;
import br.ufrn.ppgsc.pac.connectors.ConnectorFactory;
import br.ufrn.ppgsc.pac.connectors.SCMConnector;
import br.ufrn.ppgsc.pac.db.DatabaseService;
import br.ufrn.ppgsc.pac.db.GenericDAO;
import br.ufrn.ppgsc.pac.db.PostgreSQLJDBC;
import br.ufrn.ppgsc.pac.model.Node;
import br.ufrn.ppgsc.pac.model.RuntimeScenario;
import br.ufrn.ppgsc.pac.util.PropertiesUtil;
import br.ufrn.ppgsc.pac.util.RuntimeCallGraphPrintUtil;
import br.ufrn.ppgsc.pac.wala.CallGraphWALA;

public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("|Path Coverage Analysis|");
		
//		SCMConnector gitConnector = new ConnectorFactory().getSCMConnector("GIT");
//		gitConnector.performSetup();
//		
//		CallGraphWALA cg = new CallGraphWALA();
//		cg.saveWalaCallGraphToFile(gitConnector.getRepositoryLocalPath());
//
//		System.out.println("\n-|Recovering covered paths from the database...");
//		GenericDAO<RuntimeScenario> dao = new DatabaseService<RuntimeScenario>().getGenericDAO();
//		List<RuntimeScenario> scenarios = dao.readAll(RuntimeScenario.class);
//		Appendable buffer = new StringBuffer();
//		for (RuntimeScenario runtimeScenario : scenarios) {
//			RuntimeCallGraphPrintUtil.printScenarioTree(runtimeScenario, buffer);
//		}
//		FileWriter out = new FileWriter("coveredPaths.txt");;
//		out.write(buffer.toString());
//		out.close();
//		System.out.println("\t-|Covered paths saved to file: coveredPaths.txt");
//		
		
		EvolutionConnector githubConnector = new ConnectorFactory().getEvolutionConnector("GITHUB");
		githubConnector.performSetup();
		
		List<String> changedMethods = githubConnector.getChangedMethodsFromEvolution();

		FileWriter out2 = new FileWriter("changedMethods.txt");
		for (String method : changedMethods) {
			out2.write(method.replace("/", ".")+"\n");
		}
		out2.close();
		System.out.println("|End of analysis|");
	}
}
