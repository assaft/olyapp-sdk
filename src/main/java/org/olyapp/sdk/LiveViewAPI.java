package org.olyapp.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiveViewAPI {

	public static final Pattern propertyDescPattern = Pattern.compile("<.?xml.*><desc><propname>(.*)</propname><attribute>(.*)</attribute><value>(.*)</value>(?:<enum>(.*)</enum>)?</desc>");
	
	private static final String TAKE_MISC = "exec_takemisc";
	private static final String TAKE_MOTION = "exec_takemotion.cgi";

	public static final String GET_CAM_PROP = "get_camprop.cgi";
	public static final String SET_CAM_PROP = "set_camprop.cgi";
	
	public String getPropertyValue(Property property) throws ProtocolError {
		return getPropertyDesc(property).getValue();
	}

	public PropertyDesc getPropertyDesc(Property property) throws ProtocolError {
		String response = HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + GET_CAM_PROP + "?prop=desc&propname=" + property.getName());
		Matcher matcher = propertyDescPattern.matcher(response);
		if (!matcher.matches()) {
			throw new ProtocolError("Failed to retrieve camera property description for [" + property + "]");
		}
		String type = matcher.group(2);
		String value = matcher.group(3);
		List<String> values = matcher.group(4)!=null ? Arrays.asList(matcher.group(4).split(" ")) : new ArrayList<>();
		return new PropertyDesc(property, value, type, values);
	}

	public void setProperty(Property property, String value) throws ProtocolError {
		String request = "<?xml version=\"1.0\"?><set><value>"+value+"</value></set>";
		HTTPClient.doPost("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + SET_CAM_PROP + "?prop=set&propname=" + property.getName(),request);
	}
	
	public void startLiveView(int port) throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP+TAKE_MISC + "?com=startliveview&port="+port);
	}	

	public void stopLiveView() throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + TAKE_MISC + "?com=stopliveview");
	}	
	
	
	void acquireFocus(int x, int y) throws ProtocolError {
		String response = HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + TAKE_MOTION + 
				"?com=assignafframe&point="+
				String.format("%010d", x) + "x" +
				String.format("%010d", y));
		System.out.println(response);
/*
		return response.getAffocus().equals("ok")
				? new IFocusResult.IFocusOK(
						IPoint.parse(response.getAfframepoint()),
						IDimension.parse(response.getAfframesize()))
				: new IFocusResult.IFocusError(response.getAffocus());*/

	}
	
	void releaseFocus() throws ProtocolError {
		HTTPClient.doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + TAKE_MOTION + "?com=releaseafframe");
	}
	
	//LiveViewShot shoot() throws ProtocolError;

}
