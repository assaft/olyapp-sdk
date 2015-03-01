package org.olyapp.sdk;


public interface LiveViewAPI {
	LiveViewParams getParams();
	void setParams(LiveViewParams params);
	
	void setShootingMode(ShootingMode mode);
	void setExpComp(ExposureValue ev);
	void setAperture(Aperture aperture);
	void setShutterSpeed(ShutterSpeed shutterSpeed);	

	void getShootingMode(ShootingMode mode);
	void getExpComp(ExposureValue ev);
	void getAperture(Aperture aperture);
	void getShutterSpeed(ShutterSpeed shutterSpeed);	
	
	void setFocus(FocusPoint fp);
	
	Frame getNextFrame();
	Frame getNextFrame(LiveViewParams params, int timeoutMS);
	
	void shoot();
	void shoot(LiveViewParams params);
	void bracket(ShutterSpeed[] scheme);
}
