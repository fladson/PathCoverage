package br.ufrn.ppgsc.pac.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import br.ufrn.ppgsc.pac.model.Node;
import br.ufrn.ppgsc.pac.model.RuntimeNode;
import br.ufrn.ppgsc.pac.model.RuntimeScenario;


public abstract class RuntimeCallGraphPrintUtil {
	
	public static void printScenarioTree(RuntimeScenario tree, Appendable buffer) throws IOException {
//		buffer.append("Scenario: " + tree.getName());
		
		if (tree.getContext() != null && !tree.getContext().isEmpty()) {
			Iterator<String> itr = tree.getContext().values().iterator();
			while (itr.hasNext()) {
				buffer.append(itr.next());
				
				if (itr.hasNext())
					buffer.append(", ");
			}
		}
		else {
			buffer.append("-");
		}
		
//		buffer.append(")\n");

//		printInOrder(tree.getRoot(), buffer);
		buffer.append(System.lineSeparator());
		printTreeNode(tree.getRoot(), "", buffer);
	}
	
	private static void printInOrder(RuntimeNode root, Appendable buffer) throws IOException {
		buffer.append(root.getMemberSignature());
		
		for (RuntimeNode node : root.getChildren()) {
			buffer.append(" > ");
			printInOrder(node, buffer);
		}
	}
	
	private static void printTreeNode(RuntimeNode root, String tabs, Appendable buffer) throws IOException {
//		buffer.append(tabs + root.getMemberSignature() + " - " + root.getId() +
//				" (" + root.getExecutionTime() + "ms, " +
//				(root.getExecutionTime() == -1 ? true : false) + ") Parent: " + 
//				(root.getParent() == null ? "-" : root.getParent().getId()));
		
		buffer.append(tabs + root.getMemberSignature());
		
//		buffer.append(" | Scenarios: ");
//		for (int i = 0; i < root.getScenarios().size(); i++) {
//			RuntimeScenario rs = root.getScenarios().get(i);
//			
//			buffer.append(rs.getName() + " [" + rs.getId() + "]");
//			
//			if (i + 1 < root.getScenarios().size())
//				buffer.append(", ");
//		}
		
//		buffer.append(" | " + (root.getExceptionMessage() == null ? "-" : root.getExceptionMessage()));
		
//		Set<RuntimeGenericAnnotation> annotations = root.getAnnotations();
//		
//		if (annotations != null && !annotations.isEmpty())
//			for (RuntimeGenericAnnotation ann : annotations)
//				buffer.append(" | " + ann.getClass().getSimpleName() + ": " + ann.getName());
//		
		buffer.append(System.lineSeparator());
		
		for (RuntimeNode node : root.getChildren())
			printTreeNode(node, tabs + ">", buffer);
	}
}
