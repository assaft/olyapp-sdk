package org.olyapp.sdk.impl;

import lombok.Value;

import org.olyapp.sdk.Point;
import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.FocusResult.FocusError;
import org.olyapp.sdk.FocusResult.FocusOK;

public class IFocusResult {

	@Value
	public static class IFocusError implements FocusError {
		String message;
	}
	
	@Value
	public static class IFocusOK implements FocusOK {
		Point focusPoint;
		Dimensions focusArea;
	}
}
