package org.olyapp.sdk;

public interface LiveViewHandler {
	void onImage(LiveViewImageData imageData);
	void onTimeout(long ms);
}
