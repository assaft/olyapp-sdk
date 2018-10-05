package org.olyapp.sdk;

import lombok.Value;

@Value
public class TakeResult {

	public enum TakeStatus {
		OK,
		FAILED
	}
	
	TakeStatus takeStatus;
	FocusResult focusResult;
	
	public String toString() {
		return takeStatus.toString() + "; auto-focus: " + focusResult.toString();
	}
}
