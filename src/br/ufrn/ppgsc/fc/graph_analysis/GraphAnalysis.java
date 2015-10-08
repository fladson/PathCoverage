package br.ufrn.ppgsc.fc.graph_analysis;

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
public class GraphAnalysis {
	
	public static void analyseGraphsfromTextFile(String callEntriesPath, String coveredEntriesPath, String changedMethodsPath){
		
	}

}
