package org.olyapp.sdk.impl;

import org.olyapp.sdk.FocusArea;

public class IFocusArea implements FocusArea {

	private final int width;
	private final int height;

	public IFocusArea(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}

	static FocusArea parse(String frameArea) {
		int xPos = frameArea.indexOf('x');
		return new IFocusArea(
				Integer.parseInt(frameArea.substring(0,xPos)), 
				Integer.parseInt(frameArea.substring(xPos+1)));
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

}
