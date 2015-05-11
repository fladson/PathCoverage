package br.ufrn.ppgsc.pac.connectors;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import br.ufrn.ppgsc.pac.connectors.git.GitConnector;
import br.ufrn.ppgsc.pac.connectors.git.GithubConnector;
import br.ufrn.ppgsc.pac.util.PropertiesUtil;

/**
 * UNIVERSIDADE FEDERAL DO RIO GRANDE DO NORTE - UFRN
 * DEPARTAMENTO DE INFORMATICA E MATEMATICA APLICADA - DIMAP
 * Programa de Pos-Graduacao em Sistemas e Computacao - PPGSC
 */

/**
 * A factory design patters to create connectors classes. 
 * This connectors classes have information about how to connect to external informations.
 *
 * @author fladson - fladsonthiago@gmail.com
 * @since 20/03/2015
 *
 */
public class ConnectorFactory {
	
	Properties propConnections;
	Properties propConnector;
	Properties propConfig;
	public ConnectorFactory() throws IOException{
		propConnections = PropertiesUtil.getPropertieFile("connections");
		propConnector = PropertiesUtil.getPropertieFile("connectors");
		propConfig = PropertiesUtil.getPropertieFile("config");
	}
	
	
	public EvolutionConnector getEvolutionConnector(String connectorName) throws Exception{
			
		EvolutionConnector connector = (EvolutionConnector) Class.forName(propConnector.getProperty(connectorName)).newInstance();
		
		if(connector instanceof GithubConnector){
			GithubConnector githubConnector = (GithubConnector) connector;
			githubConnector.setUrl(propConnections.getProperty("GITHUB_URL"));
			githubConnector.setUser(propConnections.getProperty("GITHUB_USER"));
			githubConnector.setToken(propConnections.getProperty("GITHUB_TOKEN"));
			githubConnector.setStartVersion(propConnections.getProperty("EVOLUTION_START_VERSION"));
			githubConnector.setEndVersion(propConnections.getProperty("EVOLUTION_END_VERSION"));
			githubConnector.setBranch(propConnections.getProperty("EVOLUTION_BRANCH"));
			githubConnector.setPullRequest(propConnections.getProperty("EVOLUTION_PULL_REQUEST"));
			githubConnector.setEvolutionLocalPath(propConnections.getProperty("EVOLUTION_REPO_LOCAL_PATH"));
		}
		return connector;
	}
	
	public SCMConnector getSCMConnector(String repositoryConnectorName) throws Exception{
//		String connectorName = propConfig.getProperty("REPOSITORY_CONNECTOR");
		Properties propConnector = PropertiesUtil.getPropertieFile("connectors");
		SCMConnector repositoryConnector = (SCMConnector) Class.forName(propConnector.getProperty(repositoryConnectorName)).newInstance();
		
		if(repositoryConnector instanceof GitConnector){
			GitConnector gitConnector = (GitConnector) repositoryConnector;
			gitConnector.setRepositoryLocalPath(propConnections.getProperty("REPORITORY_LOCAL_PATH"));
			gitConnector.setRepositoryName(propConnections.getProperty("REPOSITORY_NAME"));
		}
		
		return repositoryConnector;
	}
}
