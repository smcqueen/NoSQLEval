package org.ektorp.http;

import java.io.*;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.params.*;
import org.apache.http.conn.*;
import org.apache.http.conn.params.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.tsccm.*;
import org.apache.http.params.*;
import org.ektorp.util.*;
import org.slf4j.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public class StdHttpClient implements HttpClient {
	
	private final org.apache.http.client.HttpClient client;
	private final static Logger LOG = LoggerFactory.getLogger(StdHttpClient.class);

	public StdHttpClient(org.apache.http.client.HttpClient hc) {
		client = hc;
	}
	
	@Override
	public HttpResponse delete(String uri) {
		return executeRequest(new HttpDelete(uri));
	}

	@Override
	public HttpResponse get(String uri) {
		return executeRequest(new HttpGet(uri));
	}

	@Override
	public HttpResponse post(String uri, String content) {
		return executePutPost(new HttpPost(uri), content);
	}

	@Override
	public HttpResponse put(String uri, String content) {
		return executePutPost(new HttpPut(uri), content);
	}

	@Override
	public HttpResponse put(String uri) {
		return executeRequest(new HttpPut(uri));
	}

	@Override
	public HttpResponse put(String uri, InputStream data, String contentType,
			int contentLength) {
		InputStreamEntity e = new InputStreamEntity(data, contentLength);
		e.setContentType(contentType);
		
		HttpPut hp = new HttpPut(uri);
		hp.setEntity(e);
		return executeRequest(hp);
	}
	
	@Override
	public HttpResponse head(String uri) {
		return executeRequest(new HttpHead(uri));
	}
	
	private HttpResponse executePutPost(HttpEntityEnclosingRequestBase request, String content) {
		try {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Content: {}", content);
			}
			StringEntity e = new StringEntity(content, "UTF-8");
			e.setContentType("application/json");
			request.setEntity(e);
			return executeRequest(request);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}
	
	private HttpResponse executeRequest(HttpRequestBase request) {
		try {
			org.apache.http.HttpResponse rsp = client.execute(request);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format("%s %s %s", request.getMethod(), request.getURI(), rsp.getStatusLine().getStatusCode(), rsp.getStatusLine().getReasonPhrase()));
			}
			return StdHttpResponse.of(rsp, request.getURI().toString());
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}
	
	public static class Builder {
		String host = "localhost";
		int port = 5984;
		int maxConnections = 20;
		int connectionTimeout = 1000;
		int socketTimeout = 10000;
		ClientConnectionManager conman;
		
		String username;
		String password;
		
		public Builder host(String s) {
			host = s;
			return this;
		}
		
		public ClientConnectionManager configureConnectionManager() {
			if (conman != null) {
				return conman;
			}
			
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(
			         new Scheme("http", PlainSocketFactory.getSocketFactory(), port));
			
			HttpParams params = new BasicHttpParams();
			
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
			HttpConnectionParams.setSoTimeout(params, socketTimeout);
			HttpConnectionParams.setTcpNoDelay(params, Boolean.TRUE);
			
			ConnManagerParams.setMaxTotalConnections(params, maxConnections);
			ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(maxConnections));
			
			return new ThreadSafeClientConnManager(params, schemeRegistry);
		}
		
		public org.apache.http.client.HttpClient configureClient() {
			HttpParams params = new BasicHttpParams();
			params.setParameter(ClientPNames.DEFAULT_HOST, new HttpHost(host, port, "http"));
			
			DefaultHttpClient dc = new DefaultHttpClient(configureConnectionManager(), params);
			if (username != null && password != null) {
				dc.getCredentialsProvider().setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(username, password));
			}
			return dc;
		}

		public Builder port(int i) {
			port = i;
			return this;
		}
		
		public Builder username(String s) {
			username = s;
			return this;
		}
		
		public Builder password(String s) {
			password = s;
			return this;
		}
		
		public Builder maxConnections(int i) {
			maxConnections = i;
			return this;
		}
		
		public Builder connectionTimeout(int i) {
			connectionTimeout = i;
			return this;
		}
		
		public Builder socketTimeout(int i) {
			socketTimeout = i;
			return this;
		}
		/**
		 * Bring your own Connection Manager.
		 * If this parameters is set, the parameters port, maxConnections, connectionTimeout and socketTimeout are ignored.
		 * @param cm
		 * @return
		 */
		public Builder connectionManager(ClientConnectionManager cm) {
			conman = cm;
			return this;
		}
		
		public HttpClient build() {
			return new StdHttpClient(configureClient());
		}
		
	}

}
