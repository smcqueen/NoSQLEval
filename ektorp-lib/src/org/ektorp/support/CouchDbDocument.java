package org.ektorp.support;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.annotate.*;
import org.ektorp.*;
import org.ektorp.util.*;

/**
 * 
 * @author henrik lundgren
 *
 */
@JsonWriteNullProperties(value = false)
public class CouchDbDocument implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String revision;
	private Map<String, Attachment> attachments;
	
	@JsonProperty("_id")
	public String getId() {
		return id;
	}
	
	@JsonProperty("_id")
	public void setId(String s) {
		Assert.hasText(s, "id must have a value");
		if (id != null) {
			throw new IllegalStateException("cannot set id, id already set");
		}
		id = s;
	}
	
	@JsonProperty("_rev")
	public String getRevision() {
		return revision;
	}
	@JsonProperty("_rev")
	public void setRevision(String s) {
		this.revision = s;
	}
	@JsonIgnore
	public boolean isNew() {
		return revision == null;
	}
	
	@JsonProperty("_attachments")
	public Map<String, Attachment> getAttachments() {
		return attachments;
	}
	
	@JsonProperty("_attachments")
	void setAttachments(Map<String, Attachment> attachments) {
		this.attachments = attachments;
	}

	protected void removeAttachment(String id) {
		Assert.hasText(id, "id may not be null or emtpy");
		if (attachments != null) {
			attachments.remove(id);
		}
	}
	
	protected void addInlineAttachment(Attachment a) {
		Assert.notNull(a, "attachment may not be null");
		Assert.hasText(a.getDataBase64(), "attachment must have data base64-encoded");
		if (attachments == null) {
			attachments = new HashMap<String, Attachment>();
		}
		attachments.put(a.getId(), a);
	}
	
}
