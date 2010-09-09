package org.ektorp.test.impl;

import java.io.*;

import org.ektorp.http.*;

public class HttpResponseStub implements HttpResponse {

	int code;
	String body;
	
	HttpResponseStub(int code, String body) {
		this.code = code;
		this.body = body;
	}
	
	public static HttpResponse valueOf(int code, String body) {
		return new HttpResponseStub(code, body);
	}
	
	@Override
	public int getCode() {
		return code;
	}

	@Override
	public InputStream getContent() {
		return new ByteArrayInputStream(body.getBytes());
	}

	@Override
	public boolean isSuccessful() {
		return code < 300;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void releaseConnection() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getRequestURI() {
		return "static/test/path";
	}

}
