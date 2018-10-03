package org.olyapp.sdk;

import java.util.Optional;

import lombok.Value;

@Value
public class FocusResult {

	public enum FocusStatus {
		OK, 		// Auto focus succeeded
		FAILED, 	// Auto focus failed
		DISABLED,	// Auto focus disabled (e.g. when in manual focus mode)
	}
	
	FocusStatus focusStatus;
	Optional<Integer> focusFrameTopLeftX;
	Optional<Integer> focusFrameTopLeftY;
	Optional<Integer> focusFrameWidth;
	Optional<Integer> focusFrameHeight;
}
