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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

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
	
//	private String startRev;
//	private String endRev;
//	private String filedir;

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

	private void cloneRepository(){
		// Clonando no mesmo diretorio onde o projeto eclipse esta presente 
		this.repositoryLocalPath = System.getProperty("user.dir").replace("PathCoverage", this.systemName);
		File file = new File(repositoryLocalPath);
		if(!file.exists()){
			System.out.println("Clonando repositorio em: "+repositoryLocalPath);
			new File(repositoryLocalPath).mkdir();
			try {
				Git repo = null;
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
					System.out.println("Repositorio clonado para:" + repositoryPath);
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
	
	public void getFilesChanged() throws Exception{
		try {
			this.commitsOnRangeString = this.getCommitsInRange();
			changedMethods = new ArrayList<Collection<UpdatedMethod>>();
			for (String commit : commitsOnRangeString) {
				List<String> changedFilesInRevision = getFilesOfRevision(commit);
				System.out.println("Arquivos modificados em: " + commit.substring(0, 7));
				this.changedFiles = new ArrayList<String>();
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
			for (Collection<UpdatedMethod> collection : changedMethods) {
				for(UpdatedMethod method : collection ){
					System.out.println(method.getMethodLimit().getSignature());
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
			ObjectId start = repository.resolve(this.startVersion);
			ObjectId end = repository.resolve(this.endVersion);
			Iterable<RevCommit> commitsInRange = new Git(repository).log().addRange(start,end).call();	
			for (RevCommit revCommit : commitsInRange) {
				commitsOnRangeString.add(revCommit.name());
			}
			commitsOnRangeString.add(this.startVersion);
		}
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
	
	
	public void calculateChangedLines(String commit, String filename) {
		String so_prefix = "cmd /c ";
		String command =  "git blame -l " + commit + ".." + commit + " " + filename;
		String path = this.repositoryLocalPath.replace('\\', '/')+"/";
		try {
			Process p = Runtime.getRuntime().exec(command, null, new File(path));
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
