import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

/*
 * Algoritmo de comparação dos arquivos com os paths
 * 
 * 1. Remover os métodos de testes do arquivo WALA
 * 2. Extrair métodos cobertos, contidos no arquivo da ferramenta de Felipe, do arquivo WALA
 * 3. O que ficar restante são os métodos não cobertos da versão
 * 4. Fazer os passos acima na versão evoluída
 * 5. Remover métodos iguais entre as versões
 * 6. O que ficar restante são os métodos da nova versão não cobertos pelos testes
 * 
 * Abordagem para usar algoritmo de Diff
 * 1. Remover os métodos de testes do arquivo WALA
 * 2. Ordenar os arquivos em ordem alfabética
 * 3. Rodar o diff entre eles
 * 4. As mudanças são os métodos não cobertos
 */
public class ComparisonAlgorithm {
	private File all_methods;
	private File covered_methods;

	public ComparisonAlgorithm(File all_methods, File covered_methods) {
		this.all_methods = all_methods;
		this.covered_methods = covered_methods;
	}
	
	public List<Delta> getDifferences() throws IOException{
		System.out.println("Begin comparison of files: " + all_methods.getName() + " and " + covered_methods.getName());
		final List<String> allMethodsFileLines = fileToLines(all_methods);
		final List<String> coveredMethodsFileLines = fileToLines(covered_methods);
		System.out.println("Sorting files...");
		Collections.sort(allMethodsFileLines);
		Collections.sort(coveredMethodsFileLines);
		System.out.println("Diffing files...");
		final Patch patch = DiffUtils.diff(allMethodsFileLines, coveredMethodsFileLines);
		System.out.println("Ended file comparison in XX.XX seconds");
		System.out.println("=== Statistics ===");
		System.out.println("Total of methods: " + allMethodsFileLines.size());
		System.out.println("Total of methods covered: " + coveredMethodsFileLines.size());
		System.out.println("Total of methods not covered: " + patch.getDeltas().size());
		System.out.println("Percentage of coverage: " + (coveredMethodsFileLines.size()*100)/allMethodsFileLines.size() + "%");
		return patch.getDeltas();
	}
	
	private List<String> fileToLines(File file) throws IOException {
		final List<String> lines = new ArrayList<String>();
		String line;
		final BufferedReader in = new BufferedReader(new FileReader(file));
		while ((line = in.readLine()) != null) {
			lines.add(line);
		}
		in.close();
		return lines;
	}
	
	public List<Chunk> getChangesFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.CHANGE);
	}

	public List<Chunk> getInsertsFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.INSERT);
	}

	public List<Chunk> getDeletesFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.DELETE);
	}

	private List<Chunk> getChunksByType(Delta.TYPE type) throws IOException {
		final List<Chunk> listOfChanges = new ArrayList<Chunk>();
		final List<Delta> deltas = getDifferences();
		for (Delta delta : deltas) {
			if (delta.getType() == type) {
				listOfChanges.add(delta.getRevised());
			}
		}
		return listOfChanges;
	}
//
//	private List<Delta> getDeltas() throws IOException {
//		final List<String> originalFileLines = fileToLines(original);
//		final List<String> revisedFileLines = fileToLines(revised);
//		final Patch patch = DiffUtils.diff(originalFileLines, revisedFileLines);
//		return patch.getDeltas();
//	}
	
	
//	private void sortLines(List<String> linesToSort, String fileToWrite) throws IOException{
//	    Collections.sort(linesToSort);
//	    FileWriter writer = new FileWriter(fileToWrite);
//	    for(String cur: linesToSort)
//	    	writer.write(cur+"\n");
//	    writer.close();
//	}

}
