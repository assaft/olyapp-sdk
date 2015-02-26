package org.olyapp.sdk.impl;

import java.io.IOException;

import org.olyapp.sdk.SimpleRequest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class ISimpleRequest implements SimpleRequest {

	@Override
	public void request(String s) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(s);
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response!=null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
