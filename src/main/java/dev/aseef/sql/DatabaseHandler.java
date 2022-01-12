package dev.aseef.sql;

import com.zaxxer.hikari.HikariDataSource;
import dev.aseef.sql.component.Database;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A generic database handler that holds a HikariCP data source so we can have
 * multiple database connection.
 * 
 * Note: This should be init() with either the Plugin/Config path to load
 * settings, or can be init() with just database credentials, which uses default
 * HikariCP settings.
 * 
 * @author sbahr
 */
public class DatabaseHandler implements Database {

	/** The default MySQL driver */
	private static final String SQLITE_DRIVER = "org.sqlite.SQLiteDataSource";
	/** The database name */
	private String dbName;
	/** Data source connection pool from HikariCP */
	private HikariDataSource hikariSource = new HikariDataSource();

	// NOTE: HikariCP performs best at fixed pool size, minIdle=maxConns
	// https://github.com/brettwooldridge/HikariCP

	/** How many minimum idle connections should we always have (2) */
	protected int minIdle;
	/** How many max connections should exist in pool (2) */
	protected int maxPoolSize;
	/** How long, in millis, we stop waiting for new connection (2 minutes) */
	protected int connectionTimeoutMs = 2 * 1000 * 60;
	/** How long, in millis, before connections timeout (45 secs) */
	protected int idleTimeoutMs = 45 * 1000;
	/** How long, in millis, this connection can be alive for (30 mins) */
	protected int maxLifetimeMs = 30 * 60 * 1000;
	/** How long, in millis, can a connection be gone from a pool (4 secs) */
	protected int leakDetectionThresholdMs = 4 * 1000;
	/** The ping alive query */
	protected String connectionTestQuery = "SELECT 1";

	public DatabaseHandler(int poolSize) {
		this.maxPoolSize = poolSize;
		this.minIdle = poolSize;
	}

	/**
	 * Initialize the handler with the specified database credentials.
	 * <p>
	 * Sets up the configuration for the connection pool and default settings.
	 * </p>
	 * 
	 * @param dbPath - the name for the database
	 */
	public void init(String dbPath) {
		this.dbName = dbPath;

		try {
			File dbFile = new File(this.dbName);
			dbFile.getParentFile().mkdir();
			dbFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// set the jdbc url, note the character encoding
		// https://stackoverflow.com/questions/3040597/jdbc-character-encoding
		hikariSource.setJdbcUrl("jdbc:sqlite:" + this.dbName);

		/** General conf settings for hikari */
		// works best when minIdle=maxPoolSize
		hikariSource.setMinimumIdle(minIdle);
		hikariSource.setMaximumPoolSize(maxPoolSize);

		// how long to wait, for a new connection
		hikariSource.setConnectionTimeout(connectionTimeoutMs);
		// how long before idle connection is destroyed
		hikariSource.setIdleTimeout(idleTimeoutMs);
		// how long can a connection exist
		hikariSource.setMaxLifetime(maxLifetimeMs);
		// how long connection is away from a pool before saying uh oh
		hikariSource.setLeakDetectionThreshold(leakDetectionThresholdMs);
		// dev.aseef.test query to confirm alive
		hikariSource.setConnectionTestQuery(connectionTestQuery);

		// MUST set log writer
		try {
			hikariSource.setLogWriter(new PrintWriter(System.out));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("A connection to the database " + this.getName() + " has successfully been established!");

	}

	/**
	 * Close HikariCP connection pool, and all the connections.
	 * <p>
	 * Note: This should be called whenever the plugin turns off!
	 * </p>
	 */
	public void close() {
		if (hikariSource != null && !hikariSource.isClosed()) {
			hikariSource.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return dbName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Connection getConnection() {
		if (hikariSource != null) {
			try {
				return hikariSource.getConnection();
			}
			catch (Exception e) {
				System.out.println("[DatabaseHandler] Unable to grab a connection from the connection pool!");
				e.printStackTrace();
			}
		}

		return null;
	}
}
