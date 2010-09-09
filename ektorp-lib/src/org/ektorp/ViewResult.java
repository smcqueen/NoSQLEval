package org.ektorp;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.annotate.*;

/**
 * 
 * @author henrik lundgren
 *
 */
public class ViewResult implements Serializable {

	private static final long serialVersionUID = 8028295884876096791L;
	private int size;
	private int offset;
	private List<Row> rows;
	
	@JsonCreator
	public ViewResult(@JsonProperty("total_rows") int size,
			@JsonProperty("offset") int offset,
			@JsonProperty("rows") List<Row> rows) {
		this.rows = rows;
		this.offset = offset;
		this.size = size;
	}
	
	public List<Row> getRows() {
		return rows;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public static class Row implements Serializable {
		
		private static final long serialVersionUID = 76378730604602724L;
		private final String id;
		private final String key;
		private final String value;
		
		@JsonCreator
		public Row(@JsonProperty("id") String id,
				@JsonProperty("key") String key,
				@JsonProperty("value") String value) {
			this.id = id;
			this.key = key;
			this.value = value;
		}
		
		public String getId() {
			return id;
		}
		
		public String getKey() {
			return key;
		}
		
		public String getValue() {
			return value;
		}
		
		public int getValueAsInt() {
			return Integer.parseInt(value);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o instanceof Row) {
				Row r = (Row)o;
				return id.equals(r.id) && key.equals(r.key) && value.equals(r.value);
			}
			return super.equals(o);
		}
		
		@Override
		public int hashCode() {
			return (id + key + value).hashCode();
		}
	}
	
}
