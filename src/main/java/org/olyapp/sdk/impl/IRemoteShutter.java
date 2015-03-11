package org.olyapp.sdk.impl;

import org.olyapp.sdk.RemoteShutter;

public class IRemoteShutter implements RemoteShutter{

	@Override
	public void trigger(boolean lock) {
		ICameraProtocol.getInst().remoteShutterTrigger(lock);
	}

}
