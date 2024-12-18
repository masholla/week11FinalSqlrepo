package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import projects.exception.DbException;

public class DbConnection 
{
	//Keeps connection information
	private static final String SCHEMA = "projects";
	private static final String USER = "projects";
	private static final String PASSWORD = "projects";
	private static final String HOST = "localhost";
	private static final int PORT = 3306;
	
	public static Connection getConnection()
	{
		//creates string formatted to give the command with given variables
		String url = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&useSSL=false", 
				HOST, PORT, SCHEMA, USER, PASSWORD);
		
		System.out.println("Connecting with url=" + url);
		
		
		//Tries to connect to server with url provided, throws exception if failed.
		try 
		{
			Connection conn = DriverManager.getConnection(url);
			System.out.println("Successfully obtained connection.");
			return conn;
			
		} catch (SQLException e) 
		{
			System.out.println("Unable to get connection!");
			throw new DbException(e);
		}
		
		
	}
}
