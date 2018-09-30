package org.olyapp.sdk.impl;

import lombok.Value;

@Value
public class HTTPResponse {
	int statusCode;
	String statusText;
	String content;
}
