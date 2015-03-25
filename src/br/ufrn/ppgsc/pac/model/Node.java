package br.ufrn.ppgsc.pac.model;

import java.util.Collections;
import java.util.List;

public class Node {
	
	protected int id;
	protected String method_signature;
	protected int parent_id;
	protected List<Node> children;
	
	public Node(int id, String method_signature, int parent_id) {
		super();
		this.id = id;
		this.method_signature = method_signature;
		this.parent_id = parent_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMethod_signature() {
		return method_signature;
	}

	public void setMethod_signature(String method_signature) {
		this.method_signature = method_signature;
	}

	public int getParent_id() {
		return parent_id;
	}

	public void setParent_id(int parent_id) {
		this.parent_id = parent_id;
	}

	public List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public void setChildren(List<Node> childrens) {
		this.children = childrens;
	}

	@Override
	public String toString() {
		return "Node [id=" + id + ", method_signature=" + method_signature
				+ ", parent_id=" + parent_id + "]";
	}
}
