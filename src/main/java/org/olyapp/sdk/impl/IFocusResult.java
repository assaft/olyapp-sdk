package org.olyapp.sdk.impl;

import org.olyapp.sdk.FocusPoint;
import org.olyapp.sdk.FocusArea;
import org.olyapp.sdk.FocusResult.FocusError;
import org.olyapp.sdk.FocusResult.FocusOK;

public class IFocusResult {

	public static class IFocusError implements FocusError {

		private final String message;
		
		public IFocusError(String message) {
			super();
			this.message = message;
		}

		@Override
		public String getMessage() {
			return message;
		}
		
	}
	
	public static class IFocusOK implements FocusOK {
		
		private final FocusPoint focusPoint;
		private final FocusArea focusArea;

		public IFocusOK(FocusPoint focusPoint, FocusArea focusArea) {
			super();
			this.focusPoint = focusPoint;
			this.focusArea = focusArea;
		}

		@Override
		public FocusPoint getFocusPoint() {
			return focusPoint;
		}

		@Override
		public FocusArea getFocusArea() {
			return focusArea;
		}
		
	}
}
