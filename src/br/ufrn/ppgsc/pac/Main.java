package br.ufrn.ppgsc.pac;

import br.ufrn.ppgsc.pac.connectors.Connector;
import br.ufrn.ppgsc.pac.connectors.ConnectorFactory;
import br.ufrn.ppgsc.pac.connectors.RepositoryConnector;

public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("| In�cio da an�lise |");
		Connector githubConnector = new ConnectorFactory().getSystemConnector("GITHUB");
		RepositoryConnector repositoryConnector = new ConnectorFactory().getRepositoryConnector();
		System.out.println(githubConnector.toString());
		
		System.out.println("| Fim da an�lise |");
	}
}
