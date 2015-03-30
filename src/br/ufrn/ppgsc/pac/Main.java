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
		System.out.println("-|Baixando projeto do Github...");
		Connector githubConnector = new ConnectorFactory().getSystemConnector("GITHUB");
		RepositoryConnector repositoryConnector = new ConnectorFactory().getRepositoryConnector();
		repositoryConnector.performSetup(githubConnector.getRepositoryLocalPath());
		
		System.out.println("-|Buscando metodos alterados do commit: nos arquivos...");
		List<String> arquivos_alterados = githubConnector.getFilesOfRevision(PropertiesUtil.getPropertieValueOf("connections", "SYSTEM_END_VERSION"));
		
		for (String string : arquivos_alterados) {
			System.out.println(string);
		}
		
		System.out.println("-|Recuperando dados dos metodos cobertos do banco de dados...");
		PostgreSQLJDBC db = new PostgreSQLJDBC();
		List<Node> coveredMethods = db.getAllCoveredMethods();
		
//		for (Node node : coveredMethods) {
//			System.out.println(node.toString());
//		}
		
		System.out.println("-|Criando grafo de chamadas com o WALA...");
		CallGraphWALA cg = new CallGraphWALA();
//		cg.init("C:/Users/Fladson Gomes/Desktop/workspace_luna/teste"); 
		cg.init(githubConnector.getRepositoryLocalPath());
		
		

	
		System.out.println("|Fim da analise|");
	}
}
