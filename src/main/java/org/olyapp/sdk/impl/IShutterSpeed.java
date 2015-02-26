package org.olyapp.sdk.impl;

import java.util.HashMap;
import java.util.Map;

import org.olyapp.sdk.ExposureValue;
import org.olyapp.sdk.ShutterSpeed;

public class IShutterSpeed implements ShutterSpeed {

	private final int num;
	private final int den;
	
	private static final IShutterSpeed[] shutterSpeeds = new IShutterSpeed[] {
		new IShutterSpeed(1,8000),new IShutterSpeed(1,6400),new IShutterSpeed(1,5000),
		new IShutterSpeed(1,4000),new IShutterSpeed(1,3200),new IShutterSpeed(1,2500),
		new IShutterSpeed(1,2000),new IShutterSpeed(1,1600),new IShutterSpeed(1,1250),
		new IShutterSpeed(1,1000),new IShutterSpeed(1,800), new IShutterSpeed(1,640),
		new IShutterSpeed(1,500), new IShutterSpeed(1,400), new IShutterSpeed(1,320),
		new IShutterSpeed(1,250), new IShutterSpeed(1,200), new IShutterSpeed(1,160),
		new IShutterSpeed(1,125), new IShutterSpeed(1,100), new IShutterSpeed(1,80),
		new IShutterSpeed(1,60),  new IShutterSpeed(1,50),  new IShutterSpeed(1,40),
		new IShutterSpeed(1,30),  new IShutterSpeed(1,25),  new IShutterSpeed(1,20),
		new IShutterSpeed(1,15),  new IShutterSpeed(1,13),  new IShutterSpeed(1,10),
		new IShutterSpeed(1,8),   new IShutterSpeed(1,6),   new IShutterSpeed(1,5),
		new IShutterSpeed(1,4),   new IShutterSpeed(1,3),
		// many more
	//	new IShutterSpeed(32,10)
		// many more
	};

	
	private static final Map<ShutterSpeed,Integer> shutterSpeedsIndexes;

	static {
		shutterSpeedsIndexes = new HashMap<ShutterSpeed, Integer>();
		for (int i=0, size=shutterSpeeds.length ; i<size ; i++) {
			shutterSpeedsIndexes.put(shutterSpeeds[i], i);
		}
		
	}
	
	public IShutterSpeed(int num, int den) {
		this.num = num;
		this.den = den;
	}
	
	@Override
	public int getNum() {
		return num;
	}

	@Override
	public int getDen() {
		return den;
	}
	
	@Override
    public boolean equals(Object obj) {
		boolean res = false;
		if (obj instanceof IShutterSpeed) {
			IShutterSpeed shutterSpeed = (IShutterSpeed) obj;
			res = shutterSpeed.num==num && shutterSpeed.den==den; 
		}
		return res;
	}
    
	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + num;
	    result = prime * result + den;
	    return result;
	}	

	@Override
	public int serializeValue() {
		return (num << 8) & den;
	}

	@Override
	public ShutterSpeed inc() {
		return shutterSpeeds[shutterSpeedsIndexes.get(this)+1];
	}

	@Override
	public ShutterSpeed dec() {
		return shutterSpeeds[shutterSpeedsIndexes.get(this)-1];
	}

	
	public String toString() {
		return (num==1 ? num + "/" + den : Double.toString((double)num/den)) + "s";
	}

	@Override
	public ShutterSpeed incBy(ExposureValue ev) {
		return shutterSpeeds[shutterSpeedsIndexes.get(this)+ev.getThirds()];
	}

	@Override
	public ShutterSpeed decBy(ExposureValue ev) {
		return shutterSpeeds[shutterSpeedsIndexes.get(this)-ev.getThirds()];
	}
	
	public static ExposureValue subtract(ShutterSpeed shutterSpeed1, ShutterSpeed shutterSpeed2) {
		return new IExposureValue(
				shutterSpeedsIndexes.get(shutterSpeed1)-
				shutterSpeedsIndexes.get(shutterSpeed2));
	}

	@Override
	public boolean isLongest() {
		return shutterSpeedsIndexes.get(this)==(shutterSpeeds.length-1);
	}

	@Override
	public boolean isShortest() {
		return shutterSpeedsIndexes.get(this)==0;
	}
}
