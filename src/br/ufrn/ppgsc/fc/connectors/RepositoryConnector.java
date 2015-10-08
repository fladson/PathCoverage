package br.ufrn.ppgsc.pac.connectors;

import java.util.List;

public interface RepositoryConnector {
	
	public abstract List<String> getFilesOfRevision(String Revision) throws Exception;
	
	public abstract List<String> getMethodsChangedOfRevision(String Revision) throws Exception;

	public String getPreviousRevision(String path, String actualRevision) throws Exception;
	
	public abstract void performSetup(String repositoryLocalPath);

	public abstract String getRepositoryPath();

}
