package org.ektorp.support;

import java.util.*;

import org.codehaus.jackson.annotate.*;

/**
 * Representation of a CouchDb design document.
 * 
 * Design documents can contain fields currently not handled by Ektorp, such as update handlers and validators.
 * These fields are store in the unknownFields map and are accessible by the method getField(String key)
 * 
 * However, if write such a document back to the database, these fields will be lost as they wont get serialized by Jackson.  
 * Unknown fields will be preserved if document is saved as a map instead. the method asMap() will produce a map representation.
 * <code>
 * db.update(designDoc.asMap());
 * </code>
 * @author henrik lundgren
 *
 */
public class DesignDocument extends CouchDbDocument {

private static final long serialVersionUID = 727813829995624926L;
	
	public static String ID_PREFIX = "_design/";
	
	private Map<String, View> views;
	private Map<String, Object> unknownFields;
	
	public DesignDocument() {}
	
	public DesignDocument(String id) {
		setId(id);
	}
	
	public Map<String, View> getViews() {
		return Collections.unmodifiableMap(views());
	}
	
	private Map<String, View> views() {
		if (views == null) {
			views = new HashMap<String, View>();
		}
		return views;
	}
	
	@JsonProperty
	void setViews(Map<String, View> views) {
		this.views = views;
	}
	
	public boolean containsView(String name) {
		return views().containsKey(name);
	}
	
	public View get(String viewName) {
		return views().get(viewName);
	}
	
	public void addView(String name, View v) {
		views().put(name, v);
	}
	/**
	 * As design documents can contain a lot of fields currently not handled by Ektorp, a generic setter for these fields is required.
	 * Used for unknown properties.
	 * 
	 * @param key
	 * @param value
	 */
	@JsonAnySetter
	public void setUnknown(String key, Object value) {
		unknownFields().put(key, value);
	}
	
	private Map<String, Object> unknownFields() {
		if (unknownFields == null) {
			unknownFields = new HashMap<String, Object>();
		}
		return unknownFields;
	}
	/**
	 * Makes a Map representation of this document.
	 * Suitable to use when saving to the database, as any unknown fields otherwise will be lost.
	 * @return
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> m = unknownFields();
		m.put("_id", getId());
		if (getRevision() != null) {
			m.put("_rev", getRevision());
		}
		m.put("views", getViews());
		return m;
	}
	
	@JsonIgnore
	public Object getField(String key) {
		return unknownFields().get(key);
	}
	
	/**
	 * Definition of a view in a design document.
	 * @author henrik lundgren
	 *
	 */
	@JsonWriteNullProperties(false)
	public static class View {
		@JsonProperty
		private String map;
		@JsonProperty
		private String reduce;
		
		public View() {}
		
		public static View of(org.ektorp.support.View v) {
			return v.reduce().isEmpty() ?
					new DesignDocument.View(v.map()) :
					new DesignDocument.View(v.map(), v.reduce());
		}
		
		public View(String map) {
			this.map = map;
		}
		
		public View(String map, String reduce) {
			this(map);
			this.reduce = reduce;
		}		
		
		public String getMap() {
			return map;
		}
		
		public void setMap(String map) {
			this.map = map;
		}
		
		public String getReduce() {
			return reduce;
		}
		
		public void setReduce(String reduce) {
			this.reduce = reduce;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((map == null) ? 0 : map.hashCode());
			result = prime * result
					+ ((reduce == null) ? 0 : reduce.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			View other = (View) obj;
			if (map == null) {
				if (other.map != null)
					return false;
			} else if (!map.equals(other.map))
				return false;
			if (reduce == null) {
				if (other.reduce != null)
					return false;
			} else if (!reduce.equals(other.reduce))
				return false;
			return true;
		}
		
	}
}
