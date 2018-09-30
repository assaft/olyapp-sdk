package org.olyapp.sdk.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;

public class XMLHandler<T> implements Function<HttpResponse,T> {

	private final List<String> xPaths;
	
	public XMLHandler(String xPath) {
		xPaths = Arrays.asList(xPath); 
	}

	public XMLHandler(List<String> xPaths) {
		this.xPaths = new ArrayList<>(xPaths);
	}

	@Override
	public T apply(HttpResponse t) {
		DocumentBuilder xmlDocument = builder.parse(this.getFile());

		XPath xPath = XPathFactory.newInstance().newXPath();
		for (String xPathExp : xPaths) {
			nodeList = (NodeList) xPath.compile(xPathExp).evaluate(xmlDocument, XPathConstants.NODESET);
		
		return null;
	}
	
}
