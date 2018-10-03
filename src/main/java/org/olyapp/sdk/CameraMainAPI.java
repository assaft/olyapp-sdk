package org.olyapp.sdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olyapp.sdk.utils.HTTPClient;

public class CameraMainAPI {
	public static final String DEF_CAMERA_IP = "192.168.0.10";

	public static final String GET_CAMERA_INFO = "get_caminfo.cgi";
	public static final String GET_CONNECT_MODE = "get_connectmode.cgi";
	public static final String GET_COMMAND_LIST = "get_commandlist.cgi";
	public static final String SWITCH_MODE = "switch_cammode.cgi";  // ?mode=play
	public static final String EXEC_POWEROFF = "exec_pwoff.cgi";
	
	public static final Pattern cameraModelPattern = Pattern.compile("<.?xml.*><caminfo><model>(.*)</model></caminfo>");
	public static final Pattern connectionModelPattern = Pattern.compile("<.?xml.*><connectmode>(.*)</connectmode>");
	
	public String getCameraModel() throws ProtocolError {
		String response = HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + GET_CAMERA_INFO);
		Matcher matcher = cameraModelPattern.matcher(response);
		if (!matcher.matches()) {
			throw new ProtocolError("Failed to retrieve camera model");
		}
		return matcher.group(1);
	}

	public String getConnectMode() throws ProtocolError {
		String response = HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + GET_CONNECT_MODE);
		Matcher matcher = connectionModelPattern.matcher(response);
		if (!matcher.matches()) {
			throw new ProtocolError("Failed to retrieve connection mode");
		}
		return matcher.group(1);
	}
	
	public String getCommandList() throws ProtocolError {
		return HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + GET_COMMAND_LIST);
	}

	public LiveViewAPI setLiveViewMode(String resolution) throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + SWITCH_MODE + "?mode=rec&lvqty=" + resolution);
		return new LiveViewAPI();
	}

	
	public RemoteShutterAPI setRemoteShutterMode() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + SWITCH_MODE + "?mode=shutter");
		return new RemoteShutterAPI();
	}

	public Play setPlayMode() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + SWITCH_MODE + "?mode=play");
		return null;
	}
	
	public void shutdown() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + EXEC_POWEROFF);
	}

}
