package org.olyapp.sdk;

import java.util.List;
import java.util.Map;

public interface LiveView {
	
	Dimensions getDimensions();
	
	Map<String,CameraProperty> getAllProperties() throws ProtocolError;
	CameraProperty getProperty(String propertyName) throws ProtocolError;

	void setPropertyAsync(CameraProperty property) throws ProtocolError;
	void setPropertiesAsync(List<CameraProperty> properties) throws ProtocolError;

	void setProperty(CameraProperty property) throws ProtocolError;
	void setProperty(CameraProperty property, int timeoutMS) throws ProtocolError;
	void setProperties(List<CameraProperty> properties) throws ProtocolError;
	void setProperties(List<CameraProperty> properties, int timeoutMS) throws ProtocolError;
	
	Frame getNextFrame() throws ProtocolError;
	Frame getNextFrame(CameraProperty property) throws ProtocolError;
	Frame getNextFrame(CameraProperty property, int timeoutMS) throws ProtocolError;
	Frame getNextFrame(List<CameraProperty> properties) throws ProtocolError;
	Frame getNextFrame(List<CameraProperty> properties, int timeoutMS) throws ProtocolError;

	FocusResult acquireFocus(Point fp) throws ProtocolError;
	void releaseFocus() throws ProtocolError;
	
	LiveViewShot shoot() throws ProtocolError;
	
}
