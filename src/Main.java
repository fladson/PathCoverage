import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

/*
 * O ideal é passar os dois jars das versões diferentes para comparação automática.
 */

public class Main {
	
	protected static HashSet<String> all_methods_wala_master;
	protected static HashSet<String> all_methods_wala_updated;
	
	public static void main(String[] args) {
		try {
			System.out.println("========== Begin ==========");
			String jar_master = "teste_master";
			String jar_updated = "teste_updated";
			String jar_master_path = "/Volumes/Beta/Mestrado/workspace_luna/jars_directory/" + jar_master + ".jar";
			String jar_updated_path = "/Volumes/Beta/Mestrado/workspace_luna/jars_directory/" + jar_updated + ".jar";
			
			all_methods_wala_master = new HashSet<String>();
			all_methods_wala_master = MethodListWALA.getMethodsFromJar(jar_master_path, all_methods_wala_master);
			
			all_methods_wala_updated = new HashSet<String>();
			all_methods_wala_updated = MethodListWALA.getMethodsFromJar(jar_updated_path, all_methods_wala_updated);

			String sca_master_file_path = "/Volumes/Beta/Mestrado/workspace_luna/jars_directory/" + jar_master + "_sca";
			File sca_master_output = new File (sca_master_file_path);
			
			String sca_updated_file_path = "/Volumes/Beta/Mestrado/workspace_luna/jars_directory/" + jar_updated + "_sca";
			File sca_updated_output = new File (sca_updated_file_path);
			
			ComparisonAlgorithm comp = new ComparisonAlgorithm(all_methods_wala_master,sca_master_output,all_methods_wala_updated,sca_updated_output);
			Collection<String> methodsNotCovered = comp.runComparison();
			
			// Escrevendo em um arquivo os métodos não cobertos (apenas para checagem)
			FileWriter out = null;
			try {
				out = new FileWriter("naoCobertos.txt");
				for (String string : methodsNotCovered) {
					out.write(string+"\r\n");
				}
			} finally {
				out.close();
			}
			System.out.println("========== End ==========");
		} catch (ClassHierarchyException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getSystemNameFromPath(String path){
		Pattern p = Pattern.compile("[\\w]+(?=\\.)");
		Matcher mt = p.matcher(path);
		String system_name = "";
		while(mt.find()) {
			 system_name = path.substring(mt.start(), mt.end());
	      }
		return system_name;
	}	
}
