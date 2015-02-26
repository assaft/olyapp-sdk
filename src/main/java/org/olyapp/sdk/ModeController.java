package org.olyapp.sdk;

public interface ModeController {

	public enum Mode {LiveView,RemoteShutter}; 
	
	Mode getMode();
	void setLiveView();
	void setRemoteShutter();
	
}
