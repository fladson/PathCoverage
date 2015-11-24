package br.ufrn.ppgsc.fc.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.ArgumentTypeEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class SpecificApplicationEntryPoints extends HashSet<Entrypoint> {

	public SpecificApplicationEntryPoints(AnalysisScope scope, IClassHierarchy cha, List<String> specific) {
		if (cha == null || specific == null || specific.size() <=0) {
			throw new IllegalArgumentException("cha or specific is null");
		}
		
		int count = 0;
		for (IClass klass : cha) {
//			if(klass.getName().toString().contains("MBean")){
//				count++;
//				System.out.println(count + ": " + klass.getName());
//			}
			if (!klass.isInterface()) {
				if (isApplicationClass(scope, klass)) {
//					System.out.println(klass.getName());
					for (Iterator methodIt = klass.getDeclaredMethods().iterator(); methodIt.hasNext();) {
						IMethod method = (IMethod) methodIt.next();
						String methodFormatted = MethodUtil.getStandartMethodSignature(method);
						if (!method.isAbstract()) {
							for(String sp:specific){
								if(methodFormatted.contains(sp)){
									add(new ArgumentTypeEntrypoint(method, cha));
									System.out.println("\t-| Specific entrypoint added: " + methodFormatted);
								}
							}
//							System.out.println(">>>" + methodFormatted);
						}
					}
				}
			}
		}
	}

	private boolean isApplicationClass(AnalysisScope scope, IClass klass) {
		return scope.getApplicationLoader().equals(klass.getClassLoader().getReference());
	}

}
