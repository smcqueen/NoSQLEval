package org.ektorp;


/**
 * 
 * @author henrik lundgren
 *
 */
public class DbAccessException extends RuntimeException {

	private static final long serialVersionUID = -1817230646884819428L;

	public DbAccessException(String message) {
		super(message);
	}
}
