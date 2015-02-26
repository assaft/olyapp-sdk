package org.olyapp.sdk;

public interface CameraAPI {
	
	String getCameraModel();
	String getCommandList();
	
	ControlMode getMode();
	LiveViewAPI setLiveViewMode();
	RemoteShutterAPI setRemoteShutterMode();
	
	void shutdownCamera();
	
}
