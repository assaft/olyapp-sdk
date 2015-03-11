package org.olyapp.sdk;

public interface FrameMetadata {

	Dimensions getDimensions();
	
	Fraction getShutterSpeed();

	Fraction getAperture();
	
	int getExpComp();
	
	boolean isExpCompWarning();
	
	int getWhiteBalance();
	
	boolean isAutoWhiteBalance();
	
	int getISO();
	
	boolean isISOWarning();
	
	int getAvailableCapacity();

	Fraction getAspectRatio();
	
	int getBatteryLevel();
	
	boolean isRotated();
	
	int getFocusField();
	
	boolean isAutoFocus();

}
