package org.olyapp.sdk.impl;

import org.olyapp.sdk.CameraAPI;
import org.olyapp.sdk.ControlMode;
import org.olyapp.sdk.LiveViewAPI;
import org.olyapp.sdk.RemoteShutterAPI;

public class ICameraAPI implements CameraAPI {

	@Override
	public String getCameraModel() {
		return ICameraProtocol.getInst().getCameraModel();
	}

	@Override
	public String getConnectionModel() {
		return ICameraProtocol.getInst().getConnectionModel();
	}
	
	@Override
	public String getCommandList() {
		return ICameraProtocol.getInst().getCommandList();
	}

	@Override
	public ControlMode getMode() {
		return ICameraProtocol.getInst().getControlMode();
	}

	@Override
	public LiveViewAPI setLiveViewMode() {
		ICameraProtocol.getInst().setControlMode(ControlMode.LiveView,0);
		return new ILiveViewAPI();
	}

	@Override
	public RemoteShutterAPI setRemoteShutterMode() {
		ICameraProtocol.getInst().setControlMode(ControlMode.RemoteShutter,0);
		return new IRemoteShutterAPI();
	}

	@Override
	public void shutdownCamera() {
		ICameraProtocol.getInst().shutdownCamera();
	}

}
