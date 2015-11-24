package br.ufrn.ppgsc.fc.util;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.strings.Atom;

public abstract class MethodUtil {

	public static String getStandartMethodSignature(IMethod method) {
		StringBuffer result = new StringBuffer();
		Atom methodPackage = method.getDeclaringClass().getName().getPackage();
		if (methodPackage != null) {
			result.append(method.getDeclaringClass().getName().getPackage().toString().replaceAll("/", "."));
			result.append(".");
		}
		result.append(method.getDeclaringClass().getName().getClassName());
		result.append(".");
		if (method.isInit())
			result.append(method.getDeclaringClass().getName().getClassName());
		else
			result.append(method.getName());
		result.append("(");
		for (int i = 0; i < method.getSelector().getDescriptor().getNumberOfParameters(); i++) {
			TypeName type = method.getSelector().getDescriptor().getParameters()[i];

			if (type.getPackage() != null) {
				result.append(type.getPackage().toString().replaceAll("/", "."));
				result.append(".");
			}
			result.append(convertTypeSignatureToName(type.getClassName().toString()));
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

	public static String convertTypeSignatureToName(String type) {
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
