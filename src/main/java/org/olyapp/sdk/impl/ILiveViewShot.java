package org.olyapp.sdk.impl;

import lombok.Value;

import org.olyapp.sdk.Image;
import org.olyapp.sdk.LiveViewShot;
import org.olyapp.sdk.ProtocolError;

@Value
public class ILiveViewShot implements LiveViewShot {
	Image smallSizeJpeg;
	
	@Override
	public Image requestFullSizeJpeg() throws ProtocolError {
		return ICameraProtocol.getInst().getFullJPG();
	}

}
