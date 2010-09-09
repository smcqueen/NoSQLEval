package org.ektorp;


/**
 * 
 * @author henrik lundgren
 *
 */
public class DocumentExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DocumentExistsException(String id) {
		super(String.format("A document with id %s already exists", id));
	}
}
