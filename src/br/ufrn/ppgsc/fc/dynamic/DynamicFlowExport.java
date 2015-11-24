package br.ufrn.ppgsc.fc.dynamic;

import java.io.IOException;
import java.util.Iterator;

import br.ufrn.ppgsc.fc.model.RuntimeNode;
import br.ufrn.ppgsc.fc.model.RuntimeScenario;


public abstract class DynamicFlowExport {
	
//	public static void printScenarioTree(RuntimeScenario tree, Appendable buffer) throws IOException {
//		buffer.append("Scenario: " + tree.getName());
//		if (tree.getContext() != null && !tree.getContext().isEmpty()) {
//			Iterator<String> itr = tree.getContext().values().iterator();
//			while (itr.hasNext()) {
//				buffer.append(itr.next());
//				if (itr.hasNext())
//					buffer.append(", ");
//			}
//		}
//		else {
//			buffer.append("-");
//		}
//		
////		buffer.append(")\n");
//
////		printInOrder(tree.getRoot(), buffer);
//		System.out.println(buffer);
//		buffer.append(System.lineSeparator());
//		printTreeNode(tree.getRoot(), "", buffer);
//	}
	
	private static void printInOrder(RuntimeNode root, Appendable buffer) throws IOException {
		buffer.append(root.getMemberSignature());
		for (RuntimeNode node : root.getChildren()) {
			buffer.append(" > ");
			printInOrder(node, buffer);
		}
	}
	
	private static void printTreeNode(RuntimeNode root, String tabs, Appendable buffer) throws IOException {
		buffer.append(tabs + root.getMemberSignature());
		buffer.append(System.lineSeparator());
		for (RuntimeNode node : root.getChildren())
			printTreeNode(node, tabs + ">", buffer);
	}
}
