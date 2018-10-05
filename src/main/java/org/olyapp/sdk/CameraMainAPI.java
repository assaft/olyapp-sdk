package org.olyapp.sdk;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olyapp.sdk.utils.HTTPClient;

import com.google.common.collect.Lists;

import lombok.Synchronized;

public class CameraMainAPI {
	public static final String DEF_CAMERA_IP = "192.168.0.10";

	public static final String GET_CAMERA_INFO = "get_caminfo.cgi";
	public static final String GET_CONNECT_MODE = "get_connectmode.cgi";
	public static final String GET_COMMAND_LIST = "get_commandlist.cgi";
	public static final String SWITCH_MODE = "switch_cammode.cgi";  // ?mode=play
	public static final String EXEC_POWEROFF = "exec_pwoff.cgi";
	
	public static final Pattern cameraModelPattern = Pattern.compile("<.?xml.*><caminfo><model>(.*)</model></caminfo>");
	public static final Pattern connectionModelPattern = Pattern.compile("<.?xml.*><connectmode>(.*)</connectmode>");
	public static final Pattern resolutionsMainPattern = Pattern.compile("<param\\d name=\"rec\"><cmd\\d name=\"lvqty\">((?:<param\\d name=\"\\d+x\\d+\"/>)+)</cmd\\d></param\\d>");
	public static final Pattern resolutionsSubPattern = Pattern.compile("<param\\d name=\"(\\d+x\\d+)\"/>");
	public static final Pattern whiteSpacePattern = Pattern.compile(">\\s+<");
	
	@Synchronized
	public String getCameraModel() throws ProtocolError {
		String response = HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + GET_CAMERA_INFO);
		Matcher matcher = cameraModelPattern.matcher(response);
		if (!matcher.matches()) {
			throw new ProtocolError("Failed to retrieve camera model; response: " + response);
		}
		return matcher.group(1);
	}
	
	@Synchronized
	public String getConnectMode() throws ProtocolError {
		String response = HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + GET_CONNECT_MODE);
		Matcher matcher = connectionModelPattern.matcher(response);
		if (!matcher.matches()) {
			throw new ProtocolError("Failed to retrieve connection mode");
		}
		return matcher.group(1);
	}
	
	@Synchronized
	public String getCommandList() throws ProtocolError {
		return HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + GET_COMMAND_LIST);
	}

	@Synchronized
	public List<Dimensions> getLiveViewResolutions() throws ProtocolError {
		List<Dimensions> result = Lists.newArrayList();
		String response = HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + GET_COMMAND_LIST);
		response = whiteSpacePattern.matcher(response).replaceAll("><");
		Matcher matcher = resolutionsMainPattern.matcher(response);
		if (matcher.find()) {
			String resolutionsTags = matcher.group(1);
			matcher = resolutionsSubPattern.matcher(resolutionsTags);
			while (matcher.find()) {
				String[] parts = matcher.group(1).split("x");
				result.add(new Dimensions(
						Integer.parseInt(parts[0]), 
						Integer.parseInt(parts[1])));
			}
		} else {
			throw new ProtocolError("Failed to extract live view resolutions; response: [" + response + "]");
		}
		return result;
	}

	@Synchronized
	public LiveViewAPI setLiveViewMode(int port, long timeout, Dimensions resolution) throws ProtocolError {
		// due to a bug in the E-M10 m1 camera firmware, it is 
		// necessary to set PLAY mode before setting Live View 
		// mode, otherwise the resolution parameter is ignored.
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + SWITCH_MODE + "?mode=play");
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + SWITCH_MODE + "?mode=rec&lvqty=" + 
				String.format("%04d", resolution.getWidth()) + "x" +
				String.format("%04d", resolution.getHeight()));
		return new LiveViewAPI(port,timeout);
	}

	@Synchronized
	public RemoteShutterAPI setRemoteShutterMode() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + SWITCH_MODE + "?mode=shutter");
		return new RemoteShutterAPI();
	}

	@Synchronized
	public Play setPlayMode() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + SWITCH_MODE + "?mode=play");
		return null;
	}
	
	@Synchronized
	public void shutdown() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + DEF_CAMERA_IP + "/" + EXEC_POWEROFF);
	}

}
