/**
 * 
 */
package org.ektorp;

import static java.lang.String.*;

import java.io.*;
import java.net.*;

import org.ektorp.util.*;

public class DbPath {
	
	private final static String UTF_8 = "UTF-8";
	private final static String DB_NAME_PATTERN = "^[\\w+-_\\$()]+(/[\\w+-_\\$()]+)*$";
	private final static String SPECIAL_DOC_PREFIX = "_";
	
	private final String dbName;
	private final String path;
	private final String allDocs;

	public DbPath(String s) {
		Assert.notNull(s);
		checkDbName(s);
		
		int start = s.startsWith("/") ? 1 : 0;
		int end = s.endsWith("/") ? s.length() -1 : s.length();
		dbName = s.substring(start, end).toLowerCase();
		path = "/" + DbPath.escape(dbName) + "/";
		
		allDocs = path + "_all_docs";
	}
	/**
	 * Appends the string to the dbPath.
	 * The String will be escaped, i.e. dbPath.append("/foo/bar") will become "path/foo%2Fbar"
	 * @param s
	 * @return
	 */
	public String append(String s) {
		return path + escape(s);
	}
	
	public String getPath() {
		return path;
	}
	
	private static String escape(String s) {
		// don't escape design doc ids
		if (s.startsWith(SPECIAL_DOC_PREFIX)) return s;
		try {
			return URLEncoder.encode(s, UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw Exceptions.propagate(e);
		}
	}
	
	public static DbPath fromString(String s) {
		return new DbPath(s);
	}
	
	private void checkDbName(String path) {
		Assert.isTrue(path.matches(DB_NAME_PATTERN), format("Invalid database name: %s", path));
	}

	public String getAllDocsPath() {
		return allDocs;
	}
	
	public String getDbName() {
		return dbName;
	}
}