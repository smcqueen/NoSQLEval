package org.ektorp.http;

import java.io.*;

import org.apache.http.*;
import org.apache.http.message.*;
import org.apache.http.protocol.*;
import org.ektorp.util.*;
import org.slf4j.*;

/**
 * 
 * @author henriklundgren
 *
 */
public class StdHttpResponse implements HttpResponse {

	private final static Logger LOG = LoggerFactory.getLogger(StdHttpResponse.class);
	private final static HttpEntity NULL_ENTITY = new NullEntity();
	
	private final HttpEntity entity;
	private final StatusLine status;
	private final String requestURI;
	
	public static StdHttpResponse of(org.apache.http.HttpResponse rsp, String requestURI) {
		return new StdHttpResponse(rsp.getEntity(), rsp.getStatusLine(), requestURI);
	}
	
	private StdHttpResponse(HttpEntity e, StatusLine status, String requestURI) {
		this.entity = e != null ? e : NULL_ENTITY;
		this.status = status;
		this.requestURI = requestURI;
	}
	
	@Override
	public int getCode() {
		return status.getStatusCode();
	}

	public String getReason() {
		return status.getReasonPhrase();
	}
	
	public String getRequestURI() {
		return requestURI;
	}
	
	@Override
	public int getContentLength() {
		return (int) entity.getContentLength();
	}

	@Override
	public String getContentType() {
		return entity.getContentType().getValue();
	}

	@Override
	public InputStream getContent() {
		try {
			return new ConnectionReleasingInputStream(entity.getContent());
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	@Override
	public boolean isSuccessful() {
		return getCode() < 300;
	}

	@Override
	public void releaseConnection() {
		try {
			entity.consumeContent();
		} catch (IOException e) {
			LOG.error("caught exception while releasing connection: {}", e.getMessage());
		}
	}

	@Override
	public String toString() {
		return status.getStatusCode() + ":" + status.getReasonPhrase();
	}
	
	private class ConnectionReleasingInputStream extends FilterInputStream {
		
		private ConnectionReleasingInputStream(InputStream src) {
			super(src);
		}

		@Override
		public void close() throws IOException {
			releaseConnection();
		}
		
	}
	
	private static class NullEntity implements HttpEntity {

		final static Header contentType = new BasicHeader(HTTP.CONTENT_TYPE, "null");
		final static Header contentEncoding = new BasicHeader(HTTP.CONTENT_ENCODING, "UTF-8");
		
		@Override
		public void consumeContent() throws IOException {
			
		}

		@Override
		public InputStream getContent() throws IOException,
				IllegalStateException {
			return null;
		}

		@Override
		public Header getContentEncoding() {
			return contentEncoding;
		}

		@Override
		public long getContentLength() {
			return 0;
		}

		@Override
		public Header getContentType() {
			return contentType;
		}

		@Override
		public boolean isChunked() {
			return false;
		}

		@Override
		public boolean isRepeatable() {
			return true;
		}

		@Override
		public boolean isStreaming() {
			return false;
		}

		@Override
		public void writeTo(OutputStream outstream) throws IOException {
			throw new UnsupportedOperationException("NullEntity cannot write");
		}
		
	}
}
