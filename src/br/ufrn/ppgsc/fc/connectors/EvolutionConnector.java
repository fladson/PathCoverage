package br.ufrn.ppgsc.fc.connectors;

import java.util.List;


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
public abstract class EvolutionConnector {

	protected String systemName;
	protected String branch;
	protected String pullRequest;
	protected String evolutionLocalPath;
	protected String startVersion;
	protected String endVersion;

	protected String url;
	protected String user;
	protected String token;
	
	protected String repositoryLocalPath;

	public EvolutionConnector() {

	}

	public void performSetup() {

	}
	
	public abstract List<String> getFilesOfRevision(String Revision) throws Exception;
	
	public abstract List<String> getMethodsChangedOfRevision(String Revision) throws Exception;

	public String getPreviousRevision(String path, String actualRevision) throws Exception {
		return null;
	}
	
	public List<String> getChangedMethodsFromEvolution() throws Exception{
		return null;
	}
	
	// Gets e Sets

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setToken(String token) {
		this.token = token;
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

	public String getPullRequest() {
		return pullRequest;
	}

	public void setPullRequest(String pullRequest) {
		this.pullRequest = pullRequest;
	}

	public String getEvolutionLocalPath() {
		return evolutionLocalPath;
	}

	public void setEvolutionLocalPath(String evolutionLocalPath) {
		this.evolutionLocalPath = evolutionLocalPath;
	}

	@Override
	public String toString() {
		return "Connector [systemName=" + systemName + ", branch=" + branch
				+ ", pullRequests=" + pullRequest + ", startVersion="
				+ startVersion + ", endVersion=" + endVersion + ", url=" + url
				+ ", user=" + user + ", password=" + token
				+ ", repositoryLocalPath=" + repositoryLocalPath  + "]";
	}
	
}
