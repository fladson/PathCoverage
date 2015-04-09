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
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import br.ufrn.ppgsc.pac.connectors.Connector;
import br.ufrn.ppgsc.pac.jdt.MethodLimitBuilder;
import br.ufrn.ppgsc.pac.model.UpdatedLine;
import br.ufrn.ppgsc.pac.model.UpdatedMethod;

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
	private org.eclipse.egit.github.core.Repository githubRepository;
	private IRepositoryIdProvider repository_id;
	private CommitService commitService;
	private String repositoryPath;
	private List<String> changedFiles;
	private List<Collection<UpdatedMethod>> changedMethods;
	private List<UpdatedLine> changedLines;
	private StringBuilder sourceCode;
	private Repository repository;
	private List<String> commitsOnRangeString;

	@Override
	public void performSetup() {
		if(this.side == "TARGET"){
			performLocalSetup();
		}else{
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
		}
		
		// return githubRepository;
	}

	private void cloneRepository(){
		// Clonando no mesmo diretorio onde o projeto eclipse esta presente 
		this.repositoryLocalPath = System.getProperty("user.dir").replace("PathCoverage", this.systemName);
		File file = new File(repositoryLocalPath);
		Git repo = null;
		if(!file.exists()){
			System.out.println("Clonando repositorio em: "+repositoryLocalPath);
			new File(repositoryLocalPath).mkdir();
			try {
				if(this.branch.isEmpty()){
					// Se nao vier especificado o branch, o master eh o padrao
					repo = Git.cloneRepository()
					.setURI(githubRepositoryService.getRepository(repository_id).getCloneUrl())
					.setDirectory(file)
					.call();
				}else{
					repo = Git.cloneRepository()
					.setURI(githubRepositoryService.getRepository(repository_id).getCloneUrl())
					.setDirectory(file)
					.setBranch(branch)
					.call();
				}				
				if(repo.getRepository() == null)
					System.out.println("Erro no clone do repositorio");
				else{
					repositoryPath = repo.getRepository().getDirectory().getAbsolutePath();
					System.out.println("Repositorio clonado para: " + repositoryPath);
//					System.out.println("-|Buscando metodos alterados no repositorio " + repo.getRepository().getWorkTree().getAbsolutePath() + "...");
				}
					
			} catch (GitAPIException | IOException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("Repositorio ja foi clonado, atualizando localmente...");
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			try {
				repository =  builder.findGitDir(file).build();
				PullResult call = new Git(repository).pull().call();
				System.out.println("Repositorio atulizado localmente");
			    repository.close();
			} catch (IOException | GitAPIException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Setup para o repo SOURCE que ja foi clonado localmente para analise dos metodos cobertos
	 */
	private void performLocalSetup(){
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			repository =  builder.findGitDir(new File(this.repositoryLocalPath)).build();
//			PullResult call = new Git(repository).pull().call();
			if(repository.getWorkTree()!=null){
				System.out.println("Repositorio recuperado localmente");
			    repository.close();
			}else{
				System.out.println("Não foi possivel recuperar o repositorio localmente");
				return;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void parseMethodsChangedOnCommitsRange() throws Exception{
		System.out.println("Recuperando metodos modificados");
		try {
			this.commitsOnRangeString = this.getCommitsInRange();
			changedMethods = new ArrayList<Collection<UpdatedMethod>>();
			for (String commit : commitsOnRangeString) {
				List<String> changedFilesInRevision = getFilesOfRevision(commit);
				System.out.println("Buscando metodos alterados no commit: " + commit.substring(0, 7));
				this.changedFiles = new ArrayList<String>();
				System.out.println("Arquivos modificados: ");
				for (String file : changedFilesInRevision) {
					changedLines = new ArrayList<UpdatedLine>();
					sourceCode = new StringBuilder();
					
					System.out.println("\t" + file);
					changedFiles.add(file);
					calculateChangedLines(commit, file);
					
					MethodLimitBuilder builder = new MethodLimitBuilder(sourceCode.toString());
					changedMethods.add(builder.filterChangedMethods(changedLines));
				}
			}
			System.out.println("Metodos modificados: ");
			for (Collection<UpdatedMethod> collection : changedMethods) {
				for(UpdatedMethod method : collection ){
					System.out.println(method.getKlass() + "." + method.getMethodLimit().getSignature());
				}
			}
			
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
	}

	private List<String> getCommitsInRange() throws IOException, NoHeadException, GitAPIException{
		commitsOnRangeString = new ArrayList<String>();
		if(this.startVersion.equals(this.endVersion)){
			commitsOnRangeString.add(this.startVersion);
		}else{
			Iterable<RevCommit> logs = new Git(repository).log().call();
	        List<String> commitsString = new ArrayList<String>();
	        List<Integer> commitsOnRange = new ArrayList<Integer>();
	        for(RevCommit c : logs){
	        	commitsString.add(c.getId().getName());
	        }
	        
	        for (int i = 0 ; i < commitsString.size()-1; i++) {
	        	if(commitsString.get(i).equals(this.endVersion)){
	        		commitsOnRange.add(i);
	        		for(int j = i+1; !commitsString.get(j).equals(this.startVersion); j++){
	        			commitsOnRange.add(j);
	        		}
	        	}
			}
	        
	        for(Integer i : commitsOnRange){
	        	commitsOnRangeString.add(commitsString.get(i));
	        }
	     }
	        commitsOnRangeString.add(this.startVersion);
//	        
//			ObjectId start = repository.resolve(this.startVersion);
//			ObjectId end = repository.resolve(this.endVersion);
//			Iterable<RevCommit> commitsInRange = new Git(repository).log().addRange(start,end).call();
//			List<RevCommit> commitsOnRange  = new ArrayList<RevCommit>();
//			Iterable<RevCommit> start_only = new Git(repository).log().add(start).setMaxCount(1).call();
//			
//			for (RevCommit commit : start_only) {
//				System.out.println(commit.abbreviate(7).name() + " " + commit.getShortMessage());
//				commitsOnRange.add(commit);
//			}
//			for (RevCommit revCommit : commitsInRange) {
//				commitsOnRangeString.add(revCommit.name());
//			}
//			commitsOnRangeString.add(this.startVersion);
		
		return commitsOnRangeString;
	}
	
	private List<String> getFilesOfRevision(String revision) {
		List<String> commitFilesString = new ArrayList<String>();
		RepositoryCommit lastCommit;
		try {
			if(revision.isEmpty()){
				// pegar ultimo commit do branch atual
				String last_revision = commitService.getCommits(repository_id).get(0).getSha();
				lastCommit = commitService.getCommit(repository_id,last_revision);
			}else{
				lastCommit = commitService.getCommit(repository_id, revision);
			}
			List<CommitFile> commitFiles = lastCommit.getFiles();
			for (CommitFile commitFile : commitFiles) {
				String fileName = commitFile.getFilename().toString();
				if(fileName.contains(".java"))
					commitFilesString.add(fileName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return commitFilesString;
	}
	
	
	private void calculateChangedLines(String commit, String filename) throws Exception {
		// Windows
		String so_prefix = "cmd /c ";
		// Mac
//		String so_prefix = "";
		String command = "";
		if(getPreviousRevision(commit) != null )
			command =  so_prefix +  "git blame -l " + getPreviousRevision(commit) + ".." + commit + " " + filename;
		else
			command =  so_prefix +  "git blame -l " + commit + ".." + commit + " " + filename;
		try {
			Process p = Runtime.getRuntime().exec(command, null, new File(this.repositoryLocalPath));
			BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = null;
			while ((line = bf.readLine()) != null) {
				UpdatedLine up = handleLine(line, filename);
				
				if (up != null)
					changedLines.add(up);
			}
			
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private UpdatedLine handleLine(String gitblameline, String filename) throws IOException {
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
		
		if (!commit.startsWith("^")) {
			return new UpdatedLine(commit_date, commit, source_line, author_name, line_number, filename.replace(".java", ""));
		}
		return null;
	}
	
	private String getPreviousRevision(String actualRevision)
			throws Exception {
		
		RepositoryCommit commit = commitService.getCommit(repository_id, actualRevision);
		//System.out.print("Pais: ");
//		for (Commit p : commit.getParents()) {
//			System.out.println(p.getSha()+ " - ");
//		}
		// TRATAR EXCECAO DE PRIMEIRO COMMIT
		return commit.getParents().get(0).getSha();
	}
	
	
}
