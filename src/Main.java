import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.wala.ipa.cha.ClassHierarchyException;


public class Main {
	
	protected static HashSet<String> allMethodsDistinct;
	
	public static void main(String[] args) {
		try {
			System.out.println("========== Begin ==========");
			String jarPath = "/Volumes/Beta/Mestrado/workspace_luna/scribe.jar";
			allMethodsDistinct = new HashSet<String>();
//			MethodListWALA methodLister = new MethodListWALA();
			allMethodsDistinct = MethodListWALA.getMethodsFromJar(jarPath, allMethodsDistinct);
			
//			File WALA_output = new File (wala_file_path);
			String sca_file_path = "/Volumes/Beta/Mestrado/workspace_luna/" + getSystemNameFromPath(jarPath) + "_sca";
			File SCA_output = new File (sca_file_path);
			ComparisonAlgorithm comp = new ComparisonAlgorithm(allMethodsDistinct,SCA_output);
			Collection<String> methodsNotCovered = comp.getDifferences();
			
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
