import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.wala.ipa.cha.ClassHierarchyException;


public class Main {
	
	HashSet allMethodsDistinct = new HashSet();
	
	public static void main(String[] args) {
		try {
			System.out.println("========== Begin ==========");
			String jarPath = "/Volumes/Beta/Mestrado/workspace_luna/scribe.jar";
			String wala_file_path = MethodListWALA.getMethodsFromJar(jarPath);
			File WALA_output = new File (wala_file_path);
			String sca_file_path = "/Volumes/Beta/Mestrado/workspace_luna/" + getSystemNameFromPath(jarPath) + "_sca";
			File SCA_output = new File (sca_file_path);
			ComparisonAlgorithm comp = new ComparisonAlgorithm(WALA_output,SCA_output);
			comp.getChangesFromOriginal();
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
