package org.ektorp;
/**
 * 
 * @author Henrik Lundgren
 * created 18 okt 2009
 *
 */
public class UpdateConflictException extends RuntimeException {
	
	private final String docId;
	private final String revision;
	private static final long serialVersionUID = 10910358334576950L;
	
	public UpdateConflictException(String documentId, String revision) {
		docId = documentId;
		this.revision = revision;
	}

	@Override
	public String getMessage() {
		return String.format("document update conflict: id: %s rev: %s", docId, revision);
	}

}
