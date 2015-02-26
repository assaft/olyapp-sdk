package org.olyapp.sdk.impl;

import org.olyapp.sdk.FocusPoint;
import org.olyapp.sdk.Frame;
import org.olyapp.sdk.LiveViewAPI;
import org.olyapp.sdk.LiveViewParams;
import org.olyapp.sdk.ShutterSpeed;

public class ILiveViewAPI implements LiveViewAPI {

	@Override
	public LiveViewParams getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(LiveViewParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus(FocusPoint fp) {
		// TODO Auto-generated method stub

	}

	@Override
	public Frame getNextFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Frame getNextFrame(LiveViewParams params, int timeoutMS) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shoot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shoot(LiveViewParams params) {
		setParams(params);
		shoot();
	}

	@Override
	public void bracket(ShutterSpeed[] scheme) {

		LiveViewParams params = getParams();
		for (ShutterSpeed entry : scheme) {
			params.setShutterSpeed(entry);
			shoot(params);
		}
		
	}

}
