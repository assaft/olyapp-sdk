package org.olyapp.sdk.impl;

import lombok.Value;

import org.olyapp.sdk.Dimensions;

@Value
public class IDimension implements Dimensions {

	int width;
	int height;

	static Dimensions parse(String dimensionStr) {
		int xPos = dimensionStr.indexOf('x');
		return new IDimension(
				Integer.parseInt(dimensionStr.substring(0,xPos)), 
				Integer.parseInt(dimensionStr.substring(xPos+1)));
	}

	public String toString() {
		return String.format("%04dx%04d",width,height);
	}
}
