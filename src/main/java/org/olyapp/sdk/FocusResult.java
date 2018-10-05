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
	Optional<Coordinate> focusFrameTopLeftCoordinate;
	Optional<Dimensions> focusFrameDimensions;
	
	public String toString() {
		return focusStatus.toString() + (focusStatus==FocusStatus.OK ? 
				" at: " + focusFrameTopLeftCoordinate.get() + 
				" size: " + focusFrameDimensions : "");
	}
}
