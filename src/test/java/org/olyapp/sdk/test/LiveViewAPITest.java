package org.olyapp.sdk.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olyapp.sdk.CameraMainAPI;
import org.olyapp.sdk.Coordinate;
import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.FocusResult;
import org.olyapp.sdk.ImageResult;
import org.olyapp.sdk.LiveViewAPI;
import org.olyapp.sdk.LiveViewHandler;
import org.olyapp.sdk.LiveViewImageData;
import org.olyapp.sdk.Property;
import org.olyapp.sdk.PropertyDesc;
import org.olyapp.sdk.ProtocolError;
import org.olyapp.sdk.TakeResult;
import org.olyapp.sdk.TakeResult.TakeStatus;
import org.olyapp.sdk.utils.StringUtils;

public class LiveViewAPITest {
	CameraMainAPI cameraMainAPI;
	LiveViewAPI liveViewAPI;

	@Before
	public void init() throws ProtocolError {
		cameraMainAPI = new CameraMainAPI();  
		cameraMainAPI.setPlayMode();
		liveViewAPI = cameraMainAPI.setLiveViewMode(20000,1000,new Dimensions(640,480));
		liveViewAPI.setProperty(Property.DRIVE_MODE, "lowvib-normal");
	}
	
	@Test
	public void getAllDescsTest() throws ProtocolError, InterruptedException {
		for (Property property : Property.values()) {
			if (!property.isSuperProperty()) {
				System.out.println(liveViewAPI.getPropertyDesc(property));
			}
		}
	}

	@Test
	public void getAllValuesTest() throws ProtocolError, InterruptedException {
		for (Property property : Property.values()) {
			if (!property.isSuperProperty()) {
				System.out.println(liveViewAPI.getPropertyValue(property));
			}
		}
	}

	@Test
	public void getSetRevertValuesTest() throws ProtocolError, InterruptedException {
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
		liveViewAPI.startLiveView(new LiveViewHandler() {
			
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
			
		});
		Thread.sleep(5000);
		liveViewAPI.stopLiveView();
	}

	@Test
	public void runLiveStreamTest1() throws ProtocolError, InterruptedException {
		long startTime = System.currentTimeMillis();
		List<LiveViewImageData> images = liveViewAPI.runLiveView(-1, 5000); 
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
		List<LiveViewImageData> images = liveViewAPI.runLiveView(80, -1);
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
		List<LiveViewImageData> images = liveViewAPI.runLiveView(1, -1); 
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
		liveViewAPI.startLiveView(new LiveViewHandler() {
			@Override
			public void onTimeout(long ms) {
				System.out.println("timeout expired");
			}
			
			@Override
			public void onImage(LiveViewImageData imageData) {
				System.out.println(Thread.currentThread().getId() + " - Image consumed: " + StringUtils.toHex(imageData.getImageId()));
			}
			
		});
		Thread.sleep(1000);
		FocusResult focusResult = liveViewAPI.acquireFocus(new Coordinate(40, 40));
		liveViewAPI.releaseFocus();
		liveViewAPI.stopLiveView();
		System.out.println(focusResult);
	}

	@Test
	public void takePictureTest() throws ProtocolError, InterruptedException, IOException {
		liveViewAPI.startLiveView(new LiveViewHandler() {
			@Override
			public void onTimeout(long ms) {
				System.out.println("timeout expired");
			}
			
			@Override
			public void onImage(LiveViewImageData imageData) {
				System.out.println("Image consumed: " + StringUtils.toHex(imageData.getImageId()));
			}
		});
		Thread.sleep(1000);
		TakeResult takeResult = liveViewAPI.takePicture();
		System.out.println("Picture-taking result: " + takeResult);
		liveViewAPI.stopLiveView();
		System.out.println("getting small jpeg");
		Files.write(Paths.get("test_small.jpg"), liveViewAPI.requestLastTakenSmallSizeJpeg());
		System.out.println("getting big jpeg");
		Files.write(Paths.get("test_big.jpg"), liveViewAPI.requestLastTakenFullSizeJpeg());
		System.out.println("done");
	}
	
	@Test
	public void takeSmallSizeJpegTest() throws ProtocolError, InterruptedException, IOException {
		ImageResult imageResult = liveViewAPI.takeSmallSizeJpeg();
		if (imageResult.getTakeResult().getTakeStatus()==TakeStatus.OK) {
			Files.write(Paths.get("test_take_small.jpg"), imageResult.getImage());
			System.out.println(imageResult.getTakeResult());
		} else {
			System.err.println("Failed to take picture: " + imageResult.getTakeResult());
		}
	}
	
	@Test
	public void takeFullSizeJpegTest() throws ProtocolError, InterruptedException, IOException {
		ImageResult imageResult = liveViewAPI.takeFullSizeJpeg();
		if (imageResult.getTakeResult().getTakeStatus()==TakeStatus.OK) {
			Files.write(Paths.get("test_take_big.jpg"), imageResult.getImage());
			System.out.println(imageResult.getTakeResult());
		} else {
			System.err.println("Failed to take picture: " + imageResult.getTakeResult());
		}
	}

}
