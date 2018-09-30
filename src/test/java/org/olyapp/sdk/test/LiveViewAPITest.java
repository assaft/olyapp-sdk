package org.olyapp.sdk.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olyapp.sdk.CameraMainAPI;
import org.olyapp.sdk.LiveViewAPI;
import org.olyapp.sdk.Property;
import org.olyapp.sdk.PropertyDesc;
import org.olyapp.sdk.ProtocolError;

public class LiveViewAPITest {
	CameraMainAPI cameraMainAPI;
	LiveViewAPI liveViewAPI;

	@Before
	public void init() throws ProtocolError {
		cameraMainAPI = new CameraMainAPI();  
		liveViewAPI = cameraMainAPI.setLiveViewMode();
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

	
}
