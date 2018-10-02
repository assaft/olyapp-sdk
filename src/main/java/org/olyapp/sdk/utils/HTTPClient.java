package org.olyapp.sdk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPClient {

	public static final int CONNECT_TIMEOUT = 500; //ms
	public static final int CONNECTION_TIMEOUT = 500; //ms
	public static final int SOCKET_TIMEOUT = 2000; //ms
	
	public static final String DEF_XML_CHAR_SET = "ISO-8859-1";
	
	public static String doGet(String s) throws ProtocolError {
		HttpGet httpGet = new HttpGet(s);
		return request(httpGet);
	}
	
	public static String doPost(String s, String value) throws ProtocolError {
		HttpPost httpPost = new HttpPost(s);
		httpPost.setEntity(new StringEntity(value,DEF_XML_CHAR_SET));
		return request(httpPost);
	}
	
	private static String request(HttpUriRequest httpRequest) throws ProtocolError {
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
	        return new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines()
					   .parallel().collect(Collectors.joining(""));
		} catch (IOException e) {
			throw new ProtocolError(e.getMessage());
		} 
	}
	
}
