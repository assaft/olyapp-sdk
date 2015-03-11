package org.olyapp.sdk.impl;

import lombok.Value;

import org.olyapp.sdk.Point;

@Value
public class IPoint implements Point {

	int x;
	int y;

	static Point parse(String pointStr) {
		int xPos = pointStr.indexOf('x');
		return new IPoint(
				Integer.parseInt(pointStr.substring(0,xPos)), 
				Integer.parseInt(pointStr.substring(xPos+1)));
	}		
	
}
