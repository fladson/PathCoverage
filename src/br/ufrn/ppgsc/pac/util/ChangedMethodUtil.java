package br.ufrn.ppgsc.pac.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

public abstract class ChangedMethodUtil {

	public static void formatChangedMehodsFromFile(boolean format) {
		try {
			FileWriter out = new FileWriter("changedMethodsFormated.txt");
			for (String line : Files.readAllLines(Paths.get("changedMethods.txt"))) {
				String formated_method;
				if(format){
					 formated_method = searchMethodOnWalaFile(formatChangedMethod(line)).replace(">", "")+"\n";
					out.write(formated_method);
				}else{
					out.write(searchMethodOnWalaFile(line).replace(">", "")+"\n");
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String formatChangedMethod(String method) {
		String formatedMethod = "";
		int dotCount = 0;
		for (int i = 0; i < method.length(); i++) {
			if (method.charAt(i) == '.') {
				dotCount++;
				if (dotCount == 3) {
					formatedMethod = method.substring(i + 1,method.length());
					return formatedMethod;
				}
			}
		}
		return formatedMethod;
	}
	
	private static String searchMethodOnWalaFile(String method){
		String linha = "";
		try {
			for (String line : Files.readAllLines(Paths.get("callEntries.txt"))) {
				if(line.contains(method)){
					return line;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return linha;
	}
}
