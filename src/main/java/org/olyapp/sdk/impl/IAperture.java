package org.olyapp.sdk.impl;

import org.olyapp.sdk.Aperture;

public class IAperture implements Aperture {

	
	public static final Aperture f4_0 = new IAperture(40,10);  
	public static final Aperture f5_6 = new IAperture(56,10);  
	
	/*
	private static final Aperture[] apertures = {
			new IAperture(10,10),
			new IAperture(11,10),
			new IAperture(12,10),
			new IAperture(14,10),
			new IAperture(16,10),
			new IAperture(18,10),
			new IAperture(20,10),
			new IAperture(22,10),
			new IAperture(25,10),
			new IAperture(28,10),
			new IAperture(32,10),
			new IAperture(35,10),
			new IAperture(40,10),
			new IAperture(45,10),
			new IAperture(50,10),
			new IAperture(56,10),
			new IAperture(63,10),
			new IAperture(71,10),
			new IAperture(80,10),
			new IAperture(90,10),
			new IAperture(10,10),
			new IAperture(11,10),
			new IAperture(13,10),
			new IAperture(14,10),
			new IAperture(16,10)};

	private static final Map<Aperture,Integer> aperturesIndexes;

	static {
		aperturesIndexes = new HashMap<Aperture, Integer>();
		for (int i=0, size=apertures.length ; i<size ; i++) {
			aperturesIndexes.put(apertures[i], i);
		}
		
	}*/
	
	
	private final int num;
	private final int den;
	
	public IAperture(int num, int den) {
		this.num = num;
		this.den = den;
	}

	
	
	@Override
	public int serialize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String toString() {
		return "F" + Double.toString((double)num/den);
	}
	
}
