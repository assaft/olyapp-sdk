package org.olyapp.sdk;

public interface ImageProperties {
	int getWidth();
	int getHeight();
	int getPixelCount();
	
	Aperture getAperture();
	ShutterSpeed getShutterSpeed();

	ExposureValue getExposureComp(); 

	FocusInfo getFocusInfo();
	
	ShootingMode getShootingMode();
}
