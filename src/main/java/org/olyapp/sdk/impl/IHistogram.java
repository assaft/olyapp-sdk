package org.olyapp.sdk.impl;

import org.olyapp.sdk.Histogram;

public class IHistogram implements Histogram {
	
	private final int overExposedPct;
	private final int underExposedPct;
	
	public IHistogram(int overExposedPct, int underExposedPct) {
		this.overExposedPct = overExposedPct;
		this.underExposedPct = underExposedPct;
	}

	@Override
	public int getOverExposedPct() {
		return overExposedPct;
	}

	@Override
	public int getUnderExposedPct() {
		return underExposedPct;
	}
}
