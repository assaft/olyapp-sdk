package org.olyapp.sdk.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olyapp.sdk.CameraProperty;
import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.Point;
import org.olyapp.sdk.FocusResult;
import org.olyapp.sdk.Frame;
import org.olyapp.sdk.FrameMetadata;
import org.olyapp.sdk.LiveView;
import org.olyapp.sdk.LiveViewShot;
import org.olyapp.sdk.ProtocolError;

public class ILiveView implements LiveView {

	private final static int WAIT_MS = 50;
	
	FrameMetadata lastFrameMetaData;
	
	public ILiveView(FrameMetadata firstFrameMetaData) {
		this.lastFrameMetaData = firstFrameMetaData;
	}
	
	@Override
	public Dimensions getDimensions() {
		return lastFrameMetaData.getDimensions();
	}

	@Override
	public FocusResult acquireFocus(Point fp) throws ProtocolError {
		return ICameraProtocol.getInst().acquireFocus(fp);
	}

	@Override
	public void releaseFocus() throws ProtocolError {
		ICameraProtocol.getInst().releaseFocus();		
	}
	
	@Override
	public Map<String,CameraProperty> getAllProperties() throws ProtocolError {
		return ICameraProtocol.getInst().getAllProperties();
	}

	@Override
	public CameraProperty getProperty(String propertyName) throws ProtocolError {
		return ICameraProtocol.getInst().getProperty(propertyName);
	}

	@Override
	public void setPropertyAsync(CameraProperty property) throws ProtocolError {
		ICameraProtocol.getInst().setProperty(property);
	}

	@Override
	public void setProperty(CameraProperty property) throws ProtocolError {
		setProperty(property,0);
	}

	@Override
	public void setProperty(CameraProperty property, int timeoutMS) throws ProtocolError {
		setProperties(Arrays.asList(property),timeoutMS);
	}
	
	@Override
	public void setProperties(List<CameraProperty> properties) throws ProtocolError {
		setProperties(properties,0);
	}

	@Override
	public void setProperties(List<CameraProperty> properties, int timeoutMS) throws ProtocolError {
		
		long startTime = System.currentTimeMillis();
		long endTime = timeoutMS==0 ? Long.MAX_VALUE : startTime + timeoutMS;
		
		setPropertiesAsync(properties);
		
		List<CameraProperty> propertiesToCheck = new ArrayList<CameraProperty>(properties);
		boolean stop = false;
		do {
			Map<String, CameraProperty> currentproperties = ICameraProtocol.getInst().getAllProperties();
			Iterator<CameraProperty> i = propertiesToCheck.iterator();
			while (i.hasNext()) {
				CameraProperty property = i.next();
				CameraProperty currentProperty = currentproperties.get(property.getPropName());
				if (currentProperty.getValue().equals(property.getValue())) {
					i.remove();
				}
			}

			if (propertiesToCheck.size()>0) {
				if (System.currentTimeMillis()+WAIT_MS>=endTime) {
					throw new ProtocolError("Failed to set parameters within requested timeout");
				} else { 
					try {
						Thread.sleep(WAIT_MS);
					} catch (InterruptedException e) {
						e.printStackTrace();
						throw new ProtocolError(e.getMessage());
					}
				}
			} else {
				stop = true;
			}
		} while (!stop);
	}
	
	@Override
	public Frame getNextFrame() throws ProtocolError {
		Frame newFrame = ICameraProtocol.getInst().getNextFrame();
		lastFrameMetaData = newFrame.getMetadata();
		return newFrame;
	}

	@Override
	public Frame getNextFrame(CameraProperty property) throws ProtocolError {
		return getNextFrame(Arrays.asList(property));
	}
	
	@Override
	public Frame getNextFrame(CameraProperty property, int timeoutMS) throws ProtocolError {
		return getNextFrame(Arrays.asList(property), timeoutMS);
	}
	
	@Override
	public Frame getNextFrame(List<CameraProperty> properties) throws ProtocolError {
		return getNextFrame(properties,0);
	}
	
	@Override
	public Frame getNextFrame(List<CameraProperty> properties, int timeoutMS) throws ProtocolError {
		setProperties(properties,timeoutMS);
		return getNextFrame();
	}

	@Override
	public LiveViewShot shoot() throws ProtocolError {
		return ICameraProtocol.getInst().shootLiveView(); 
	}

	@Override
	public void setPropertiesAsync(List<CameraProperty> properties) throws ProtocolError {
		for (CameraProperty property : properties) {
			ICameraProtocol.getInst().setProperty(property);
		}
	}

}
