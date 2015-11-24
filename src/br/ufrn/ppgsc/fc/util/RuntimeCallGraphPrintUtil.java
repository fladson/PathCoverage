package br.ufrn.ppgsc.fc.util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import br.ufrn.ppgsc.fc.model.RuntimeNode;
import br.ufrn.ppgsc.fc.model.RuntimeScenario;


public abstract class RuntimeCallGraphPrintUtil {

	
	public static void logScenarioTree(RuntimeScenario tree, PrintStream ps) throws IOException {
		logTreeNode(tree.getRoot(), "", ps);
	}
	
	private static void logTreeNode(RuntimeNode root, String tabs, PrintStream ps) throws IOException {
		ps.println(tabs + root.getMemberSignature());
		System.out.println(tabs + root.getMemberSignature());
		
		for (RuntimeNode node : root.getChildren())
			logTreeNode(node, tabs + ">", ps);
	}
	
	private static List<RuntimeNode> getChildrenFromList(RuntimeNode root){
		
		return null;
	}

}