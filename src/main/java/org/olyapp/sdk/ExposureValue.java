package org.olyapp.sdk;

public interface ExposureValue {
	ExposureValue inc();
	ExposureValue dec();
	
	boolean lessThan(ExposureValue ev);
	boolean moreThan(ExposureValue ev);

	int getThirds();
	
	long serializeValue(); 
}
