package org.olyapp.sdk.impl;

import org.olyapp.sdk.ExposureValue;

public class IExposureValue implements ExposureValue {
	
	private final static int constantBase = 10; 
	
	private final int thirds;
	private final int num;
	private final int den;

	private IExposureValue(int thirds, int num, int den) {
		this.num = num;
		this.den = den;
		this.thirds = thirds;
		//System.out.println("EV:" + thirds + "; " + num + "; " + den);
	}
	
	public IExposureValue(int thirds) {
		this(thirds,thirds*constantBase/3,constantBase);
	}

	public IExposureValue(int num, int den) {
		this((num/3)/den, num, den);
	}

	@Override
	public long serializeValue() {
		return (num << 8) & den;
	}

	@Override
	public int getThirds() {
		return thirds;
	}
	
	@Override
	public ExposureValue inc() {
		return new IExposureValue(thirds+1);
	}

	@Override
	public ExposureValue dec() {
		return new IExposureValue(thirds-1);
	}
	
	public String toString() {
		return Double.toString((double)num/den);
	}

	@Override
	public boolean lessThan(ExposureValue ev) {
		return getThirds()<ev.getThirds();
	}

	@Override
	public boolean moreThan(ExposureValue ev) {
		return getThirds()>ev.getThirds();
	}
	
}
