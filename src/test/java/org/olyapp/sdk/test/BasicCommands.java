package org.olyapp.sdk.test;

import org.junit.Test;
import org.olyapp.sdk.CameraAPI;
import org.olyapp.sdk.impl.ICameraAPI;

public class BasicCommands {

	@Test
	public void testCameraModel() {
		CameraAPI cameraAPI = new ICameraAPI();
		System.out.println(cameraAPI.getCameraModel());
	}

	@Test
	public void testConnectionMode() {
		CameraAPI cameraAPI = new ICameraAPI();
		System.out.println(cameraAPI.getConnectionModel());
	}

	@Test
	public void testCommandList() {
		CameraAPI cameraAPI = new ICameraAPI();
		System.out.println(cameraAPI.getCommandList());
	}
	
	@Test
	public void testDesc() {
		CameraAPI cameraAPI = new ICameraAPI();
		System.out.println(cameraAPI.getPropDesc());
	}
}
