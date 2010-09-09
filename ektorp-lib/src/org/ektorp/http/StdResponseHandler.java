package org.ektorp.http;

import org.ektorp.*;

/**
 * 
 * @author henrik lundgren
 *
 * @param <T>
 */
public class StdResponseHandler<T> implements ResponseCallback<T> {

	@Override
	public T error(HttpResponse hr) {
		if (hr.getCode() == 404) {
			throw new DocumentNotFoundException(hr.getRequestURI());
		}
		throw new DbAccessException(hr.toString() + ", URI: " + hr.getRequestURI());
	}
	
	@Override
	public T success(HttpResponse hr) throws Exception {
		return null;
	}

}
