package org.olyapp.sdk;


public interface LiveViewAPI {
	LiveViewParams getParams();
	void setParams(LiveViewParams params);
	
	void setFocus(FocusPoint fp);
	
	Frame getNextFrame();
	Frame getNextFrame(LiveViewParams params, int timeoutMS);
	
	void shoot();
	void shoot(LiveViewParams params);
	void bracket(ShutterSpeed[] scheme);
}
