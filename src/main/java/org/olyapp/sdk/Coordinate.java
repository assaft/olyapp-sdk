package org.olyapp.sdk;

import lombok.Value;

@Value
public class Coordinate {
	int x;
	int y;
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
}
