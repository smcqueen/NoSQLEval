package org.ektorp.test.support;

import static org.junit.Assert.*;

import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.support.DesignDocument;
import org.junit.*;


public class DesignDocumentTest {

	@Test
	public void should_deserialize_from_design_doc_json_from_db() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();
		DesignDocument dd = om.readValue(getClass().getResourceAsStream("design_doc.json"), DesignDocument.class);
		assertEquals("_design/TestDoc", dd.getId());
		assertTrue(dd.containsView("all"));
		assertTrue(dd.containsView("by_lastname"));
		assertEquals("javascript", dd.getField("language"));
	}
	
}
