package org.ektorp.http;

import java.io.*;


public interface HttpClient {

	HttpResponse get(String uri);

	HttpResponse put(String uri, String content);

	HttpResponse put(String uri);

	HttpResponse put(String uri, InputStream data, String contentType,
			int contentLength);

	HttpResponse post(String uri, String content);

	HttpResponse delete(String uri);
	
	HttpResponse head(String uri);

}