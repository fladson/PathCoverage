import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

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
	private HashSet<String> all_methods_master;
	private File covered_methods_master;
	private HashSet<String> all_methods_updated;
	private File covered_methods_updated;

	public ComparisonAlgorithm(HashSet<String> all_methods_master, File covered_methods_master, HashSet<String> all_methods_updated, File covered_methods_updated) {
		this.all_methods_master = all_methods_master;
		this.covered_methods_master = covered_methods_master;
		this.all_methods_updated = all_methods_updated;
		this.covered_methods_updated = covered_methods_updated;
	}

	public Collection<String> runComparison() throws IOException {
		System.out.println("Comparando as versões");
		System.out.println("Comparando métodos de " + covered_methods_master.getName());
		
		// convertendo de volta para uma lista para ordenação
		//final List<String> allMethodsList = new ArrayList<String>();
		//allMethodsList.addAll(all_methods);
		//this.all_methods.clear();
		
		//Lendo arquivo da ferramenta de Felipe para uma lista de strings
		final List<String> list_covered_methods_master = fileToLines(covered_methods_master);
		//Fazendo a diferença entre as listas
		Collection<String> list_not_covered_methods_master = CollectionUtils.subtract(all_methods_master, list_covered_methods_master);
		
		System.out.println("Comparando métodos de " + covered_methods_updated.getName());
		//Lendo arquivo da ferramenta de Felipe para uma lista de strings
		final List<String> list_covered_methods_updated = fileToLines(covered_methods_updated);
		//Fazendo a diferença entre as listas
		Collection<String> list_not_covered_methods_updated = CollectionUtils.subtract(all_methods_updated, list_covered_methods_updated);
				
		
		
		
//		Collection<String> similar = new HashSet<String>( all_methods );
//        Collection<String> different = new HashSet<String>();
//        
//        different.addAll( all_methods );
//        different.addAll( coveredMethodsList );
//
//        similar.retainAll( coveredMethodsList );
//        different.removeAll( similar );
        
		//System.out.println("Sorting files...");
		//Collections.sort(allMethodsList);
		//Collections.sort(coveredMethodsFileLines);
		
		System.out.println("Diffing files...");

		// Escrevendo em um arquivo os métodos não cobertos (apenas para checagem)
		FileWriter out = null;
		try {
			out = new FileWriter("allMethods.txt");
			for (String string : all_methods_master) {
				out.write(string + "\r\n");
			}
		} finally {
			out.close();
		}

		System.out.println("Ended methods comparison");
		System.out.println("===== Statistics =====");
		System.out.println("Total of methods: " + all_methods_master.size());
		System.out.println("Total of methods covered: "
				+ list_covered_methods_master.size());
		System.out.println("Total of methods not covered: "
				+ list_not_covered_methods_master.size());
		System.out.println("Percentage of coverage: "
				+ (list_covered_methods_master.size() * 100f)
				/ all_methods_master.size() + "%");
		System.out.println("Algum método no conjunto de cobertos está incluso no conjunto de não cobertos? " + CollectionUtils.containsAny(list_covered_methods_master, list_not_covered_methods_master));
		System.out.println("Algum método no conjunto de não cobertos está incluso no conjunto de cobertos? " + CollectionUtils.containsAny(list_not_covered_methods_master, list_covered_methods_master));
		Collection<String> prova = CollectionUtils.subtract(all_methods_master, list_not_covered_methods_master);
		for (String string : prova) {
			System.out.println(string);
		}
		return list_not_covered_methods_master;
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

	// public List<Chunk> getChangesFromOriginal() throws IOException {
	// return getChunksByType(Delta.TYPE.CHANGE);
	// }
	//
	// public List<Chunk> getInsertsFromOriginal() throws IOException {
	// return getChunksByType(Delta.TYPE.INSERT);
	// }
	//
	// public List<Chunk> getDeletesFromOriginal() throws IOException {
	// return getChunksByType(Delta.TYPE.DELETE);
	// }
	//
	// private List<Chunk> getChunksByType(Delta.TYPE type) throws IOException {
	// final List<Chunk> listOfChanges = new ArrayList<Chunk>();
	// final List<Delta> deltas = getDeltas();
	// for (Delta delta : deltas) {
	// if (delta.getType() == type) {
	// listOfChanges.add(delta.getRevised());
	// }
	// }
	// return listOfChanges;
	// }
	// //
	// private List<Delta> getDeltas() throws IOException {
	// final List<String> originalFileLines = fileToLines(original);
	// final List<String> revisedFileLines = fileToLines(revised);
	// final Patch patch = DiffUtils.diff(originalFileLines, revisedFileLines);
	// return patch.getDeltas();
	// }
	//
	//
	// private void sortLines(List<String> linesToSort, String fileToWrite)
	// throws IOException{
	// Collections.sort(linesToSort);
	// FileWriter writer = new FileWriter(fileToWrite);
	// for(String cur: linesToSort)
	// writer.write(cur+"\n");
	// writer.close();
	// }

}
