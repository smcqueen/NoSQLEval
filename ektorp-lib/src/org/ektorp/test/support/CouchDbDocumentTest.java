package org.ektorp.test.support;

import static org.junit.Assert.*;

import org.codehaus.jackson.map.*;
import org.ektorp.support.CouchDbDocument;
import org.junit.*;

public class CouchDbDocumentTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSetId() throws Exception {
		String json = "{\"field\":\"nisse\",\"_id\":\"some_id\",\"_rev\":\"123D123\",\"_attachments\":{\"name\":{\"stub\":true,\"content_type\":\"text/plain\",\"length\":29}}}";
		ObjectMapper map = new ObjectMapper();
		TestDoc td = map.readValue(json, TestDoc.class);
		assertNotNull(td);
	}
	
	public static class TestDoc extends CouchDbDocument {
		
		private static final long serialVersionUID = 1L;
		private String field;

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}
		
	}

}
