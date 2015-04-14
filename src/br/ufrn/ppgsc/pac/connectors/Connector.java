package br.ufrn.ppgsc.pac.connectors;

import java.util.Collection;
import java.util.List;

import br.ufrn.ppgsc.pac.model.UpdatedMethod;


/**
 * UNIVERSIDADE FEDERAL DO RIO GRANDE DO NORTE - UFRN
 * DEPARTAMENTO DE INFORMATICA E MATEMATICA APLICADA - DIMAP
 * Programa de Pos-Graduacao em Sistemas e Computacao - PPGSC
 */

/**
 * A Connector contains information about to connect to a service or repository.
 * @author fladson - fladsonthiago@gmail.com
 * @since 20/03/2015
 */
public abstract class Connector {

	protected String systemName;
	protected String branch;
	protected String pullRequests;
	protected String startVersion;
	protected String endVersion;

	protected String url;
	protected String user;
	protected String password;
	protected String repositoryLocalPath;
	
	protected String side;

	public Connector() {

	}

	/**
	 * Configure the connection information in the connector, have to be called
	 * after all the data be full filled
	 */
	public void performSetup() {

	}

	/**
	 * Execute what the connector have to execute. have to the called after
	 * performSetup method.
	 */
	public void performRun() {

	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getStartVersion() {
		return startVersion;
	}

	public void setStartVersion(String startVersion) {
		this.startVersion = startVersion;
	}

	public String getEndVersion() {
		return endVersion;
	}

	public void setEndVersion(String endVersion) {
		this.endVersion = endVersion;
	}

	public String getRepositoryLocalPath() {
		return repositoryLocalPath;
	}

	public void setRepositoryLocalPath(String repositoryLocalPath) {
		this.repositoryLocalPath = repositoryLocalPath;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getPullRequests() {
		return pullRequests;
	}

	public void setPullRequests(String pullRequests) {
		this.pullRequests = pullRequests;
	}

	public String getSide() {
		return side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	@Override
	public String toString() {
		return "Connector [systemName=" + systemName + ", branch=" + branch
				+ ", pullRequests=" + pullRequests + ", startVersion="
				+ startVersion + ", endVersion=" + endVersion + ", url=" + url
				+ ", user=" + user + ", password=" + password
				+ ", repositoryLocalPath=" + repositoryLocalPath + ", side="
				+ side + "]";
	}
	
	public List<String> parseMethodsChangedOnCommitsRange() throws Exception{
		return null;
	}
}
