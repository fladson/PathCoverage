package br.ufrn.ppgsc.pac;

import java.util.List;

import br.ufrn.ppgsc.pac.db.PostgreSQLJDBC;
import br.ufrn.ppgsc.pac.model.Node;

public class Main {
	public static void main(String[] args) throws Exception {
//		System.out.println("|Inicio da analise|");
//		Connector githubConnector = new ConnectorFactory().getSystemConnector("GITHUB");
//		RepositoryConnector repositoryConnector = new ConnectorFactory().getRepositoryConnector();
//		System.out.println(githubConnector.getFilesOfRevision(githubConnector.getEndVersion()).size());
//		repositoryConnector.performSetup(githubConnector.getRepositoryLocalPath());
//		System.out.println("|Fim da analise|");
		
		PostgreSQLJDBC db = new PostgreSQLJDBC();
		List<Node> coveredMethods = db.getAllCoveredMethods();
		
		for (Node node : coveredMethods) {
			System.out.println(node.toString());
		}
		
	}
}
