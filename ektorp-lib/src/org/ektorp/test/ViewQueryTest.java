package org.ektorp.test;

import static org.junit.Assert.*;

import org.ektorp.ViewQuery;
import org.junit.*;

public class ViewQueryTest {

	ViewQuery query = new ViewQuery()
							.dbPath("/somedb/")
							.designDocId("_design/doc")
							.viewName("viewname");
										
	
	@Test
	public void default_to_no_parameters() {
		assertEquals("/somedb/_design/doc/_view/viewname", query.buildQuery());
	}
	
	@Test
	public void key_parameter_added() throws Exception {
		String url = query.key("value").buildQuery();
		assertTrue(contains(url, "?key=%22value%22"));
	}
	
	@Test
	public void given_key_is_json_then_key_parameter_should_be_unchanged() throws Exception {
		String url = query.key("\"value\"").buildQuery();
		assertTrue(contains(url, "?key=%22value%22"));
	}
	
	@Test
	public void startKey_parameter_added() {
		String url = query.startKey("value").buildQuery();
		assertTrue(contains(url, "?startkey=%22value%22"));
	}
	
	@Test
	public void startKey_and_endKey_parameter_added() {
		String url = query
			.startKey("start")
			.endKey("end")
			.buildQuery();
		assertTrue(contains(url, "?startkey=%22start%22"));
		assertTrue(contains(url, "&endkey=%22end%22"));
	}
	
	@Test
	public void startDoc_parameter_added() {
		String url = query
			.startDocId("start_dic_id")
			.buildQuery();
		assertTrue(contains(url, "?startkey_docid=start"));
	}
	
	@Test
	public void include_docs_parameter_added() {
		String url = query
			.includeDocs(true)
			.buildQuery();
		assertTrue(contains(url, "?include_docs=true"));
	}
	
	@Test
	public void stale_ok_parameter_added() {
		String url = query
			.staleOk(true)
			.buildQuery();
		assertTrue(contains(url, "?stale=ok"));
	}
	
	@Test
	public void reduce_parameter_added() {
		String url = query
			.reduce(false)
			.buildQuery();
		assertTrue(contains(url, "?reduce=false"));
	}
	
	@Test(expected=java.lang.IllegalStateException.class)
	public void throw_exception_when_dbName_is_missing() {
		new ViewQuery()
//			.dbPath("/somedb/")
			.designDocId("_design/doc")
			.viewName("viewname")
			.buildQuery();
	}
	
	@Test(expected=java.lang.IllegalStateException.class)
	public void throw_exception_when_designDocId_is_missing() {
		new ViewQuery()
			.dbPath("/somedb/")
//			.designDocId("_design/doc")
			.viewName("viewname")
			.buildQuery();
	}
	
	@Test(expected=java.lang.IllegalStateException.class)
	public void throw_exception_when_viewName_is_missing() {
		new ViewQuery()
			.dbPath("/somedb/")
			.designDocId("_design/doc")
//			.viewName("viewname")
			.buildQuery();
	}

	private boolean contains(String subject, String s) {
		return subject.indexOf(s) > -1;
	}
	
}
