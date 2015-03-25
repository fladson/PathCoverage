package br.ufrn.ppgsc.pac.connectors.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import br.ufrn.ppgsc.pac.connectors.Connector;
import br.ufrn.ppgsc.pac.connectors.RepositoryConnector;

public class GitConnector implements RepositoryConnector {
	
	Repository repository = null;
	protected String lastRevision;

	@Override
	public List<String> getFilesOfRevision(String Revision) throws Exception {
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
	public void performSetup(String repositoryLocalPath) {
		// como o clone ficou para o github connector, aqui é feito a operação de leitura e afins
		File file = new File(repositoryLocalPath);
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			repository = builder.setWorkTree(file).readEnvironment().findGitDir().build();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
