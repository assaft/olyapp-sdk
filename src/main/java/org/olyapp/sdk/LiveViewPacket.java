package org.olyapp.sdk;

import lombok.Value;

@Value
public class LiveViewPacket {
	
	byte[] data;
	int startingPoint;
	int endingPoint;
	
	public LiveViewPacket(byte[] data, int startingPoint, int endingPoint) {
		super();
		this.startingPoint = startingPoint;
		this.endingPoint = endingPoint;
		this.data = new byte[endingPoint-startingPoint];
		System.arraycopy(data,startingPoint,this.data,0,this.data.length);
	}
	
}