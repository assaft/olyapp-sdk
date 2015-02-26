package org.olyapp.sdk;


public interface FrameServer {
	Frame getFrame(ShootingMode mode, 
			double param1, 
			double param2, 
			int expComp, 
			ShotType shotType);

	int getWidth();
	int getHeight();
}
