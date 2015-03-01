package org.olyapp.sdk.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.olyapp.sdk.CameraAPI;
import org.olyapp.sdk.impl.ICameraAPI;

public class TextConnectionMode {

	@Test
	public void test() {
		CameraAPI cameraAPI = new ICameraAPI();
		System.out.println(cameraAPI.getConnectionModel());
	}

}
