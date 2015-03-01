package org.olyapp.sdk.impl;

import org.olyapp.sdk.FocusPoint;

public class IFocusPoint implements FocusPoint {

	private final int x;
	private final int y;
	
	public IFocusPoint(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	static FocusPoint parse(String focusPoint) {
		int xPos = focusPoint.indexOf('x');
		return new IFocusPoint(
				Integer.parseInt(focusPoint.substring(0,xPos)), 
				Integer.parseInt(focusPoint.substring(xPos+1)));
	}		
	
}
