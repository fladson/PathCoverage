package br.ufrn.ppgsc.fc.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import br.ufrn.ppgsc.fc.util.MemberUtil;


@Entity(name = "node")
public class RuntimeNode implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "member", columnDefinition = "text")
	private String memberSignature;

//	@Column(name = "exception", columnDefinition = "text")
//	private String exceptionMessage;
//
//	@Column(name = "time")
//	private long executionTime;
	
//	@Column(name = "real_time")
//	private long realExecutionTime;
//
//	@Column(name = "constructor")
//	private boolean isConstructor;
//
//	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	@JoinTable(name = "node_annotation",
//		joinColumns = @JoinColumn(name = "node_id"),
//		inverseJoinColumns = @JoinColumn(name = "annotation_id"))
//	private Set<RuntimeGenericAnnotation> annotations;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	private RuntimeNode parent;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	@OrderBy("id asc")
	private List<RuntimeNode> children;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "node_scenario",
		joinColumns = @JoinColumn(name = "node_id"),
		inverseJoinColumns = @JoinColumn(name = "scenario_id"))
	@OrderBy("root asc")
	private List<RuntimeScenario> scenarios;

	public RuntimeNode() {

	}

	public RuntimeNode(Member member) {
		children = new ArrayList<RuntimeNode>();
		scenarios = new ArrayList<RuntimeScenario>();
		memberSignature = MemberUtil.getStandartMethodSignature(member);
//		annotations = RuntimeCallGraph.getInstance().parseMemberAnnotations(member);
//		isConstructor = member instanceof Constructor;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMemberSignature() {
		return memberSignature;
	}

	public void setMemberSignature(String memberSignature) {
		this.memberSignature = memberSignature;
	}

//	public String getExceptionMessage() {
//		return exceptionMessage;
//	}
//
//	public void setExceptionMessage(String exceptionMessage) {
//		this.exceptionMessage = exceptionMessage;
//	}
//
//	public long getExecutionTime() {
//		return executionTime;
//	}
//	
//	public long getRealExecutionTime() {
//		return realExecutionTime;
//	}
//	
//	public void setRealExecutionTime(long realExecutionTime) {
//		this.realExecutionTime = realExecutionTime;
//	}
//
//	public void setExecutionTime(long executionTime) {
//		this.executionTime = executionTime;
//	}
//
//	public boolean isConstructor() {
//		return isConstructor;
//	}
//
//	public void setConstructor(boolean isConstructor) {
//		this.isConstructor = isConstructor;
//	}

	public List<RuntimeScenario> getScenarios() {
		return Collections.unmodifiableList(scenarios);
	}

	public void setScenarios(List<RuntimeScenario> scenarios) {
		this.scenarios = scenarios;
	}

	public void addScenario(RuntimeScenario scenario) {
		scenarios.add(scenario);
	}
//
//	public Set<RuntimeGenericAnnotation> getAnnotations() {
//		return annotations;
//	}
//
//	public void setAnnotations(Set<RuntimeGenericAnnotation> annotations) {
//		this.annotations = Collections.unmodifiableSet(annotations);
//	}

	public RuntimeNode getParent() {
		return parent;
	}

	public void setParent(RuntimeNode parent) {
		this.parent = parent;
	}

	public List<RuntimeNode> getChildren() {
//		return Collections.unmodifiableList(children);
		return children;
	}

	public void setChildren(List<RuntimeNode> children) {
		this.children = children;
	}

	public void addChild(RuntimeNode node) {
		children.add(node);
	}

}
