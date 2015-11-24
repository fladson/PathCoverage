package br.ufrn.ppgsc.fc.util;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import br.ufrn.ppgsc.fc.database.PostgreSQLJDBC;
import br.ufrn.ppgsc.fc.model.Node;

public class CoveredMethods {
	
	public static void main(String[] args) {
		PostgreSQLJDBC pg = new PostgreSQLJDBC();
		try {
			List<Node> list = pg.getAllCoveredMethods();
			PrintStream ps = new PrintStream("coveredPaths2.txt");
			System.out.println("Writing to file: " + list.size() + " nodes...");
			for(Node node:list){
				ps.println(node.getMethod_signature());
//				System.out.println(node.getMethod_signature());
			}
			System.out.println("Done!");
			ps.close();
			list.clear();
		} catch (SQLException | FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

}
