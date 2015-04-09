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
	
	public Connector getSystemConnector(String connectorName, String side) throws Exception{
			
		Properties propConnector = PropertiesUtil.getPropertieFile("connectors");
		Properties propConnections = PropertiesUtil.getPropertieFile("connections");
		Connector connector = (Connector) Class.forName(propConnector.getProperty(connectorName)).newInstance();
	
		if(connector instanceof GithubConnector){
			GithubConnector githubConnector = (GithubConnector) connector;
			githubConnector.setUrl(propConnections.getProperty(side + "_SYSTEM_URL"));
			githubConnector.setUser(propConnections.getProperty(side + "_SYSTEM_USER"));
			githubConnector.setPassword(propConnections.getProperty(side + "_SYSTEM_PASSWORD"));
			githubConnector.setSystemName(propConnections.getProperty(side + "_SYSTEM_NAME"));
			githubConnector.setStartVersion(propConnections.getProperty(side + "_SYSTEM_START_VERSION"));
			githubConnector.setEndVersion(propConnections.getProperty(side + "_SYSTEM_END_VERSION"));
			githubConnector.setBranch(propConnections.getProperty(side + "_SYSTEM_BRANCH"));
			githubConnector.setPullRequests(propConnections.getProperty(side + "_SYSTEM_PULL_REQUESTS"));
			githubConnector.setRepositoryLocalPath(propConnections.getProperty(side + "_SYSTEM_LOCAL_PATH"));
			githubConnector.setSide(side);
	    	githubConnector.performSetup();
		}
		return connector;
	}
	
	public RepositoryConnector getRepositoryConnector() throws Exception{
		Properties propConfig = PropertiesUtil.getPropertieFile("config");
		String connectorName = propConfig.getProperty("REPOSITORY_CONNECTOR");
		Properties propConnector = PropertiesUtil.getPropertieFile("connectors");
		RepositoryConnector connector = (RepositoryConnector) Class.forName(propConnector.getProperty(connectorName)).newInstance();
		return connector;
	}
}
