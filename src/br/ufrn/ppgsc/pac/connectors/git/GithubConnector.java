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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

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
import br.ufrn.ppgsc.pac.model.UpdatedLine;

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
	private String repositoryPath;
	
	private Iterable<RevCommit> commitsInRange;
	private List<RevCommit> commitsOnRange;
	
	private List<String> changedFiles;
	private List<UpdatedLine> changedLines;
	private StringBuilder sourceCode;
	
	private String startRev;
	private String endRev;
	private String filedir;

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
					System.out.println("Erro no clone do repositorio");
				else{
					repositoryPath = repo.getRepository().getDirectory().getAbsolutePath();
					System.out.println("Repositorio clonado para:" + repositoryPath);
				}
					
			} catch (GitAPIException | IOException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("Repositorio ja foi clonado, atualizando localmente...");
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
	
	public List<String> getMethodsChangedOfRevision(String revision){
		changedFiles = this.getFilesOfRevision(revision);
		if(!changedFiles.isEmpty()){
			for (String string : changedFiles) {
				// Pegando mudancas de cada arquivo alterado no commit
				changedLines = new ArrayList<UpdatedLine>();
				sourceCode = new StringBuilder();
				
			}
			
		}else{
			System.out.println("Lista de arquivos modificados vazia");
		}
		
		return null;
	}
	
	/*
	 * TODO Remover arquivos nao java dessa lista antes de repassar
	 */
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
	
	public void calculateChangedLines(String filename) {
		String so_prefix = "cd " + repositoryPath;
		String command = so_prefix + "git blame -l " + startRev + ".." + endRev + " " + filename;
		
		try {
			Process p = Runtime.getRuntime().exec(command, null, new File(filedir));
			BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = null;
			while ((line = bf.readLine()) != null) {
				UpdatedLine up = handleLine(line);
				
				if (up != null)
					changedLines.add(up);
			}
			
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private UpdatedLine handleLine(String gitblameline) throws IOException {
		Scanner in = new Scanner(gitblameline);
		
		String commit = in.next();
		
		List<String> tokens = new ArrayList<String>();
		while (true) {
			String t = in.next();
			
			if (t.endsWith(")")) {
				t = t.substring(0, t.length() - 1);
				tokens.add(t);
				break;
			}
			
			if (t.startsWith("("))
				t = t.substring(1);

			tokens.add(t);
		}
		
		String source_line = in.nextLine().substring(1);
		
		in.close();
		
		int line_number = Integer.parseInt(tokens.get(tokens.size() - 1));
		
		Date commit_date = null;
		try {
			commit_date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(
					tokens.get(tokens.size() - 4) + " " + tokens.get(tokens.size() - 3));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String author_name = "";
		for (int i = 0; i < tokens.size() - 4; i++)
			author_name += (i == 0 ? "" : " ") + tokens.get(i);
		
		sourceCode.append(source_line + System.lineSeparator());
		
		return new UpdatedLine(commit_date, commit, author_name, source_line, line_number);

	}
}
