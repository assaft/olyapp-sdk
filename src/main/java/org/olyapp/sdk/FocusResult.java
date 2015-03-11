package org.olyapp.sdk;

public interface FocusResult {

	public interface FocusError extends FocusResult {
		String getMessage();
	}
	
	public interface FocusOK extends FocusResult {
		Point getFocusPoint();
		Dimensions getFocusArea();
	}
}
