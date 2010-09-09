package org.ektorp;

import java.io.*;

import org.codehaus.jackson.annotate.*;
import org.ektorp.util.*;

/**
 * 
 * @author henrik lundgren
 *
 */
@JsonWriteNullProperties(false)
public class Attachment implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String contentType;
	private int length;
	private transient InputStream data;
	private String dataBase64;
	private boolean stub;
	private int revpos;
	/**
	 * 
	 * @param id
	 * @param data
	 * @param contentType
	 * @param contentLength
	 */
	public Attachment(String id, InputStream data, String contentType, int contentLength) {
		Assert.hasText(id, "attachmentId must have a value");
		Assert.hasText(contentType, "contentType must have a value");
		Assert.notNull(data, "data input stream cannot be null");
		this.id = id;
		this.data = data;
		this.contentType = contentType;
		this.length = contentLength;
	}
	/**
	 * Constructor that takes data as String.
	 * The data must be base64 encoded single line of characters, so pre-process your data to remove any carriage returns and newlines
	 * 
	 * Useful if you want to save the attachment as an inline attachent.
	 * 
	 * @param id
	 * @param data base64-encoded
	 * @param contentType
	 * @param contentLength
	 */
	public Attachment(String id, String data, String contentType) {
		Assert.hasText(id, "attachmentId must have a value");
		Assert.hasText(contentType, "contentType must have a value");
		Assert.notNull(data, "data input stream cannot be null");
		this.id = id;
		this.contentType = contentType;
		this.dataBase64 = data;
		this.length = data.getBytes().length;
	}
	
	Attachment() {}

	@JsonProperty("content_type")
	public String getContentType() {
		return contentType;
	}

	@JsonProperty("content_type")
	void setContentType(String contentType) {
		this.contentType = contentType;
	}
	@JsonIgnore
	public int getLength() {
		return length;
	}
	/**
	 * Only populated if this attachment was created with data as String constructor.
	 * @return
	 */
	@JsonProperty("data")
	public String getDataBase64() {
		return dataBase64;
	}
	
	@JsonIgnore
	public InputStream getData() {
		return data;
	}

	@JsonIgnore
	public String getId() {
		return id;
	}

	@JsonIgnore
	void setId(String id) {
		this.id = id;
	}

	void setLength(int contentLength) {
		this.length = contentLength;
	}

	public boolean isStub() {
		return stub;
	}

	void setStub(boolean stub) {
		this.stub = stub;
	}
	
	public int getRevpos() {
		return revpos;
	}
	
	public void setRevpos(int revpos) {
		this.revpos = revpos;
	}
	
}
