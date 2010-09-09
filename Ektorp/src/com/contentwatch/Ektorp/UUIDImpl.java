package com.contentwatch.Ektorp;

import java.io.IOException;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.RestTemplate;

public class UUIDImpl {

	public static Set<String> getUuids(HttpClient httpClient, int n) throws JsonParseException, JsonMappingException, IOException {
		RestTemplate template = new RestTemplate(httpClient);
		HttpResponse rsp = template.get("/_uuids?count=" + n);
    	ObjectMapper objectMapper = new ObjectMapper();
    	UUID uuid = objectMapper.readValue(rsp.getContent(), UUID.class);
    	return uuid.getUuids();
	}

}
