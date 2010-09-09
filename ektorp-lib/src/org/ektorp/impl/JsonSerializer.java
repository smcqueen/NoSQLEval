package org.ektorp.impl;

import org.codehaus.jackson.map.*;
import org.ektorp.util.*;
import org.slf4j.*;
/**
 * 
 * @author Henrik Lundgren
 * created 1 nov 2009
 *
 */
public class JsonSerializer {
	
	private final Logger LOG = LoggerFactory.getLogger(JsonSerializer.class);
	private final ObjectMapper objectMapper;
	
	public JsonSerializer(ObjectMapper om) {
		objectMapper = om;
	}
	
	public String toJson(Object o) {
		try {
			if (LOG.isDebugEnabled()) {
				String json = objectMapper.writeValueAsString(o);
				LOG.debug(json);
				return json;
			}
			return objectMapper.writeValueAsString(o);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}
}
