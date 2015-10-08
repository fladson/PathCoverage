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
package br.ufrn.ppgsc.fc.connectors.git;

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

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import br.ufrn.ppgsc.fc.connectors.EvolutionConnector;
import br.ufrn.ppgsc.fc.jdt.MethodLimitBuilder;
import br.ufrn.ppgsc.fc.jdt.UpdatedLine;
import br.ufrn.ppgsc.fc.jdt.UpdatedMethod;

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

public class GithubConnector extends EvolutionConnector {

	private GitHubClient clientManager;
	private RepositoryService repositoryService;
	private Repository repository;
	private IRepositoryIdProvider repository_id;
	private CommitService commitService;
	private PullRequestService pullRequestService;
	
	private List<String> changedFiles;
	private List<Collection<UpdatedMethod>> changedMethods;
	private List<String> changedMethodsString;
	private List<UpdatedLine> changedLines;
	private StringBuilder sourceCode;
	private List<String> commitsOnRangeString;

	@Override
	public void performSetup() {
		repository_id = RepositoryId.createFromUrl(url);
		clientManager = new GitHubClient().setOAuth2Token(token);
		repositoryService = new RepositoryService();
		commitService = new CommitService(clientManager);
		pullRequestService = new PullRequestService(clientManager);
		try {
			repository = repositoryService.getRepository(repository_id);
			if(!repository.getUrl().equals("")){
				System.out.println("Having Github Logical Repository: " + repository.getName());
			}
		} catch (IOException e) {
			System.out.println("Erro na comunicacao do repositorio Github: ");
			e.printStackTrace();
			System.exit(0);
		}
	}

	public List<String> getChangedMethodsFromEvolution() throws Exception {
		System.out.println("\t|-Recovering changed methods from evolution");
		try {
			this.commitsOnRangeString = this.getCommitsInRange();
			changedMethods = new ArrayList<Collection<UpdatedMethod>>();
			changedMethodsString = new ArrayList<String>();
			for (String commit : commitsOnRangeString) {

				List<String> changedFilesInRevision = getFilesOfRevision(commit);
				System.out.println("\t Searching changed methods on commit: "+ commit.substring(0, 7));
				this.changedFiles = new ArrayList<String>();
				System.out.println("\t-|Modified Files: ");
				for (String file : changedFilesInRevision) {
					changedLines = new ArrayList<UpdatedLine>();
					sourceCode = new StringBuilder();

					System.out.println("\t\t" + file);
					changedFiles.add(file);
					calculateChangedLines(commit, file);

					MethodLimitBuilder builder = new MethodLimitBuilder(sourceCode.toString());
					changedMethods.add(builder.filterChangedMethods(changedLines));
				}
			}
			System.out.println("\t-|Modified Methods: ");
			for (Collection<UpdatedMethod> collection : changedMethods) {
				for (UpdatedMethod method : collection) {
					changedMethodsString.add(method.getKlass() + "."
							+ method.getMethodLimit().getSignature());
					System.out.println("\t\t" + method.getKlass() + "."
							+ method.getMethodLimit().getSignature());
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return changedMethodsString;
	}

	private List<String> getCommitsInRange() throws Exception {
		commitsOnRangeString = new ArrayList<String>();
		// Pull Request
		if(!this.getPullRequest().equals("")){
			for(RepositoryCommit commit : pullRequestService.getCommits(repository_id, new Integer(getPullRequest()))){
				commitsOnRangeString.add(commit.getSha());
			}
			
		}else{
			// Commit range
			// TODO
			if (getPreviousRevision(this.startVersion).equals("nil")) {
				System.out
						.println("O primeiro commit de um repositorio nao pode ser analisado por nao conter alteracoes\n"
								+ "Favor modificar o start version no arquivo connections.properties");
				System.exit(0);
			} else {
				if (this.startVersion.equals(this.endVersion)) {
					commitsOnRangeString.add(this.startVersion);
				} else {
					commitsOnRangeString.add(this.startVersion);
					List<RepositoryCommit> log = commitService.getCommits(repository, branch, null);
					List<String> commitsString = new ArrayList<String>();
					List<Integer> commitsOnRange = new ArrayList<Integer>();
					for (RepositoryCommit commit : log) {
						commitsString.add(commit.getSha());
					}

					for (int i = 0; i < commitsString.size() - 1; i++) {
						if (commitsString.get(i).equals(this.endVersion)) {
							commitsOnRange.add(i);
							for (int j = i + 1; !commitsString.get(j).equals(this.startVersion); j++) {
								commitsOnRange.add(j);
							}
						}
					}
					for (Integer i : commitsOnRange) {
						commitsOnRangeString.add(commitsString.get(i));
					}
				}
			}
		}
		
		return commitsOnRangeString;
	}

	@Override
	public List<String> getFilesOfRevision(String revision) {
		List<String> commitFilesString = new ArrayList<String>();
		RepositoryCommit commit;
			try {
				commit = commitService.getCommit(repository_id, revision);
				List<CommitFile> commitFiles = commit.getFiles();
				for (CommitFile commitFile : commitFiles) {
					String fileName = commitFile.getFilename().toString();
					if (fileName.contains(".java"))
						commitFilesString.add(fileName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		return commitFilesString;
	}

	private void calculateChangedLines(String commit, String filename)
			throws Exception {
		// Windows
//		String so_prefix = "cmd /c ";
		// Mac
		 String so_prefix = "";
		String command = "";
		command = so_prefix + "git blame -l " + getPreviousRevision(commit)+ ".." + commit + " " + filename;

		try {
			Process p = Runtime.getRuntime().exec(command, null, new File(this.evolutionLocalPath));
			BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			if(bf.readLine() == null){
				System.out.println("\t\t!!! EMPTY GIT BLAME  !!! Try: " + command);
				return;
			}
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

	private UpdatedLine handleLine(String gitblameline, String filename)
			throws IOException {
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
			commit_date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
					.parse(tokens.get(tokens.size() - 4) + " "
							+ tokens.get(tokens.size() - 3));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String author_name = "";
		for (int i = 0; i < tokens.size() - 4; i++)
			author_name += (i == 0 ? "" : " ") + tokens.get(i);

		sourceCode.append(source_line + System.lineSeparator());

		if (!commit.startsWith("^")) {
			return new UpdatedLine(commit_date, commit, source_line,
					author_name, line_number, filename.replace(".java", ""));
		}
		return null;
	}

	private String getPreviousRevision(String actualRevision) throws Exception {
		RepositoryCommit commit = commitService.getCommit(repository_id, actualRevision);
		// Descartando primeiro commit de um repo pois ele nao possui alteracoes
		if (commit.getParents().isEmpty()) {
			return "nil";
		} else
			return commit.getParents().get(0).getSha();
	}

	@Override
	public List<String> getMethodsChangedOfRevision(String Revision)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
