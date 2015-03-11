package org.olyapp.sdk.impl;

import lombok.Value;

import org.olyapp.sdk.FocusResult;
import org.olyapp.sdk.Image;
import org.olyapp.sdk.LiveViewShot;
import org.olyapp.sdk.ProtocolError;

@Value
public class ILiveViewShot implements LiveViewShot {
	String takeStatus;
	FocusResult focusResult;
	
	@Override
	public Image requestSmallSizeJpeg() throws ProtocolError {
		return ICameraProtocol.getInst().getSmallJpeg();
	}

	@Override
	public Image requestFullSizeJpeg() throws ProtocolError {
		return ICameraProtocol.getInst().getFullJpeg();
	}

}
