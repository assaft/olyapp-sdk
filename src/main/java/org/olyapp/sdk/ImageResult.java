package org.olyapp.sdk;

import lombok.Value;

@Value
public class ImageResult {

	byte[] image;
	TakeResult takeResult;
	
	public String toString() {
		return takeResult.toString(); 
	}
}
