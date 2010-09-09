package org.ektorp.http;

import java.io.*;

import org.ektorp.util.*;
/**
 * 
 * @author Henrik Lundgren
 * 
 */
public class RestTemplate {

	private final HttpClient client;

	public RestTemplate(HttpClient client) {
		this.client = client;
	}

	public <T> T get(String path, ResponseCallback<T> callback) {
		HttpResponse hr = client.get(path);
		return handleResponse(callback, hr);
	}

	public HttpResponse get(String path) {
		HttpResponse hr = client.get(path);
		try {
			return hr.isSuccessful() ? hr : null;
		} catch (Exception e) {
			hr.releaseConnection();
			throw Exceptions.propagate(e);
		}
	}

	public void put(String path) {
		handleVoidResponse(client.put(path));
	}

	public <T> void put(String path, String content, ResponseCallback<T> callback) {
		handleVoidResponse(callback, client.put(path, content));
	}


	public void put(String path, String content) {
		handleVoidResponse(client.put(path, content));
	}

	public void put(String path, InputStream data, String contentType,
			int contentLength) {
		handleVoidResponse(client.put(path, data, contentType, contentLength));
	}

	public <T> T put(String path, InputStream data, String contentType,
			int contentLength, ResponseCallback<T> callback) {
		return handleResponse(callback, client.put(path, data, contentType, contentLength));
	}
	
	public <T> void post(String path, String content, ResponseCallback<T> callback) {
		handleVoidResponse(callback, client.post(path, content));
	}

	public <T> T delete(String path, ResponseCallback<T> callback) {
		return handleResponse(callback, client.delete(path));
	}
	
	public void delete(String path) {
		handleVoidResponse(client.delete(path));
	}

	public <T> T head(String path, ResponseCallback<T> callback) {
		return handleResponse(callback, client.head(path));
	}
	
	private void handleVoidResponse(HttpResponse hr) {
		if (hr == null)
			return;
		try {
			if (!hr.isSuccessful()) {
				new StdResponseHandler<Void>().error(hr);
			}
		} finally {
			hr.releaseConnection();
		}
	}
	
	private <T> void handleVoidResponse(ResponseCallback<T> callback, HttpResponse hr) {
		try {
			if (hr.isSuccessful()) {
				callback.success(hr);
			} else {
				callback.error(hr);
			}
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		} finally {
			hr.releaseConnection();
		}
	}
	
	private <T> T handleResponse(ResponseCallback<T> callback, HttpResponse hr) {
		try {
			return hr.isSuccessful() ? callback.success(hr) : callback.error(hr);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		} finally {
			hr.releaseConnection();
		}
	}
}
