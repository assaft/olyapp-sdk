package org.olyapp.sdk.comm;

class ImagePacket {
	
	private final byte[] data;
	private final int startingPoint;
	private final int endingPoint;
	
	public ImagePacket(byte[] data, int startingPoint, int endingPoint) {
		super();
		this.startingPoint = startingPoint;
		this.endingPoint = endingPoint;
		this.data = new byte[endingPoint-startingPoint];
		System.arraycopy(data,startingPoint,this.data,0,this.data.length);
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int getStartingPoint() {
		return startingPoint;
	}
	
	public int getEndingPoint() {
		return endingPoint;
	}
	
}