/**
 * UNIVERSIDADE FEDERAL DO RIO GRANDE DO NORTE - UFRN
 * DEPARTAMENTO DE INFORMATICA E MATEMATICA APLICADA - DIMAP
 * Programa de Pos-Graduacao em Sistemas e Computacao - PPGSC
 */

/**
 * GithubConnector.java
 * @author fladson - fladsonthiago@gmail.com
 * @since 20/03/2015
 *
 */
package br.ufrn.ppgsc.pac.connectors.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import br.ufrn.ppgsc.pac.connectors.Connector;

/**
 * UNIVERSIDADE FEDERAL DO RIO GRANDE DO NORTE - UFRN
 * DEPARTAMENTO DE INFORMATICA E MATEMATICA APLICADA - DIMAP
 * Programa de Pos-Graduacao em Sistemas e Computacao - PPGSC
 */

/**
 * GithubConnector.java
 * 
 * @author fladson - fladsonthiago@gmail.com
 * @since 20/03/2015
 *
 */

public class GithubConnector extends Connector {

	private GitHubClient githubClientManager;
	private RepositoryService githubRepositoryService;
	private Repository githubRepository;
	private IRepositoryIdProvider repository_id;

	private CommitService commitService;
	
	private Iterable<RevCommit> commitsInRange;
	private List<RevCommit> commitsOnRange;

	@Override
	public void performSetup() {
		repository_id = RepositoryId.createFromUrl(url);
		githubClientManager = new GitHubClient().setOAuth2Token(password);
		githubRepositoryService = new RepositoryService();
		commitService = new CommitService(githubClientManager);
		try {
			githubRepository = githubRepositoryService.getRepository(repository_id);
			cloneRepository();
		} catch (IOException e) {
			System.out.println("Erro na comunicacao do repositorio Github: ");
			e.printStackTrace();
		}
		// return githubRepository;
	}

	public void cloneRepository(){
		this.repositoryLocalPath = System.getProperty("user.dir").replace("PathCoverage", this.systemName);
		File file = new File(repositoryLocalPath);
		if(!file.exists()){
			System.out.println("Creating a local project: "+repositoryLocalPath);
			new File(repositoryLocalPath).mkdir();
			try {
				Git repo = Git.cloneRepository()
				.setURI(githubRepositoryService.getRepository(repository_id).getCloneUrl())
				.setDirectory(file)
				.call();
				if(repo.getRepository() == null)
					System.out.println("Erro no clone do repositório");
				else
					System.out.println("Repositório clonado para:" + repo.getRepository().getDirectory());
			} catch (GitAPIException | IOException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("Repositório já foi clonado, atualizando localmente...");
			// Tentar fazer o pull aqui
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			try {
				org.eclipse.jgit.lib.Repository repository = builder.findGitDir(file).build();
				PullResult call = new Git(repository).pull().call();
				System.out.println("Pulled from the remote repository: " + call);
			    repository.close();
			} catch (IOException | GitAPIException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<String> getMethodsChangedOfRevision(String Revision){
		return null;
	}
	
	public List<String> getFilesOfRevision(String Revision) {
		List<String> commitFilesString = new ArrayList<String>();
		RepositoryCommit lastCommit;
		try {
			lastCommit = commitService.getCommit(repository_id, Revision);
			List<CommitFile> commitFiles = lastCommit.getFiles();
			
			for (CommitFile commitFile : commitFiles) {
				commitFilesString.add(commitFile.getFilename().toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return commitFilesString;
	}
}
