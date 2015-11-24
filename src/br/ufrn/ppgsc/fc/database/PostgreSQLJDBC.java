package br.ufrn.ppgsc.fc.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.ufrn.ppgsc.fc.model.Node;

public class PostgreSQLJDBC {

	protected Connection c = null;
	protected Statement stmt = null;

	private void connectToDatabase() {
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/jetty",
					"fladson", "opaco/");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Os roots sao os metodos de teste que iniciam a cadeia de chamadas
	 */
	public String getRootsIds() throws SQLException {
		if(c == null){
			connectToDatabase();
		}
		String rootsIds = "";
		try {
			stmt = c.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT id, member, parent_id FROM NODE where parent_id is null;");
			while (rs.next()) {
				String id = rs.getInt("id")+"";
				if(!rootsIds.contains(id)){
					rootsIds += id + ",";
				}
			}
			rs.close();
			stmt.close();
			//c.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(rootsIds.substring(rootsIds.length()-1).equals(",")){
			return rootsIds.substring(0, rootsIds.length()-1);
		}else{
			return rootsIds;
		}
	}
	
	public List<Node> getAllCoveredMethods() throws SQLException {
		if(c == null){
			connectToDatabase();
		}
		List<Node> nodes = new ArrayList<Node>();
		String rootsIds = this.getRootsIds();
		System.out.println("Root Ids: " + rootsIds);
		try {
			stmt = c.createStatement();
			// Select das raízes
			ResultSet rs = stmt
					.executeQuery("select * from node where parent_id in (" + rootsIds + ");");
			while (rs.next()) {
				nodes.add(new Node(rs.getInt("id"), rs.getString("member"), rs.getInt("parent_id"), 1));
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nodes;
	}
	
	public Node getNodeAt(int id){
		Node node = null;
		try {
			stmt = c.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT id, member, parent_id FROM NODE where id = "+ id + ";");
			while (rs.next()) {
				node = new Node(rs.getInt("id"), rs.getString("member"), rs.getInt("parent_id"),0);
			}
			rs.close();
			stmt.close();
//			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return node;
	}
	
	public Node getNodeAtParent(int id){
		Node node = null;
		try {
			stmt = c.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT id, member, parent_id FROM NODE where parent_id = "+ id + ";");
			while (rs.next()) {
				node = new Node(rs.getInt("id"), rs.getString("member"), rs.getInt("parent_id"),0);
			}
			rs.close();
			stmt.close();
//			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return node;
	}
}