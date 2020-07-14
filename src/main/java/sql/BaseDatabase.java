package sql;

import javax.swing.text.html.Option;
import java.util.*;

/**
 * A generic database handler that acts as a singleton so we can reference it
 * anywhere.
 * 
 * @author sbahr
 */
public class BaseDatabase extends DatabaseHandler {

	private static List<BaseDatabase> writeInstances = new ArrayList<>();
	private static List<BaseDatabase> readInstances = new ArrayList<>();
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
	 * Get an instances of base database for database reading. The read database contains only
	 * a single pool since concurrent writing is not possible with SQL lite.
	 *
	 * @return - Returns base database - its connection should only be used for writing data!
	 */
	public static BaseDatabase getWriteInstance(String dbName) {
		Optional<BaseDatabase> optionalDatabase = writeInstances.stream().filter(database -> database.dbName.equalsIgnoreCase(dbName)).findFirst();
		if (!optionalDatabase.isPresent()) {
			BaseDatabase database = new BaseDatabase(dbName, 1);
			writeInstances.add(database);
			return database;
		}
		return optionalDatabase.get();
	}

	/**
	 * Get an instances of base database for database reading. The read database contains multiple
	 * pools since concurrent reading is possible with SQL lite.
	 *
	 * @return - Returns base database - its connection should only be used for reading data!
	 */
	public static BaseDatabase getReadInstance(String dbName) {
		Optional<BaseDatabase> optionalDatabase = readInstances.stream().filter(database -> database.dbName.equalsIgnoreCase(dbName)).findFirst();
		if (!optionalDatabase.isPresent()) {
			BaseDatabase database = new BaseDatabase(dbName, 50);
			readInstances.add(database);
			return database;
		}
		return optionalDatabase.get();
	}

	public static List<BaseDatabase> getAllInstances() {
		List<BaseDatabase> databases = new ArrayList<>();
		databases.addAll(writeInstances);
		databases.addAll(readInstances);
		return databases;
	}

	/**
	 * Initialize the database handler given the credentials.
	 */
	public void init() {
		this.init(this.dbName);
	}

}