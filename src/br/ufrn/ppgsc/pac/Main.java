package br.ufrn.ppgsc.pac;

import java.io.FileWriter;
import java.util.List;

import com.ibm.wala.shrikeCT.InvalidClassFileException;

import br.ufrn.ppgsc.pac.connectors.Connector;
import br.ufrn.ppgsc.pac.connectors.ConnectorFactory;
import br.ufrn.ppgsc.pac.connectors.RepositoryConnector;
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
		
		System.out.println("|Inicio da analise|");
		System.out.println("-|Recuperando projeto TARGET clonado localmente...");
		// Repo master onde sera extraido o call graph completo e os metodos cobertos
		Connector githubTargetConnector = new ConnectorFactory().getSystemConnector("GITHUB", "TARGET");

		System.out.println("-|Criando grafo de chamadas com o WALA...");
		System.out.println("Path: " + githubTargetConnector.getRepositoryLocalPath());
		CallGraphWALA cg = new CallGraphWALA();
//		cg.init(githubTargetConnector.getRepositoryLocalPath());
	
		System.out.println("-|Recuperando caminhos cobertos do banco de dados...\n!Tenha certeza de ter rodado o AspectJ antes disso!");
		GenericDAO<RuntimeScenario> dao = new DatabaseService<RuntimeScenario>().getGenericDAO();
		List<RuntimeScenario> scenarios = dao.readAll(RuntimeScenario.class);
		Appendable buffer = new StringBuffer();
		for (RuntimeScenario runtimeScenario : scenarios) {
			RuntimeCallGraphPrintUtil.printScenarioTree(runtimeScenario, buffer);
		}

		System.out.println("Caminhos cobertos capturados");
		FileWriter out = new FileWriter("coveredPaths.txt");;
		System.out.println(buffer);
		out.write(buffer.toString());
		out.close();
		
//		// Repo onde sera analisado apenas as alteracoes, apenas os metodos alterados serao extraidos
		System.out.println("-|Baixando projeto SOURCE do Github...");
		Connector githubSourceConnector = new ConnectorFactory().getSystemConnector("GITHUB", "SOURCE");
		FileWriter out2 = new FileWriter("changedMethods.txt");
		List<String> changedMethods = githubSourceConnector.parseMethodsChangedOnCommitsRange();
		for (String method : changedMethods) {
			out2.write(method.replace("/", ".")+"\n");
		}
		out2.close();
		System.out.println("|Fim da analise|");
	}
}
