package org.olyapp.sdk.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olyapp.sdk.CameraMainAPI;
import org.olyapp.sdk.FocusResult;
import org.olyapp.sdk.LiveViewAPI;
import org.olyapp.sdk.LiveViewHandler;
import org.olyapp.sdk.LiveViewImageData;
import org.olyapp.sdk.Property;
import org.olyapp.sdk.PropertyDesc;
import org.olyapp.sdk.ProtocolError;
import org.olyapp.sdk.utils.StringUtils;

public class LiveViewAPITest {
	CameraMainAPI cameraMainAPI;
	LiveViewAPI liveViewAPI;

	@Before
	public void init() throws ProtocolError {
		cameraMainAPI = new CameraMainAPI();  
		cameraMainAPI.setPlayMode();
		liveViewAPI = cameraMainAPI.setLiveViewMode("0640x0480");
	}
	
	@Test
	public void getAllDesc() throws ProtocolError, InterruptedException {
		for (Property property : Property.values()) {
			System.out.println(liveViewAPI.getPropertyDesc(property));
		}
	}

	@Test
	public void getAllValues() throws ProtocolError, InterruptedException {
		for (Property property : Property.values()) {
			System.out.println(liveViewAPI.getPropertyValue(property));
		}
	}

	@Test
	public void getSetRevertValues() throws ProtocolError, InterruptedException {
		for (Property property : Property.values()) {
			PropertyDesc desc = liveViewAPI.getPropertyDesc(property);
			if (desc.getType().contains("set")) {
				String originalValue = desc.getValue();
				List<String> values = desc.getValues();
				int id = values.indexOf(originalValue);
				int newId = ((id+1) % values.size());
				String newValue = values.get(newId);
				Assert.assertFalse(newValue.equals(originalValue));
	
				liveViewAPI.setProperty(property, newValue);
				String updatedValue = liveViewAPI.getPropertyValue(property);
				Assert.assertTrue(newValue.equals(updatedValue));
	
				liveViewAPI.setProperty(property, originalValue);
				String revertedValue = liveViewAPI.getPropertyValue(property);
				Assert.assertTrue(originalValue.equals(revertedValue));
			}
		}
	}

	@Test
	public void startStopLiveStreamTest() throws ProtocolError, InterruptedException {
		liveViewAPI.startLiveView(22000, new LiveViewHandler() {
			
			@Override
			public void onTimeout(long ms) {
				System.out.println("timeout");
			}
			
			@Override
			public void onImage(LiveViewImageData imageData) {
				try {
					Files.write(Paths.get(imageData.getImageId() + ".jpg"),imageData.getData());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		},1000);
		Thread.sleep(20000);
		liveViewAPI.stopLiveView();
	}

	@Test
	public void runLiveStreamTest1() throws ProtocolError, InterruptedException {
		long startTime = System.currentTimeMillis();
		List<LiveViewImageData> images = liveViewAPI.runLiveView(20000, 1000, -1, 5000); 
		long endTime = System.currentTimeMillis();
		System.out.println("Total: " + images.size() + " images in " + (endTime-startTime) + " ms");
		images.forEach(imageData->{
			try {
				Files.write(Paths.get(imageData.getImageId() + ".jpg"),imageData.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Test
	public void runLiveStreamTest2() throws ProtocolError, InterruptedException {
		long startTime = System.currentTimeMillis();
		List<LiveViewImageData> images = liveViewAPI.runLiveView(20000, 1000, 80, -1);
		long endTime = System.currentTimeMillis();
		System.out.println("Total: " + images.size() + " images in " + (endTime-startTime) + " ms");
		images.forEach(imageData->{
			try {
				Files.write(Paths.get(imageData.getImageId() + ".jpg"),imageData.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	@Test
	public void runLiveStreamTest3() throws ProtocolError, InterruptedException {
		long startTime = System.currentTimeMillis();
		List<LiveViewImageData> images = liveViewAPI.runLiveView(20000, 1000, 1, -1); 
		long endTime = System.currentTimeMillis();
		System.out.println("Total: " + images.size() + " images in " + (endTime-startTime) + " ms");
		images.forEach(imageData->{
			try {
				Files.write(Paths.get(imageData.getImageId() + ".jpg"),imageData.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	

	@Test
	public void focusTest() throws ProtocolError, InterruptedException {
		AtomicReference<FocusResult> focusResult = new AtomicReference<>();
		liveViewAPI.startLiveView(22000, new LiveViewHandler() {
			@Override
			public void onTimeout(long ms) {
				System.out.println("timeout");
			}
			
			@Override
			public void onImage(LiveViewImageData imageData) {
				System.out.println(Thread.currentThread().getId() + " - Image consumed: " + StringUtils.toHex(imageData.getImageId()));
			}
			
		}, 1000);
		Thread.sleep(1000);
		focusResult.set(liveViewAPI.acquireFocus(327, 213));
		liveViewAPI.releaseFocus();
		liveViewAPI.stopLiveView();
		System.out.println(focusResult.get());
	}

	
	@Test
	public void takePicture() throws ProtocolError, InterruptedException, IOException {
		liveViewAPI.startLiveView(22000, new LiveViewHandler() {
			@Override
			public void onTimeout(long ms) {
			}
			
			@Override
			public void onImage(LiveViewImageData imageData) {
				System.out.println(Thread.currentThread().getId() + " - Image consumed: " + StringUtils.toHex(imageData.getImageId()));
			}
		},1000);
		Thread.sleep(1000);
		liveViewAPI.takePicture();
		Files.write(Paths.get("test_small.jpg"), liveViewAPI.requestSmallSizeJpeg());
		Files.write(Paths.get("test_big.jpg"), liveViewAPI.requestFullSizeJpeg());
		liveViewAPI.stopLiveView();
	}

}
