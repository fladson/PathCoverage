package br.ufrn.ppgsc.pac.connectors.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import br.ufrn.ppgsc.pac.connectors.EvolutionConnector;
import br.ufrn.ppgsc.pac.connectors.SCMConnector;

public class GitConnector extends SCMConnector {
	
	private Repository localRepository;
	private FileRepositoryBuilder builder;

	@Override
	public void performSetup() {
		builder = new FileRepositoryBuilder();
		try {
			localRepository = builder.findGitDir(new File(this.repositoryLocalPath)).build();
		    ObjectId headId = localRepository.resolve(Constants.HEAD);
			if (headId != null) {
				System.out.println("Having local repository at: " + localRepository.getDirectory());
				localRepository.close();
			} else {
				System.out.println("Error reading local git repository");
				System.exit(0);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
