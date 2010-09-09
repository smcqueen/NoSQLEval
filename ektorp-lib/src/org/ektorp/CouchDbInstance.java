package org.ektorp;

import java.util.*;

import org.ektorp.http.*;


/**
 * 
 * @author henrik lundgren
 *
 */
public interface CouchDbInstance {
	/**
	 * 
	 * @return the names of all databases residing in this instance.
	 */
	List<String> getAllDatabases();
	
	void createDatabase(DbPath path);
	void createDatabase(String path);
	
	void deleteDatabase(String path);
	
	HttpClient getConnection();
}
