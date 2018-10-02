package org.olyapp.sdk;

import org.olyapp.sdk.utils.HTTPClient;

public class RemoteShutterAPI {

	public static final String EXEC_SHUTTER = "exec_shutter.cgi";

	public void firstPush() throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=1stpush");
	}
	
	public void secondPush() throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=2ndpush");
	}
	
	public void firstSecondPush() throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=1st2ndpush");
	}
	
	public void firstRelease() throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=1strelease");
	}
	
	public void secondRelease() throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=2ndrelease");
	}
	
	public void secondFirstRelease() throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + EXEC_SHUTTER + "?com=2nd1strelease");
	}

	public void singleShot() throws ProtocolError {
		firstSecondPush();
		secondFirstRelease();
	}
	
	public void multiShot(int n) throws ProtocolError, InterruptedException {
		multiShot(n,1000);
	}

	public void multiShot(int n, int delay) throws ProtocolError, InterruptedException {
		firstPush();
		for (int i=0 ; i<n ; i++) {
			secondPush();
			secondRelease();
			Thread.sleep(delay);
		}
		firstRelease();
	}
	
}
