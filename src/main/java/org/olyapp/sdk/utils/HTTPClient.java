package org.olyapp.sdk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.olyapp.sdk.ProtocolError;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPClient {

	private static final int CONNECT_TIMEOUT = 500; //ms
	private static final int CONNECTION_TIMEOUT = 500; //ms
	private static final int SOCKET_TIMEOUT = 5000; //ms
	
	private static final String DEF_XML_CHAR_SET = "ISO-8859-1";

	private static final HTTPClient instance = new HTTPClient();
	
	public static HTTPClient getInstance() {
		return instance;
	}
	
	private HTTPClient() {
	}
	
	private interface HttpEntityReader<T> {
		T read(HttpEntity entity) throws IOException;
	}

	@Synchronized
	public String doGet(String s) throws ProtocolError {
		HttpGet httpGet = new HttpGet(s);
		return request(httpGet,textualEntityReader);
	}

	@Synchronized
	public byte[] doGetBinary(String s) throws ProtocolError {
		HttpGet httpGet = new HttpGet(s);
		return request(httpGet,binaryEntityReader);
	}
	
	@Synchronized
	public String doPost(String s, String value) throws ProtocolError {
		HttpPost httpPost = new HttpPost(s);
		httpPost.setEntity(new StringEntity(value,DEF_XML_CHAR_SET));
		return request(httpPost,textualEntityReader);
	}
	
	private <T> T request(HttpUriRequest httpRequest, HttpEntityReader<T> entityReader) throws ProtocolError {
		RequestConfig config = RequestConfig.custom()
				  .setConnectTimeout(CONNECT_TIMEOUT)
				  .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
				  .setSocketTimeout(SOCKET_TIMEOUT).build();
		try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
				CloseableHttpResponse response = httpclient.execute(httpRequest)) {
			StatusLine statusLine = response.getStatusLine();
	        int statusCode = statusLine.getStatusCode();
			if (statusCode!=200 && statusCode!=202) {
	        	log.error("HTTP error: [" + statusLine + "] for request: [" + httpRequest + "]");
	        	throw new ProtocolError("Request error: " + statusLine);
	        }
	        return entityReader.read(response.getEntity());
		} catch (IOException e) {
			throw new ProtocolError(e.getMessage());
		} 
		
	}
	
	private static final HttpEntityReader<String> textualEntityReader = h->
		new BufferedReader(new InputStreamReader(h.getContent())).lines()
			   .parallel().collect(Collectors.joining(""));  

	private static final HttpEntityReader<byte[]> binaryEntityReader = h-> 
		h.getContent().readAllBytes();
		
}
