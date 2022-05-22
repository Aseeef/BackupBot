package dev.aseef.database.component;

import java.sql.Connection;

/**
 * An interface that represents a database (and it's credentials).
 * 
 * @author sbahr
 */
public interface Database {

	/**
	 * Get the name for the database
	 * 
	 * @return The database name
	 */
	String getName();

	/**
	 * Get the connection for the database.
	 * 
	 * @return The connection for the database.
	 */
	Connection getConnection();

}
