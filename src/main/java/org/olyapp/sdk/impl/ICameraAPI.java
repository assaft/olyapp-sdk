package org.olyapp.sdk.impl;

import org.olyapp.sdk.CameraAPI;
import org.olyapp.sdk.ControlMode;
import org.olyapp.sdk.LiveView;
import org.olyapp.sdk.Play;
import org.olyapp.sdk.ProtocolError;
import org.olyapp.sdk.RemoteShutter;

public class ICameraAPI implements CameraAPI {

	@Override
	public String getCameraModel() throws ProtocolError {
		return ICameraProtocol.getInst().getCameraModel();
	}

	@Override
	public String getConnectionMode() throws ProtocolError {
		return ICameraProtocol.getInst().getConnectionModel();
	}
	
	@Override
	public String getCommandList() throws ProtocolError {
		return ICameraProtocol.getInst().getCommandList();
	}

	@Override
	public ControlMode getControlMode() throws ProtocolError {
		return ICameraProtocol.getInst().getControlMode();
	}

	@Override
	public LiveView setLiveViewMode() throws ProtocolError {
		ICameraProtocol.getInst().setControlMode(ControlMode.LiveView);
		return new ILiveView(ICameraProtocol.getInst().getNextFrame().getMetadata());
	}

	@Override
	public RemoteShutter setRemoteShutterMode() throws ProtocolError {
		ICameraProtocol.getInst().setControlMode(ControlMode.RemoteShutter);
		return new IRemoteShutter();
	}

	@Override
	public Play setPlayMode() throws ProtocolError {
		ICameraProtocol.getInst().setControlMode(ControlMode.Play);
		return new IPlay();
	}
	
	@Override
	public void shutdownCamera() throws ProtocolError {
		ICameraProtocol.getInst().shutdownCamera();
	}

}
