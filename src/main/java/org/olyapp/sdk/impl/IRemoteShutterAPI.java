package org.olyapp.sdk.impl;

import org.olyapp.sdk.RemoteShutterAPI;

public class IRemoteShutterAPI implements RemoteShutterAPI{

	@Override
	public void trigger(boolean lock) {
		ICameraProtocol.getInst().remoteShutterTrigger(lock);
	}

}
