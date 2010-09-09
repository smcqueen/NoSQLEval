package org.ektorp.impl;

import static java.lang.String.*;

import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.type.*;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.util.*;
import org.slf4j.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public class StdCouchDbInstance implements CouchDbInstance {

	private final static Logger LOG = LoggerFactory.getLogger(StdCouchDbInstance.class);
	private final static TypeReference<List<String>> STRING_LIST_TYPE_DEF = new TypeReference<List<String>>() {};
	
	private final HttpClient client;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	
	public StdCouchDbInstance(HttpClient client) {
		this(client, new JsonFactory());
	}
	
	public StdCouchDbInstance(HttpClient client, JsonFactory jf) {
		Assert.notNull(client, "HttpClient cannot be null");
		Assert.notNull(jf, "JsonFactory cannot be null");
		this.client = client;
		this.restTemplate = new RestTemplate(client);
		this.objectMapper = new ObjectMapper(jf);
	}
	
	@Override
	public void createDatabase(String path) {
		createDatabase(DbPath.fromString(path));
	}
	
	@Override
	public void createDatabase(DbPath db) {
		List<String> all = getAllDatabases();
		if (all.contains(db.getDbName())) {
			throw new RuntimeException(format("A database with path %s already exists", db.getPath()));
		}
		LOG.debug("creating db path: {}", db.getPath());
		restTemplate.put(db.getPath());
	}

	@Override
	public void deleteDatabase(String path) {
		Assert.notNull(path);
		restTemplate.delete(DbPath.fromString(path).getPath());
	}

	@Override
	public List<String> getAllDatabases() {
		return restTemplate.get("/_all_dbs", new StdResponseHandler<List<String>>(){
			@Override
			public List<String> success(HttpResponse hr) throws Exception {
				return objectMapper.readValue(hr.getContent(), STRING_LIST_TYPE_DEF);
			}
		});
	}

	@Override
	public HttpClient getConnection() {
		return client;
	}
}
