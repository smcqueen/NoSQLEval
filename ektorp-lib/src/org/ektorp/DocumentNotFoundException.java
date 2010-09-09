package org.ektorp;
/**
 * 
 * @author Henrik Lundgren
 * created 7 nov 2009
 *
 */
public class DocumentNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4130993962797968754L;
	
	private final String path;

	public DocumentNotFoundException(String path) {
		super(String.format("nothing found on db path: %s", path));
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
