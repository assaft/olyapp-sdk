package org.olyapp.sdk;

public interface CameraAPI {
	
	String getCameraModel() throws ProtocolError;
	String getConnectionMode() throws ProtocolError;
	String getCommandList() throws ProtocolError;
	
	ControlMode getControlMode() throws ProtocolError;
	LiveView setLiveViewMode() throws ProtocolError;
	RemoteShutter setRemoteShutterMode() throws ProtocolError;
	Play setPlayMode() throws ProtocolError;
	
	void shutdownCamera() throws ProtocolError;
	
}
