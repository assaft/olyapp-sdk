package org.olyapp.sdk;

import org.olyapp.sdk.utils.HTTPClient;

import lombok.Synchronized;

public class RemoteShutterAPI {

	public static final String EXEC_SHUTTER = "exec_shutter.cgi";

	@Synchronized
	public void firstPush() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=1stpush");
	}
	
	@Synchronized
	public void secondPush() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=2ndpush");
	}
	
	@Synchronized
	public void firstSecondPush() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=1st2ndpush");
	}
	
	@Synchronized
	public void firstRelease() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=1strelease");
	}
	
	@Synchronized
	public void secondRelease() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=2ndrelease");
	}
	
	@Synchronized
	public void secondFirstRelease() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=2nd1strelease");
	}
	
	@Synchronized
	public void singleShot() throws ProtocolError {
		firstSecondPush();
		secondFirstRelease();
	}
	
	@Synchronized
	public void multiShotAEFLocked(int n) throws ProtocolError, InterruptedException {
		multiShot(n,1000);
	}

	@Synchronized
	public void multiShotAEFLocked(int n, int delay) throws ProtocolError, InterruptedException {
		firstPush();
		for (int i=0 ; i<n ; i++) {
			secondPush();
			secondRelease();
			Thread.sleep(delay);
		}
		firstRelease();
	}

	@Synchronized
	public void multiShot(int n, int delay) throws ProtocolError, InterruptedException {
		for (int i=0 ; i<n ; i++) {
			singleShot();
			Thread.sleep(delay);
		}
	}

}
