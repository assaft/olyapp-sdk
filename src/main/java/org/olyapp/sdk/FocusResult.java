package org.olyapp.sdk;

public interface FocusResult {

	public interface FocusError extends FocusResult {
		String getMessage();
	}
	
	public interface FocusOK extends FocusResult {
		int getFocusX();
		int getFocusY();
		int getFocusWidth();
		int getFocusHeight();
	}
}
