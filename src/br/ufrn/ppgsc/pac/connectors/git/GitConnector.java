package br.ufrn.ppgsc.pac.connectors.git;

import java.util.List;

import br.ufrn.ppgsc.pac.connectors.RepositoryConnector;

public class GitConnector implements RepositoryConnector {

	@Override
	public List<String> getFilesOfRevision(String Revision) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMethodsChangedOfRevision(String Revision)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPreviousRevision(String path, String actualRevision)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRepositoryPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performSetup() {
		System.out.println("performing setup from git connector");
	}

}
