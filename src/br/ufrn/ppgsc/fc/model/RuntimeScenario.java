package br.ufrn.ppgsc.fc.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;

//import br.ufrn.ppgsc.scenario.analyzer.cdynamic.util.RuntimeCallGraph;

@Entity(name = "scenario")
public class RuntimeScenario implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String name;

	@Column
	private Date date;

	@Column(name = "thread_id")
	private long threadId;
//
//	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	@JoinColumn(name = "execution_id")
//	private SystemExecution execution;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private RuntimeNode root;
	
	// Usar estas anotações no sigaa.
//	@CollectionOfElements
//	@JoinTable(name = "scenario_context", joinColumns = @JoinColumn(name = "id"))
//	@MapKey(columns = @Column(name = "key"))
//	@Column(name = "value")
	
	/*
	 * Este código assume Hibernate Annotations 3.4 ou superior, com JP 2.0 ou superior
	 * As anotações antigas estão acima para mantermos a compatibilidade com o SIGAA
	 * que usa uma versão mais antiga do Hibernate
	 */
//	@ElementCollection
//	@CollectionTable(name = "scenario_context", joinColumns = @JoinColumn(name = "id"))
//	@MapKeyColumn(name = "key")
//	@Column(name = "value")
//	private Map<String, String> context;

	public RuntimeScenario() {

	}

	public RuntimeScenario(String name, RuntimeNode root) {
//		this.threadId = Thread.currentThread().getId();
		this.root = root;
		this.name = name;
//		this.date = new Date();
//		this.execution = RuntimeCallGraph.getInstance().getCurrentExecution();

//		if (context != null)
//			this.context = Collections.unmodifiableMap(context);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

//	public SystemExecution getExecution() {
//		return execution;
//	}
//
//	public void setExecution(SystemExecution execution) {
//		this.execution = execution;
//	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public RuntimeNode getRoot() {
		return root;
	}

	public void setRoot(RuntimeNode root) {
		this.root = root;
	}

//	public Map<String, String> getContext() {
//		return context;
//	}
//
//	public void setContext(Map<String, String> context) {
//		this.context = context;
//	}
	
	@Override
	public String toString() {
		return getName();
	}

}
