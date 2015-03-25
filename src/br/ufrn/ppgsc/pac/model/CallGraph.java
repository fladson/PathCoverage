package br.ufrn.ppgsc.pac.model;

import java.io.IOException;
import java.util.List;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

public class CallGraph {

	protected Graph<Node, DefaultEdge> grafo;

	public CallGraph() {
		super();
		grafo = new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);
	}
	
	public List<Node> getAllRoots(List<Node> nodes){
		
		return null;
	}
	
}
