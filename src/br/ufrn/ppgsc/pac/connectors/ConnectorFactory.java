package br.ufrn.ppgsc.pac.connectors;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import br.ufrn.ppgsc.pac.connectors.git.GitConnector;
import br.ufrn.ppgsc.pac.connectors.git.GithubConnector;

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
	
	public Connector getSystemConnector(String connectorName) throws Exception{
			
		Properties propConnector = getProperties("connectors");
		Properties propConnections = getProperties("connections");
		Connector connector = (Connector) Class.forName(propConnector.getProperty(connectorName)).newInstance();
	
		if(connector instanceof GithubConnector){
			GithubConnector githubConnector = (GithubConnector) connector;
			githubConnector.setUrl(propConnections.getProperty("SYSTEM_URL"));
			githubConnector.setUser(propConnections.getProperty("SYSTEM_USER"));
			githubConnector.setPassword(propConnections.getProperty("SYSTEM_PASSWORD"));
			githubConnector.setSystemName(propConnections.getProperty("SYSTEM_NAME"));
			githubConnector.setStartVersion(propConnections.getProperty("SYSTEM_START_VERSION"));
			githubConnector.setEndVersion(propConnections.getProperty("SYSTEM_END_VERSION"));
	    	githubConnector.performSetup();
		}
		return connector;
	}
	
	public RepositoryConnector getRepositoryConnector() throws Exception{
		
		Properties propConfig = getProperties("config");
		String connectorName = propConfig.getProperty("REPOSITORY_CONNECTOR");
		Properties propConnector = getProperties("connectors");
		RepositoryConnector connector = (RepositoryConnector) Class.forName(propConnector.getProperty(connectorName)).newInstance();
		if(connector instanceof GitConnector){
			GitConnector gitConnector = (GitConnector) connector;
			gitConnector.performSetup();
		}
		return connector;
	}
	
	public static Properties getProperties(String filename) throws IOException {
		Properties props = new Properties();
		FileInputStream file = new FileInputStream(System.getProperty("user.dir") + "/src/properties/" + filename + ".properties");
		props.load(file);
		return props;
	}
}
