package org.olyapp.sdk;

public interface ShutterSpeed {
	int getNum();
	int getDen();

	int serializeValue();
	
	ShutterSpeed inc();
	ShutterSpeed dec();

	ShutterSpeed incBy(ExposureValue ev);
	ShutterSpeed decBy(ExposureValue ev);

	boolean isLongest();
	boolean isShortest();
	
}
