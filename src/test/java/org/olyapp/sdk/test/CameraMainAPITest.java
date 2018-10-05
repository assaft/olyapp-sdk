package org.olyapp.sdk.test;

import org.junit.Before;
import org.junit.Test;
import org.olyapp.sdk.CameraMainAPI;
import org.olyapp.sdk.ProtocolError;
import org.olyapp.sdk.RemoteShutterAPI;

public class CameraMainAPITest {

	CameraMainAPI cameraAPI;
	
	@Before
	public void init() {
		cameraAPI = new CameraMainAPI();
	}
	
	@Test
	public void testCameraModel() throws ProtocolError {
		System.out.println(cameraAPI.getCameraModel());
	}

	@Test
	public void testConnectionMode() throws ProtocolError {
		System.out.println(cameraAPI.getConnectMode());
	}

	@Test
	public void testCommandList() throws ProtocolError {
		System.out.println(cameraAPI.getCommandList());
	}

	@Test
	public void testRemoteShutterMode() throws ProtocolError, InterruptedException {
		RemoteShutterAPI remoteShutterAPI = cameraAPI.setRemoteShutterMode();
		remoteShutterAPI.singleShot();
	}

	@Test
	public void testLiveViewMode320x240() throws ProtocolError {
		cameraAPI.setLiveViewMode(20000,1000,"0320x0240");
	}

	@Test
	public void testLiveViewMode640x480() throws ProtocolError {
		cameraAPI.setLiveViewMode(20000,1000,"0640x0480");
	}
	
	@Test 
	public void testLiveViewResolutions() throws ProtocolError {
		System.out.println(cameraAPI.getLiveViewResolutions());
	}

	@Test
	public void testPlayMode() throws ProtocolError {
		cameraAPI.setPlayMode();
	}
	
	@Test
	public void testShutdown() throws ProtocolError {
		cameraAPI.shutdown();
	}
	
}
