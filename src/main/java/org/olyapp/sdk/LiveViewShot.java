package org.olyapp.sdk;

public interface LiveViewShot {
	String getTakeStatus();
	FocusResult getFocusResult();
	
	Image requestSmallSizeJpeg() throws ProtocolError;
	Image requestFullSizeJpeg() throws ProtocolError;
}
