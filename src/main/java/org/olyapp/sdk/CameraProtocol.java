package org.olyapp.sdk;

import org.olyapp.sdk.io.StartTake;

public interface CameraProtocol {

	String getCameraModel();
	String getConnectionModel();
	
	String getCommandList();

	ControlMode getControlMode();
	void setControlMode(ControlMode controlMode, int timeoutMS);

	FocusResult setFocusAt(FocusPoint point);
	void releaseFocus();
	
	void setIso(ISO iso);
	void setAperture(Aperture aperture);
	void setShutterSpeed(ShutterSpeed speed);
	void setExpComp(ExposureValue ev);
	
	StartTake startLiveView();
	void stopLiveView();
	
	void remoteShutterTrigger(boolean lock);

	void shutdownCamera();

}
