package org.olyapp.sdk.test;

import org.junit.Test;
import org.olyapp.sdk.CameraAPI;
import org.olyapp.sdk.impl.ICameraAPI;

public class TestCameraInfo {

	@Test
	public void test() {
		CameraAPI cameraAPI = new ICameraAPI();
		System.out.println(cameraAPI.getCameraModel());
	}

}
