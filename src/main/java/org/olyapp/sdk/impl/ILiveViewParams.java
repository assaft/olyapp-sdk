package org.olyapp.sdk.impl;

import org.olyapp.sdk.Aperture;
import org.olyapp.sdk.ExposureValue;
import org.olyapp.sdk.FocusInfo;
import org.olyapp.sdk.LiveViewParams;
import org.olyapp.sdk.ShootingMode;
import org.olyapp.sdk.ShutterSpeed;

public class ILiveViewParams extends IImageProperties implements LiveViewParams {

	private Aperture aperture;
	private ShutterSpeed shutterSpeed;
	
	private ExposureValue exposureValue;
	private ShootingMode shottingMode;
	
	public ILiveViewParams(int width, int height, Aperture aperture,
			ShutterSpeed shutterSpeed, ExposureValue exposureValue,
			ShootingMode shottingMode, FocusInfo focusInfo) {
		super(width,height,aperture,shutterSpeed,exposureValue,shottingMode,focusInfo);
	}
	
	public Aperture getAperture() {
		return aperture!=null ? aperture : super.getAperture();
	}
	
	public ShutterSpeed getShutterSpeed() {
		return shutterSpeed!=null ? shutterSpeed : super.getShutterSpeed();
	}

	public ExposureValue getExposureComp() {
		return exposureValue!=null ? exposureValue : super.getExposureComp();
	}

	public ShootingMode getShootingMode() {
		return shottingMode!=null ? shottingMode : super.getShootingMode();
	}
	
	public void setAperture(Aperture aperture) {
		this.aperture = aperture;
	}

	public void setShutterSpeed(ShutterSpeed shutterSpeed) {
		this.shutterSpeed = shutterSpeed;
	}

	public void setExposureValue(ExposureValue exposureValue) {
		this.exposureValue = exposureValue;
	}

	public void setShootingMode(ShootingMode shottingMode) {
		this.shottingMode = shottingMode;
	}

	public String toString() {
		return getAperture() + "; " + getShutterSpeed();
	}
	
}
