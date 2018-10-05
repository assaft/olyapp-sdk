package org.olyapp.sdk;

import lombok.Value;

@Value
public class Dimensions {
	int width;
	int height;
	
	public String toString() {
		return width + "x" + height;
	}
}
