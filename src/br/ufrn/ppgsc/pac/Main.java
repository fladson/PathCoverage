package br.ufrn.ppgsc.pac;

import java.util.List;

import com.ibm.wala.shrikeCT.InvalidClassFileException;

import br.ufrn.ppgsc.pac.connectors.Connector;
import br.ufrn.ppgsc.pac.connectors.ConnectorFactory;
import br.ufrn.ppgsc.pac.connectors.RepositoryConnector;
import br.ufrn.ppgsc.pac.db.PostgreSQLJDBC;
import br.ufrn.ppgsc.pac.model.Node;
import br.ufrn.ppgsc.pac.util.PropertiesUtil;
import br.ufrn.ppgsc.pac.wala.CallGraphWALA;

public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("|Inicio da analise|");
		System.out.println("-|Recuperando projeto SOURCE clonado localmente...");
		// Repo master onde sera extraido o call graph completo e os metodos cobertos
		Connector githubTargetConnector = new ConnectorFactory().getSystemConnector("GITHUB", "TARGET");
		// Repo onde sera analisado apenas as alteracoes, apenas os metodos alterados serao extraidos
		System.out.println("-|Baixando projeto TARGET do Github...");
		Connector githubSourceConnector = new ConnectorFactory().getSystemConnector("GITHUB", "SOURCE");

		System.out.println("-|Criando grafo de chamadas com o WALA...");
		System.out.println("Path: " + githubTargetConnector.getRepositoryLocalPath());
		CallGraphWALA cg = new CallGraphWALA();
		cg.init(githubTargetConnector.getRepositoryLocalPath());
		
		
		
//		System.out.println("-|Recuperando metodos cobertos do banco de dados...\n!Tenha certeza de ter rodado o AspectJ antes disso!");
//		PostgreSQLJDBC db = new PostgreSQLJDBC();
//		List<Node> coveredMethods = db.getAllCoveredMethods();
//		
		githubSourceConnector.parseMethodsChangedOnCommitsRange();
	
		System.out.println("|Fim da analise|");
	}
}
