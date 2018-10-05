package org.olyapp.sdk.test;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.olyapp.sdk.CameraMainAPI;
import org.olyapp.sdk.LiveViewAPI;
import org.olyapp.sdk.Property;
import org.olyapp.sdk.ProtocolError;
import org.olyapp.sdk.utils.StringUtils;

import com.google.common.collect.Maps;

public class LearningAPITest {

	CameraMainAPI cameraMainAPI; 
	
	@Before
	public void init() throws ProtocolError {
		cameraMainAPI = new CameraMainAPI();  
	}

	public byte[] getMetadata(LiveViewAPI liveViewAPI, Property property, String value) throws ProtocolError, InterruptedException {
		// set the parameter 
		liveViewAPI.setProperty(property,value);
		
		// get an image and return its metadata buffer
		return liveViewAPI.runLiveViewForSingleImage().getMetadata().getBuffer();
	}
	
	@Test 
	public void findExpComp() throws ProtocolError, InterruptedException {
		LiveViewAPI liveViewAPI = cameraMainAPI.setLiveViewMode(20000,1000,"0x640x0480");

		Map<String,byte[]> metadata = Maps.newLinkedHashMap();
		
		/*
		//liveViewAPI.setProperty(Property.TAKE_MODE,"A");
		metadata.put("exp-comp 0", 		getMetadata(liveViewAPI, Property.EXP_COMP, "0"));
		metadata.put("exp-comp +3.7", 	getMetadata(liveViewAPI, Property.EXP_COMP, "+3.7"));
		metadata.put("exp-comp -4.3", 	getMetadata(liveViewAPI, Property.EXP_COMP, "-4.3"));
		metadata.put("exp-comp 0", 		getMetadata(liveViewAPI, Property.EXP_COMP, "0"));
		*/

		/*
		liveViewAPI.setProperty(Property.TAKE_MODE,"M");
		metadata.put("shutter speed 1/40", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "40"));
		metadata.put("shutter speed 1/250", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "250"));
		metadata.put("shutter speed 1/4000", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "4000"));
		metadata.put("shutter speed 1/1.6", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "1.6"));
		metadata.put("shutter speed 1/1", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "1\""));
		metadata.put("shutter speed 20/1", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "20\""));
		metadata.put("shutter speed livebulb", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "livebulb"));
		metadata.put("shutter speed livetime", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "livetime"));
		metadata.put("shutter speed 1/40-2", 	getMetadata(liveViewAPI, Property.SHUTTER_SPEED_VALUE, "40"));
		 */
		
		metadata.put("auto-wb",		getMetadata(liveViewAPI, Property.WB_VALUE, ""));

		
		String lastName = null;
		byte[] lastBuffer = null;
		for (Entry<String,byte[]> entry : metadata.entrySet()) {
			String currentName = entry.getKey();
			byte[] currentBuffer = entry.getValue();
			if (lastBuffer!=null) {
				System.out.println(StringUtils.toHex(0) + ":" + lastName + " ; " + currentName);

				for (int i=0 ; i<currentBuffer.length ; i++) {
					int currentValue = currentBuffer[i];
					int lastValue = lastBuffer[i];
					if (currentValue != lastValue) {
						System.out.println(StringUtils.toHex(i) + ":" + StringUtils.toHex(lastValue) + " ; " + StringUtils.toHex(currentValue));
					}
				}
			}
			lastName = currentName;
			lastBuffer = currentBuffer;
		}
	}
	

}
