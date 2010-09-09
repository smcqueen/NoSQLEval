package org.ektorp.test.impl;

import static java.lang.String.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.*;
import org.ektorp.util.*;
import org.joda.time.*;
import org.junit.*;
import org.mockito.*;
import org.mockito.internal.verification.*;

public class StdCouchDbConnectorTest {

	private static final String TEST_DB_PATH = "/test_db/";
	StdCouchDbConnector dbCon;
	StdHttpClient httpClient;
	TestDoc td = new TestDoc();
	
	@Before
	public void setUp() throws Exception {
		httpClient = mock(StdHttpClient.class);
		dbCon = new StdCouchDbConnector("test_db/", new StdCouchDbInstance(httpClient));
		
		td.name = "nisse";
		td.age = 12;
	}

	@Test
	public void testCreate() {
		td.setId("some_id");
		setupNegativeContains(td.getId());
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
		assertEquals("some_id", td.getId());
		assertEquals("123D123", td.getRevision());
		assertEqualJson("create.json", ac.getValue());
	}
	
	@Test
	public void docId_should_be_escaped_when_id_contains_slash() throws UnsupportedEncodingException {
		String escapedId = "http%3A%2F%2Fsome%2Fopenid%3Fgoog";
		td.setId("http://some/openid?goog");
		setupNegativeContains(escapedId);
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/" + escapedId), ac.capture());
		assertEquals("http://some/openid?goog", td.getId());
	}
	
	@Test
	public void testCreateFromJsonNode() throws Exception {
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		JsonNode root = new ObjectMapper().readValue(getClass().getResourceAsStream("create_from_json_node.json"), JsonNode.class);
		dbCon.create("some_id", root);
		String facit = IOUtils.toString(getClass().getResourceAsStream("create_from_json_node.json"), "UTF-8").trim();
		verify(httpClient).put("/test_db/some_id", facit);
	}
	
	@Test
	public void save_document_with_utf8_charset() {
		td.setId("some_id");
		td.name = "Örjan Åäö";
		setupNegativeContains(td.getId());
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
		assertEqualJson("charset.json", ac.getValue());
	}

	@Test(expected=DocumentExistsException.class)
	public void give_docid_already_exists_when_creating_doc_then_exception_should_be_thrown() {
		td.setId("some_id");
		setupPositiveContains(td.getId());
		dbCon.create(td);
	}
	
	@Test
	public void create_should_post_if_id_is_missing() {		
		when(httpClient.post(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).post(eq(TEST_DB_PATH), ac.capture());
		assertEquals("some_id", td.getId());
		assertEquals("123D123", td.getRevision());
		assertEqualJson("create_with_no_id.json", ac.getValue());
	}
	
	@Test
	public void testDelete() {
		when(httpClient.delete(anyString())).thenReturn(HttpResponseStub.valueOf(200, "{\"ok\":true,\"rev\":\"123D123\"}"));
		dbCon.delete("some_id", "123D123");
		verify(httpClient).delete("/test_db/some_id?rev=123D123");
	}

	@Test
	public void testDeleteCouchDbDocument() {
		td.setId("some_id");
		td.setRevision("123D123");
		setupGetDocResponse();
		when(httpClient.delete(anyString())).thenReturn(HttpResponseStub.valueOf(200, "{\"ok\":true,\"rev\":\"123D123\"}"));
		dbCon.delete(td);
		verify(httpClient).delete("/test_db/some_id?rev=123D123");
	}
	
	@Test
	public void testGet() {
		setupGetDocResponse();
		TestDoc getted = dbCon.get(TestDoc.class, "some_id");
		verify(httpClient).get(eq("/test_db/some_id"));
		
		assertNotNull(getted);
		assertEquals("some_id", getted.getId());
		assertEquals("123D123", getted.getRevision());
		assertEquals("nisse", getted.name);
		assertEquals(12, getted.age);
	}

	private void setupGetDocResponse() {
		when(httpClient.get(anyString())).thenReturn(HttpResponseStub.valueOf(200, "{\"name\":\"nisse\",\"age\":12,\"_id\":\"some_id\",\"_rev\":\"123D123\"}"));
	}
	
	private void setupGetDocResponse(String... ids) {
		for (String id : ids) {
			when(httpClient.get(TEST_DB_PATH + id)).thenReturn(HttpResponseStub.valueOf(200, "{\"name\":\"nisse\",\"age\":12,\"_id\":\" "+ id + "\",\"_rev\":\"123D123\"}"));
		}
	}

	@Test(expected=DocumentNotFoundException.class)
	public void throw_exception_when_doc_is_missing() {
		when(httpClient.get(anyString())).thenReturn(HttpResponseStub.valueOf(404, ""));
		dbCon.get(TestDoc.class, "some_id");
	}
	
	@Test
	public void update() {
		td.setId("some_id");
		td.setRevision("123D123");
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.update(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
		assertEquals("some_id", td.getId());
		assertEquals("123D123", td.getRevision());
		assertEqualJson("update.json", ac.getValue());
	}
	
	@Test(expected=UpdateConflictException.class)
	public void throw_exception_when_in_conflict() {
		td.setId("some_id");
		td.setRevision("123D123");
		when(httpClient.put(anyString(), anyString())).thenReturn(ResponseOnFileStub.newInstance(409, "update_conflict.json"));
		dbCon.update(td);
	}
	
	@Test
	public void should_create_db_if_missing() {
		when(httpClient.get("/_all_dbs")).thenReturn(HttpResponseStub.valueOf(200, "[\"abc_bilar/inventory\", \"users\", \"accounts\"]"));
		
		dbCon.setDatabaseName("abc_bilar/inventory");
		dbCon.createDatabaseIfNotExists();
		verify(httpClient, VerificationModeFactory.times(0)).put(anyString());
	}

	@Test
	public void testSetDatabaseName() {
		dbCon.setDatabaseName("new_db/");
		td.setId("some_id");
		when(httpClient.head("/new_db/" + td.getId())).thenReturn(HttpResponseStub.valueOf(404, ""));
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/new_db/some_id"), ac.capture());
		assertEqualJson("set_dbname.json", ac.getValue());
	}
	
	@Test
	public void return_all_docIds_in_db() {
		when(httpClient.get("/test_db/_all_docs")).thenReturn(HttpResponseStub.valueOf(200, 
				"{\"total_rows\": 3, \"offset\": 0, \"rows\": [" +
				"{\"id\": \"doc1\", \"key\": \"doc1\", \"value\": {\"rev\": \"4324BB\"}}," +
				"{\"id\": \"doc2\", \"key\": \"doc2\", \"value\": {\"rev\":\"2441HF\"}}," +
				"{\"id\": \"doc3\", \"key\": \"doc3\", \"value\": {\"rev\":\"74EC24\"}}]}"));
		List<String> all = dbCon.getAllDocIds();
		assertEquals(3, all.size());
		assertEquals("doc1", all.get(0));
		assertEquals("doc2", all.get(1));
		assertEquals("doc3", all.get(2));
	}

	@Test
	public void return_all_revisions() {
		when(httpClient.get("/test_db/some_doc_id?revs_info=true"))
			.thenReturn(ResponseOnFileStub.newInstance(200, "revisions.json"));
		List<Revision> l = dbCon.getRevisions("some_doc_id");
		assertNotNull(l);
		assertEquals(8, l.size());
		assertEquals(new Revision("8-8395fd3a7a2dd04022cc1330a4d20e66","available"), l.get(0));
	}
	
	@Test
	public void return_null_revisions_when_doc_is_missing() {
		when(httpClient.get("/test_db/some_doc_id?revs_info=true"))
			.thenReturn(HttpResponseStub.valueOf(404, ""));
		List<Revision> l = dbCon.getRevisions("some_doc_id");
		assertNotNull(l);
		assertTrue(l.isEmpty());
	}
	
	@Test
	public void return_attachment_with_open_data_stream() throws Exception {
		ResponseOnFileStub rsp = ResponseOnFileStub.newInstance(200, "attachment.txt", "text", 12);
		when(httpClient.get("/test_db/some_doc_id/some_attachment")).thenReturn(rsp);
		Attachment a = dbCon.getAttachment("some_doc_id", "some_attachment");
		assertNotNull(a);
		assertFalse(rsp.isConnectionReleased());
		assertEquals("detta är ett påhäng med ett ö i", IOUtils.toString(a.getData(), "UTF-8"));
		assertEquals(rsp.getContentType(), a.getContentType());
		assertEquals(rsp.getContentLength(), a.getLength());
	}
	
	@Test
	public void read_document_as_stream() throws Exception {
		// content type is really json but it doesn't matter here.
		ResponseOnFileStub rsp = ResponseOnFileStub.newInstance(200, "attachment.txt");
		when(httpClient.get("/test_db/some_doc_id")).thenReturn(rsp);
		InputStream data = dbCon.getAsStream("some_doc_id");
		assertNotNull(data);
		assertFalse(rsp.isConnectionReleased());
		assertEquals("detta är ett påhäng med ett ö i", IOUtils.toString(data, "UTF-8"));
	}
	
	@Test
	public void should_stream_attachmed_content() {
		when(httpClient.put(anyString(), any(InputStream.class), anyString(), anyInt())).thenReturn(ResponseOnFileStub.newInstance(200, "create_attachment_rsp.json"));
		
		dbCon.createAttachment("docid", new Attachment("attachment_id",IOUtils.toInputStream("content"), "text/html", 12));
		
		verify(httpClient).put(eq("/test_db/docid/attachment_id"), any(InputStream.class), eq("text/html"), eq(12));
	}
	
	@Test
	public void load_query_result() {
		setupGetDocResponse("doc_id1", "doc_id2");
		
		ViewQuery query = new ViewQuery()
			.dbPath(TEST_DB_PATH)
			.designDocId("_design/testdoc")
			.viewName("test_view")
			.key("key_value");
		
		when(httpClient.get(query.buildQuery())).thenReturn(ResponseOnFileStub.newInstance(200, "view_result.json"));
		
		ViewResult result = dbCon.queryView(query);
		
		assertEquals(2, result.getSize());
		assertEquals("doc_id1", result.getRows().get(0).getValue());
		assertEquals("doc_id2", result.getRows().get(1).getValue());
		
	}
	
	@Test
	public void dates_should_be_serialized_in_ISO_8601_format() {
		setupNegativeContains("some_id");
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		
		DateTime dt = new DateTime(2010, 4, 25, 20, 11, 24, 555,DateTimeZone.forID("+00:00"));
		Date d = dt.toDate();
		System.out.println(d);
		DateDoc dd = new DateDoc();
		dd.setId("some_id");
		dd.setDateTime(dt);
		dd.setDate(dt.toDate());
		
		dbCon.create(dd);
		
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
		String json = ac.getValue();
		assertEqualJson("dates.json", json);
		
		when(httpClient.get("/test_db/some_id")).thenReturn(HttpResponseStub.valueOf(201, json));
		
		DateDoc deserialized = dbCon.get(DateDoc.class, dd.getId());
		assertEquals(dt, deserialized.getDateTime());
		assertEquals(d, deserialized.getDate());
		
	}
	
	@Test
	public void given_that_doc_exists_then_contains_should_return_true() {
		setupPositiveContains("some_id");
		assertTrue(dbCon.contains("some_id"));
	}
	
	private void setupPositiveContains(String id) {
		when(httpClient.head("/test_db/" + id)).thenReturn(HttpResponseStub.valueOf(200, ""));
	}
	
	private void setupNegativeContains(String id) {
		when(httpClient.head("/test_db/" + id)).thenReturn(HttpResponseStub.valueOf(404, ""));
	}
	
	@Test
	public void given_that_doc_does_not_exists_then_contains_should_return_false() {
		setupNegativeContains("some_id");
		assertFalse(dbCon.contains("some_id"));
	}
	
	@SuppressWarnings("serial")
	static class DateDoc extends CouchDbDocument {
		
		private Date date;
		private DateTime dateTime;
		
		public Date getDate() {
			return date;
		}
		
		public void setDate(Date date) {
			this.date = date;
		}
		
		public DateTime getDateTime() {
			return dateTime;
		}
		
		public void setDateTime(DateTime dateTime) {
			this.dateTime = dateTime;
		}
	}
	
	@SuppressWarnings("serial")
	static class TestDoc extends CouchDbDocument {
		private String name;
		private int age;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		
	}
	
	private void assertEqualJson(String expectedFileName, String actual) {
		String facit = getString(expectedFileName);
		assertTrue(format("expected: %s was: %s", facit, actual), JSONComparator.areEqual(facit, actual));
	}
	
	private String getString(String resourceName) {
		try {
			return IOUtils.toString(getClass().getResourceAsStream(resourceName), "UTF-8");
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}

}
