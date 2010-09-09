package org.ektorp.test.impl;

import java.io.*;

import org.apache.commons.io.*;
import org.ektorp.http.*;

public class ResponseOnFileStub implements HttpResponse {

	int code;
	InputStream in;
	boolean connectionReleased;
	String contentType = "application/json";
	int contentLength;
	
	public static ResponseOnFileStub newInstance(int code, String fileName) {
		ResponseOnFileStub r = new ResponseOnFileStub();
		r.code = code;
		r.in = r.getClass().getResourceAsStream(fileName);
		return r;
	}
	
	public static ResponseOnFileStub newInstance(int code, String fileName, String contentType, int contentLength) {
		ResponseOnFileStub r = new ResponseOnFileStub();
		r.code = code;
		r.in = r.getClass().getResourceAsStream(fileName);
		r.contentLength = contentLength;
		r.contentType = contentType;
		return r;
	}
	
	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getContentType() {
		return "text/json";
	}

	@Override
	public InputStream getContent() {
		return in;
	}

	@Override
	public boolean isSuccessful() {
		return code < 300;
	}

	@Override
	public void releaseConnection() {
		connectionReleased = true;
		IOUtils.closeQuietly(in);
	}
	
	public boolean isConnectionReleased() {
		return connectionReleased;
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
