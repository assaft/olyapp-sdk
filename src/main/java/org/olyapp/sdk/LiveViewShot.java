package org.olyapp.sdk;

public interface LiveViewShot {
	Image getSmallSizeJpeg();
	Image requestFullSizeJpeg() throws ProtocolError;
}
