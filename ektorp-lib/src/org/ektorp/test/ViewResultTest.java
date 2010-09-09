package org.ektorp.test;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.ViewResult;
import org.junit.*;

public class ViewResultTest {

	@Test
	public void fromJson() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();
		ViewResult result = om.readValue(getClass().getResourceAsStream("impl/view_result.json"), ViewResult.class);
		assertEquals(2, result.getSize());
		assertEquals(1, result.getOffset());
		List<ViewResult.Row> rows = result.getRows(); 
		assertEquals("doc_id1", rows.get(0).getId());
		assertEquals("doc_id2", rows.get(1).getId());
	}

}
