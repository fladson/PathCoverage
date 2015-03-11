import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.strings.Atom;

public class MethodListWALA {

	/*
	 * Gets a jar and outputs its methods to a txt file
	 */
	public static HashSet<String> getMethodsFromJar(String pathToJar,
			HashSet<String> allMethodsDistinct) throws IOException,
			ClassHierarchyException {
		System.out.println("Analysing paths with WALA from jar at: "
				+ pathToJar);
		String system_name = Main.getSystemNameFromPath(pathToJar);
		String output_file = "/Volumes/Beta/Mestrado/workspace_luna/"
				+ system_name + "_wala.txt";

		AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

		scope.addToScope(ClassLoaderReference.Primordial, new JarFile(
				"/Volumes/Beta/Mestrado/workspace_luna/jsdg-stubs-jre1.5.jar"));
		scope.addToScope(ClassLoaderReference.Application, new JarFile(
				pathToJar));
		IClassHierarchy cha = ClassHierarchy.make(scope);
		// FileWriter out = null;
		// try {
		// out = new FileWriter(output_file);
		for (IClass cl : cha) {
			if (cl.getClassLoader().getReference()
					.equals(ClassLoaderReference.Application)) {
				for (IMethod m : cl.getAllMethods()) {
					// Filtro para métodos nativos do Java e suas libs
					if (!m.getReference().toString().contains("< Primordial,")) {
						// Filtro para métodos em pacotes de teste
						if (!m.getSignature().contains("Test")) {
							if (!m.getSignature().contains(".test.")) {
								// System.out.println(m.getSignature());
								allMethodsDistinct
										.add(getStandartMethodSignature(m));
								// out.write(getStandartMethodSignature(m) +
								// "\r\n");
							}
						}
					}
				}
			}
		}
		// } finally {
		// out.close();
		System.out.println("Ended WALA analysis of " + system_name + ".jar");
		// }
		return allMethodsDistinct;
	}

	/*
	 * Format the WALA method signature to the Standard signature pattern
	 */
	private static String getStandartMethodSignature(IMethod method) {
		StringBuffer result = new StringBuffer();

		// O pacote do método será null se ele estiver no pacote padrão
		Atom methodPackage = method.getDeclaringClass().getName().getPackage();
		if (methodPackage != null) {
			result.append(method.getDeclaringClass().getName().getPackage()
					.toString().replaceAll("/", "."));
			result.append(".");
		}

		result.append(method.getDeclaringClass().getName().getClassName());
		result.append(".");

		if (method.isInit())
			result.append(method.getDeclaringClass().getName().getClassName());
		else
			result.append(method.getName());

		result.append("(");

		for (int i = 0; i < method.getSelector().getDescriptor()
				.getNumberOfParameters(); i++) {
			TypeName type = method.getSelector().getDescriptor()
					.getParameters()[i];

			if (type.getPackage() != null) {
				result.append(type.getPackage().toString().replaceAll("/", "."));
				result.append(".");
			}

			result.append(convertTypeSignatureToName(type.getClassName()
					.toString()));

			if (type.isArrayType()) {
				int j = 0;

				while (type.toString().charAt(j++) == '[')
					result.append("[]");
			}

			result.append(",");
		}

		if (result.charAt(result.length() - 1) == ',')
			result.deleteCharAt(result.length() - 1);

		return result + ")";
	}

	private static String convertTypeSignatureToName(String type) {
		switch (type) {
		case "Z":
			return "boolean";
		case "B":
			return "byte";
		case "C":
			return "char";
		case "D":
			return "double";
		case "F":
			return "float";
		case "I":
			return "int";
		case "J":
			return "long";
		case "S":
			return "short";
		case "V":
			return "void";
		default:
			return type;
		}
	}
}
