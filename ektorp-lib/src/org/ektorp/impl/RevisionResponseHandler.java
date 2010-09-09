package org.ektorp.impl;

import java.util.*;

import org.codehaus.jackson.map.*;
import org.ektorp.http.*;
import org.ektorp.util.*;
/**
 * Extracts the document revision if the operation was successful
 * @author henrik lundgren
 *
 */
public class RevisionResponseHandler extends StdResponseHandler<String> {

	private static final String REVISION_FIELD_NAME = "rev";
	ObjectMapper objectMapper;
	
	public RevisionResponseHandler(ObjectMapper om) {
		Assert.notNull(om, "ObjectMapper cannot be null");
		objectMapper = om;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String success(HttpResponse hr) throws Exception {
		Map<String, ?> rsp = objectMapper.readValue(hr.getContent(), Map.class);
		return (String) rsp.get(REVISION_FIELD_NAME);
	}
}
