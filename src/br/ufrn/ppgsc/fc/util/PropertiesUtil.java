/**
 * UNIVERSIDADE FEDERAL DO RIO GRANDE DO NORTE - UFRN
 * DEPARTAMENTO DE INFORMATICA E MATEMATICA APLICADA - DIMAP
 * Programa de Pos-Graduacao em Sistemas e Computacao - PPGSC
 */

/**
 * PropertiesUtil.java
 * @author fladson - fladsonthiago@gmail.com
 * @since 25/03/2015
 *
 */
package br.ufrn.ppgsc.fc.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
/**
 * UNIVERSIDADE FEDERAL DO RIO GRANDE DO NORTE - UFRN
 * DEPARTAMENTO DE INFORMATICA E MATEMATICA APLICADA - DIMAP
 * Programa de Pos-Graduacao em Sistemas e Computacao - PPGSC
 */

/**
 * PropertiesUtil.java
 * @author fladson - fladsonthiago@gmail.com
 * @since 25/03/2015
 *
 */

/**
 * @author fladson
 *
 */
public abstract class PropertiesUtil {
	
	public static Properties getPropertieFile(String filename) throws IOException {
		Properties props = new Properties();
		FileInputStream file = new FileInputStream(System.getProperty("user.dir") + "/src/properties/" + filename + ".properties");
		props.load(file);
		file.close();
		return props;
	}
	
	public static String getPropertieValueOf(String filename, String propertie) throws IOException {
		Properties props = new Properties();
		FileInputStream file = new FileInputStream(System.getProperty("user.dir") + "/src/properties/" + filename + ".properties");
		props.load(file);
		file.close();
		return props.getProperty(propertie);
	}

}
