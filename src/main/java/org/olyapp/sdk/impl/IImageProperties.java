package org.olyapp.sdk.impl;

import org.olyapp.sdk.Aperture;
import org.olyapp.sdk.ExposureValue;
import org.olyapp.sdk.FocusInfo;
import org.olyapp.sdk.ImageProperties;
import org.olyapp.sdk.ShootingMode;
import org.olyapp.sdk.ShutterSpeed;

public class IImageProperties implements ImageProperties {

	final int width;
	final int height;
	final int pixelCount;

	final Aperture aperture;
	final ShutterSpeed shutterSpeed;
	
	final ExposureValue exposureValue;
	final ShootingMode shottingMode;
	
	final FocusInfo focusInfo;
	
	public IImageProperties(int width, int height, Aperture aperture,
			ShutterSpeed shutterSpeed, ExposureValue exposureValue,
			ShootingMode shottingMode, FocusInfo focusInfo) {
		super();
		this.width = width;
		this.height = height;
		this.pixelCount = width*height;
		this.aperture = aperture;
		this.shutterSpeed = shutterSpeed;
		this.exposureValue = exposureValue;
		this.shottingMode = shottingMode;
		this.focusInfo = focusInfo;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getPixelCount() {
		return pixelCount;
	}

	public Aperture getAperture() {
		return aperture;
	}
	
	@Override
	public ShutterSpeed getShutterSpeed() {
		return shutterSpeed;
	}

	public ExposureValue getExposureComp() {
		return exposureValue;
	}

	public ShootingMode getShootingMode() {
		return shottingMode;
	}

	public FocusInfo getFocusInfo() {
		return focusInfo;
	}


}
