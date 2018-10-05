package org.olyapp.sdk.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olyapp.sdk.CameraMainAPI;
import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.LiveViewAPI;
import org.olyapp.sdk.LiveViewImageData;
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
	public void testLiveViewMode() throws ProtocolError {
		List<Dimensions> resolutions = cameraAPI.getLiveViewResolutions();
		cameraAPI.setLiveViewMode(20000,1000,resolutions.get(resolutions.size()-1));
	}
	
	@Test 
	public void testLiveViewModeAllResolutions() throws ProtocolError, InterruptedException {
		List<Dimensions> resolutions = cameraAPI.getLiveViewResolutions();
		for (Dimensions resolution : resolutions) {
			testLiveViewModeResolution(resolution);
		}
	}
	
	public void testLiveViewModeResolution(Dimensions resolution) throws ProtocolError, InterruptedException {
		LiveViewAPI liveViewAPI = cameraAPI.setLiveViewMode(20000,1000,resolution);
		List<LiveViewImageData> images = liveViewAPI.runLiveView(1, -1);
		LiveViewImageData image = images.get(0);
		Dimensions imageResolution = image.getMetadata().getDimensions();
		assertEquals(resolution, imageResolution);
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
