package org.olyapp.sdk;

public interface CameraProtocol {

	String getCameraModel();
	String getCommandList();

	ControlMode getControlMode();
	void setControlMode(ControlMode controlMode, int timeoutMS);

	void remoteShutterTrigger(boolean lock);

	Object shutdownCamera();

}
