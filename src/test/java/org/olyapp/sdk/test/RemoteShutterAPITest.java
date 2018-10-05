package org.olyapp.sdk.test;

import org.junit.Before;
import org.junit.Test;
import org.olyapp.sdk.CameraMainAPI;
import org.olyapp.sdk.ProtocolError;
import org.olyapp.sdk.RemoteShutterAPI;

public class RemoteShutterAPITest {

	CameraMainAPI cameraMainAPI;
	RemoteShutterAPI remoteShutterAPI;

	@Before
	public void init() throws ProtocolError {
		cameraMainAPI = new CameraMainAPI();  
		remoteShutterAPI = cameraMainAPI.setRemoteShutterMode();
	}
	
	@Test
	public void switchToRemoteShutter() {
	}
	
	@Test
	public void firstPushRelease() throws ProtocolError, InterruptedException {
		remoteShutterAPI.firstPush();
		Thread.sleep(2000);
		remoteShutterAPI.firstRelease();
	}

	@Test 
	public void firstSecondPush() throws ProtocolError, InterruptedException {
		remoteShutterAPI.firstSecondPush();
	}

	@Test 
	public void secondFirstRelease() throws ProtocolError, InterruptedException {
		remoteShutterAPI.secondFirstRelease();
	}
	
	@Test 
	public void allSteps() throws ProtocolError {
		remoteShutterAPI.firstPush();
		remoteShutterAPI.secondPush();
		remoteShutterAPI.secondRelease();
		remoteShutterAPI.firstRelease();
	}

	public void shortSteps() throws ProtocolError {
		remoteShutterAPI.firstSecondPush();
		remoteShutterAPI.secondFirstRelease();
	}
	
	@Test
	public void singleShot() throws ProtocolError, InterruptedException {
		remoteShutterAPI.singleShot();
	}

	@Test
	public void multiShotAEFLocked() throws ProtocolError, InterruptedException {
		remoteShutterAPI.multiShot(3,500);
	}
	
	@Test
	public void multiShot() throws ProtocolError, InterruptedException {
		remoteShutterAPI.multiShot(3,500);
	}
	
}
