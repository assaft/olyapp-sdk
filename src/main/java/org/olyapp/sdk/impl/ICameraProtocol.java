package org.olyapp.sdk.impl;

import java.io.IOException;

import org.olyapp.sdk.CameraProtocol;
import org.olyapp.sdk.ControlMode;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ICameraProtocol implements CameraProtocol {

	private static final String CAMERA_IP = "192.168.0.10";
	
	private ICameraProtocol() {
	}
	
	private void request(String s) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(s);
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response!=null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	@Override
	public void remoteShutterTrigger(boolean lock) {
		// TODO Auto-generated method stub
	}
	
	private static CameraProtocol instance = new ICameraProtocol();
	
	public static CameraProtocol getInst() {
		return instance;
	}

	@Override
	public void setControlMode(ControlMode controlMode, int timeoutMS) {
		long startTime = System.currentTimeMillis();
		long endTime = timeoutMS > 0 ? startTime + timeoutMS : Long.MAX_VALUE;
		if (controlMode==ControlMode.RemoteShutter) {
			request("http://"+CAMERA_IP+"/switch_cammode.cgi?mode=shutter" /*,
					System.currentTimeMillis()-startTime*/);
		} else {
			// live view request
			// open udp sockets, create queues...
		}
		
		long remainingTime;
		while (System.currentTimeMillis()>endTime && getControlMode()!=controlMode) {
			if ((remainingTime = System.currentTimeMillis()-endTime)>0) {
//				Thread.sleep(Math.min(200, remainingTime));
			}
		}

		if (getControlMode()!=controlMode) {
			//throw new TimeoutExpired();
		}
		
	}

	@Override
	public ControlMode getControlMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCameraModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shutdownCamera() {
		// TODO Auto-generated method stub
		return null;
	}


}
