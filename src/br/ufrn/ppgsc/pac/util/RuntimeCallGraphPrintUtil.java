package br.ufrn.ppgsc.pac.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import br.ufrn.ppgsc.pac.model.Node;


//import br.ufrn.ppgsc.scenario.analyzer.cdynamic.model.RuntimeGenericAnnotation;
//import br.ufrn.ppgsc.scenario.analyzer.cdynamic.model.RuntimeNode;
//import br.ufrn.ppgsc.scenario.analyzer.cdynamic.model.RuntimeScenario;

public abstract class RuntimeCallGraphPrintUtil {
	
	private static void printInOrder(Node root, Appendable buffer) throws IOException {
		buffer.append(root.getMethod_signature());
		
		for (Node node : root.getChildren()) {
			buffer.append(" > ");
			printInOrder(node, buffer);
		}
	}
	
	private static void printTreeNode(Node root, String tabs, Appendable buffer) throws IOException {
		buffer.append(tabs + root.getMethod_signature() + " - " + root.getId() +
				" (" + " Parent: " +  "-" + root.getParent_id());
		
		buffer.append(System.lineSeparator());
		
		for (Node node : root.getChildren())
			printTreeNode(node, tabs + "   ", buffer);
	}

}
