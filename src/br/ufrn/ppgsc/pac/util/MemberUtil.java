package br.ufrn.ppgsc.pac.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/* TODO: Ver como juntar esses métodos com os outros de assinatura
 * sem gerar problemas com as dependências do classpath
 */
public abstract class MemberUtil {

	public static String getStandartMethodSignature(Member member) {
		StringBuilder result = new StringBuilder();
		Class<?>[] ptypes = null;

		if (member instanceof Method) {
			result.append(member.getDeclaringClass().getName());
			result.append(".");
			result.append(member.getName());

			ptypes = ((Method) member).getParameterTypes();
		} else if (member instanceof Constructor) {
			result.append(member.getName());
			ptypes = ((Constructor<?>) member).getParameterTypes();
		}

		result.append("(");

		for (int i = 0; i < ptypes.length; i++) {
			result.append(ptypes[i].getCanonicalName());

			if (i + 1 < ptypes.length)
				result.append(",");
		}

		result.append(")");

		return result.toString();
	}

}
