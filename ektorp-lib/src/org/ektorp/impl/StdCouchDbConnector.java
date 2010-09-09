package org.ektorp.impl;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.SerializationConfig.*;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.util.*;
import org.slf4j.*;

/**
 * 
 * @author henrik lundgren
 *
 */
public class StdCouchDbConnector implements CouchDbConnector {
	
	private static final int DOCUMENT_NOT_FOUND_RESPONSE = 404;
	private static final int UPDATE_CONFLICT_RESPONSE = 409;
	private static final Logger LOG = LoggerFactory.getLogger(StdCouchDbConnector.class);
	
	private final JsonFactory jsonFactory;
	private final ObjectMapper objectMapper;
	private final JsonSerializer jsonSerializer;
	
	private DbPath dbPath;

	private final RestTemplate restTemplate;
	
	private final CouchDbInstance dbInstance;
	
	private RevisionResponseHandler revisionHandler;
	
	public StdCouchDbConnector(String databaseName, CouchDbInstance dbInstance) {
		this(databaseName, dbInstance, new ObjectMapper());
		objectMapper.configure(Feature.WRITE_DATES_AS_TIMESTAMPS, false);
	}
	
	public StdCouchDbConnector(String databaseName, CouchDbInstance dbi, ObjectMapper om) {
		Assert.hasText(databaseName, "DatabaseName cannot be empty");
		Assert.notNull(dbi, "CouchDbInstance cannot be null");
		Assert.hasText(databaseName, "JsonFactory cannot be null");
		
		this.dbPath = new DbPath(databaseName);
		this.dbInstance = dbi;
		
		this.jsonFactory = om.getJsonFactory();
		
		this.objectMapper = om;;
		
		
		this.jsonSerializer = new JsonSerializer(objectMapper);
		
		this.restTemplate = new RestTemplate(dbi.getConnection());
		
		this.revisionHandler = new RevisionResponseHandler(objectMapper);
		
	}
	
	@Override
	public String path() {
		return dbPath.getPath();
	}
	
	public void setDatabaseName(String s) {
		dbPath = DbPath.fromString(s);
	}
	
	public void create(final Object o) {
		Assert.notNull(o, "Document cannot be null");
		Assert.isTrue(ReflectionUtils.isNew(o), "Object must be new");
		
		ResponseCallback<Void> rspHandler = new StdResponseHandler<Void>() {
			@Override
			public Void success(HttpResponse hr) throws Exception {
				OkDocOpRsp rsp = objectMapper.readValue(hr.getContent(), OkDocOpRsp.class);
				if (ReflectionUtils.getId(o) == null)
					ReflectionUtils.setId(o, rsp.id);
				ReflectionUtils.setRevision(o, rsp.rev);
				return null;
			}
		};

		String json = jsonSerializer.toJson(o);
		String id = ReflectionUtils.getId(o);
		if (id != null) {
			if (contains(id)) {
				throw new DocumentExistsException(id);
			}
			restTemplate.put(dbPath.append(id), json, rspHandler);
		} else
			restTemplate.post(dbPath.getPath(), json, rspHandler);
	}
	
	@Override
	public void create(String id, JsonNode node) {
		assertDocIdHasValue(id);
		Assert.notNull(node, "Node cannot be null");
		restTemplate.put(dbPath.append(id), jsonSerializer.toJson(node));	
	}
	
	@Override
	public boolean contains(String id) {
		return restTemplate.head(dbPath.append(id), new ResponseCallback<Boolean>() {
			@Override
			public Boolean error(HttpResponse hr) {
				if (hr.getCode() == DOCUMENT_NOT_FOUND_RESPONSE) {
					return Boolean.FALSE;
				}
				throw new DbAccessException(hr.toString());
			}

			@Override
			public Boolean success(HttpResponse hr) throws Exception {
				return Boolean.TRUE;
			}
			
			
		}).booleanValue();
	}
	
	@Override
	public String createAttachment(String docId, Attachment a) {
		return createAttachment(docId, null, a);
	}
	
	@Override
	public String createAttachment(String docId, String revision, Attachment a) {
		assertDocIdHasValue(docId);
		String path = dbPath.append(docId) + "/" + a.getId();
		if (revision != null) {
			path += "?rev=" + revision;
		}
		return restTemplate.put(path, a.getData(), a.getContentType(), a.getLength(), revisionHandler);
	}
	
	@Override
	public Attachment getAttachment(final String id, final String attachmentId) {
		assertDocIdHasValue(id);
		Assert.hasText(attachmentId, "attachmentId must have a value");
		if (LOG.isTraceEnabled()) {
			LOG.trace("fetching attachment for doc: {} attachmentId: {}", id, attachmentId);
		}
		HttpResponse r = restTemplate.get(dbPath.append(id) + "/" + attachmentId);
		return r != null ? new Attachment(attachmentId, r.getContent(), r.getContentType(), r.getContentLength()) : null;
	}
	
	@Override
	public String delete(Object o) {
		Assert.notNull(o, "document cannot be null");
		String rev = ReflectionUtils.getRevision(o);
		Object d = get(o.getClass(), rev);
		if (d != null) {
			return delete(ReflectionUtils.getId(o), rev);
		}
		return null;
	}

	@Override
	public <T> T get(final Class<T> c, String id) {
		Assert.notNull(c, "Class cannot be null");
		assertDocIdHasValue(id);
		return restTemplate.get(dbPath.append(id), new StdResponseHandler<T>() {
			@Override
			public T success(HttpResponse hr) throws Exception  {
				return objectMapper.readValue(hr.getContent(), c);
			}
		});
	}
	
	@Override
	public List<Revision> getRevisions(String id) {
		assertDocIdHasValue(id);
		return restTemplate.get(dbPath.append(id) + "?revs_info=true", new StdResponseHandler<List<Revision>>(){
			@Override
			public List<Revision> success(HttpResponse hr) throws Exception {
				JsonNode root = objectMapper.readValue(hr.getContent(), JsonNode.class);
				List<Revision> revs = new ArrayList<Revision>();
				for (Iterator<JsonNode> i = root.get("_revs_info").getElements(); i.hasNext();) {
					JsonNode rev = i.next();
					revs.add(new Revision(rev.get("rev").getTextValue(), rev.get("status").getTextValue()));
				}
				return revs;
			}
			@Override
			public List<Revision> error(HttpResponse hr) {
				if (hr.getCode() == DOCUMENT_NOT_FOUND_RESPONSE) {
					return Collections.emptyList();	
				}
				return super.error(hr);
			}
		});
	}

	@Override
	public InputStream getAsStream(String id) {
		assertDocIdHasValue(id);
		HttpResponse r = restTemplate.get(dbPath.append(id));
		return r != null ? r.getContent() : null;
	}
	
	@Override
	public void update(final Object o) {
		Assert.notNull(o, "Document cannot be null");
		final String id = ReflectionUtils.getId(o);
		assertDocIdHasValue(id);
		restTemplate.put(dbPath.append(id), jsonSerializer.toJson(o), new StdResponseHandler<Void>(){
			@Override
			public Void success(HttpResponse hr) throws Exception {
				JsonNode n = objectMapper.readValue(hr.getContent(), JsonNode.class);
				ReflectionUtils.setRevision(o, n.get("rev").getTextValue());
				return null;
			}

			@Override
			public Void error(HttpResponse hr) {
				if (hr.getCode() == UPDATE_CONFLICT_RESPONSE) {
					throw new UpdateConflictException(id, ReflectionUtils.getId(o));
				}
				return super.error(hr);
			}
		});
	}

	@Override
	public String delete(String id, String revision) {
		assertDocIdHasValue(id);
		return restTemplate.delete(dbPath.append(id) + "?rev=" + revision, revisionHandler);
	}
	
	@SuppressWarnings("unused")
	private static class OkDocOpRsp {
		
		public boolean ok;
		public String id;
		public String rev;
		
		
		public void setOk(boolean ok) {
			this.ok = ok;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public void setRev(String rev) {
			this.rev = rev;
		}
		
	}

	@Override
	public List<String> getAllDocIds() {
		return restTemplate.get(dbPath.getAllDocsPath(), new StdResponseHandler<List<String>>(){
			@Override
			public List<String> success(HttpResponse hr) throws Exception {
				JsonParser jp = jsonFactory.createJsonParser(hr.getContent());
				// Sanity check: verify that we got "Json Object":
				if (jp.nextToken() != JsonToken.START_OBJECT) {
					throw new RuntimeException("Expected data to start with an Object");
				}
				boolean inRow = false;
				List<String> result = null;
				
				while (jp.nextToken() != null) {
					switch (jp.getCurrentToken()) {
						case START_ARRAY:
							inRow = true;
							break;
						case END_ARRAY:
							inRow = false;
							break;
						case FIELD_NAME:
							String n = jp.getCurrentName(); 
							if (inRow) {
								if ("id".equals(n)) {
									jp.nextToken();
									result.add(jp.getText());	
								}
							} else if ("total_rows".equals(n)) {
								jp.nextToken();
								result = new ArrayList<String>(jp.getIntValue());
							}
							break;
					}
				}
				return result;
			}
		});
	}

	@Override
	public void createDatabaseIfNotExists() {
		if (!dbInstance.getAllDatabases().contains(dbPath.getDbName())) {
			dbInstance.createDatabase(dbPath);
		}
	}

	@Override
	public String getDatabaseName() {
		return dbPath.getDbName();
	}

	@Override
	public <T> List<T> queryView(ViewQuery query, final Class<T> type) {
		Assert.notNull(query, "query cannot be null");
		query.dbPath(dbPath.getPath());
		return restTemplate.get(query.buildQuery(), new StdResponseHandler<List<T>>(){
			@Override
			public List<T> success(HttpResponse hr) throws Exception {
				// TODO: reading view result should be done through the Jackson streaming API
				JsonNode root = objectMapper.readValue(hr.getContent(), JsonNode.class);
				int totalRows = root.get("total_rows").getIntValue();
				List<T> result = new ArrayList<T>(totalRows);
				if (totalRows > 0) {
					JsonNode rows = root.get("rows");
	                for (JsonNode row : (Iterable<JsonNode>) rows) {
	                	String id = row.get("value").getTextValue();
	                	if (id == null || id.isEmpty()) {
	                		throw new DbAccessException("view result value field did not contain a document id");
	                	}
	                    result.add(get(type, id));
	                }
				}
				return result;
			}
		});
	}

	@Override
	public ViewResult queryView(ViewQuery query) {
		Assert.notNull(query, "query cannot be null");
		query.dbPath(dbPath.getPath());
		return restTemplate.get(query.buildQuery(), new StdResponseHandler<ViewResult>(){
			@Override
			public ViewResult success(HttpResponse hr) throws Exception {
				return objectMapper.readValue(hr.getContent(), ViewResult.class);
			}
		});
	}
	
	@Override
	public InputStream queryForStream(ViewQuery query) {
		Assert.notNull(query, "query cannot be null");
		query.dbPath(dbPath.getPath());
		return restTemplate.get(query.buildQuery()).getContent();
	}
	
	@Override
	public String deleteAttachment(String docId, String revision,
			String attachmentId) {
		return restTemplate.delete(dbPath.append(docId) + "/" + attachmentId + "?rev=" + revision, 
				revisionHandler);
	}
	
	private void assertDocIdHasValue(String docId) {
		Assert.hasText(docId, "document id cannot be empty");
	}
}
