package sql;

import java.util.*;

/**
 * A generic database handler that acts as a singleton so we can reference it
 * anywhere.
 * 
 * @author sbahr
 */
public class BaseDatabase extends DatabaseHandler {

	private static List<BaseDatabase> databases = new ArrayList<>();
	private String dbName;

	/**
	 * Private constructor as singleton's cannot be instantiated.
	 */
	private BaseDatabase(String dbName, int poolSize) {
		super(poolSize);
		this.dbName = dbName;
		// Note: DatabaseHandler doesn't have a constructor for a reason
	}

	/**
	 * @return - Returns base database connection
	 */
	public static BaseDatabase getInstance(String dbName) {
		Optional<BaseDatabase> optionalDatabase = databases.stream().filter(database -> database.dbName.equalsIgnoreCase(dbName)).findFirst();
		if (!optionalDatabase.isPresent()) {
			BaseDatabase database = new BaseDatabase(dbName, 1);
			databases.add(database);
			return database;
		}
		return optionalDatabase.get();
	}

	public static List<BaseDatabase> getInstances() {
		return databases;
	}

	/**
	 * Initialize the database handler given the credentials.
	 */
	public void init() {
		this.init(this.dbName);
	}

}