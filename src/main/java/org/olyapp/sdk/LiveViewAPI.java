package org.olyapp.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olyapp.sdk.FocusResult.FocusStatus;
import org.olyapp.sdk.utils.HTTPClient;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiveViewAPI {

	private static final Pattern propertyDescPattern = Pattern.compile("<.?xml.*><desc><propname>(.*)</propname><attribute>(.*)</attribute><value>(.*)</value>(?:<enum>(.*)</enum>)?</desc>");
	private static final Pattern focusPattern = Pattern.compile("<.?xml.*><response><affocus>(.*)</affocus>(?:<afframepoint>(.*)</afframepoint><afframesize>(.*)</afframesize>)?</response>");
	
	private static final String TAKE_MISC = "exec_takemisc.cgi";
	private static final String TAKE_MOTION = "exec_takemotion.cgi";

	private static final String GET_CAM_PROP = "get_camprop.cgi";
	private static final String SET_CAM_PROP = "set_camprop.cgi";
	
	public String getPropertyValue(Property property) throws ProtocolError {
		return getPropertyDesc(property).getValue();
	}

	public PropertyDesc getPropertyDesc(Property property) throws ProtocolError {
		String response = HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + GET_CAM_PROP + "?prop=desc&propname=" + property.getName());
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
		HTTPClient.getInstance().doPost("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + SET_CAM_PROP + "?prop=set&propname=" + property.getName(),request);
	}
	
	public void startLiveView(int port, LiveViewHandler handler, long timeout) throws ProtocolError, InterruptedException {
		try {
			LiveViewServer.getInstance().start(port, handler, timeout);
			HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP +  "/" + TAKE_MISC + "?com=startliveview&port="+port);
		} catch (Exception e) {
			LiveViewServer.getInstance().stop();
			throw new ProtocolError(e.getMessage());
		}
	}	

	public void stopLiveView() throws ProtocolError, InterruptedException {
		try {
			HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP +  "/" + TAKE_MISC + "?com=stopliveview");
		} finally {
			LiveViewServer.getInstance().stop();
		}
	}

	public LiveViewImageData runLiveViewForSingleImage(int port, long timeout) throws ProtocolError, InterruptedException {
		List<LiveViewImageData> images = runLiveView(port, timeout, 1, -1);
		return images.size()==1 ? images.get(0) : null;
	}

	public List<LiveViewImageData> runLiveView(int port, long timeout, int imageCount, long time) throws ProtocolError, InterruptedException {
		final List<LiveViewImageData> images = Lists.newArrayList();
		Lock lock = new ReentrantLock();
		Condition condition = lock.newCondition();
		lock.lock();
		try {
			startLiveView(port,new LiveViewHandler() {

				AtomicLong firstImageTime = new AtomicLong();
				AtomicBoolean notified = new AtomicBoolean(false);
				
				public void signal(String reason) {
					lock.lock();
					try {
						if (!notified.get()) {
							notified.set(true);
							condition.signal();
						}
					} finally {
						lock.unlock();
					}
				}
				
				@Override
				public void onTimeout(long ms) {
					signal("timeout expired");
				}
				
				@Override
				public void onImage(LiveViewImageData imageData) {
					long timeNow = System.currentTimeMillis();
					if (images.size()==0) {
						firstImageTime.set(timeNow);
					}
					if ((time==-1 || timeNow-firstImageTime.get()<time) && 
							(imageCount==-1 || images.size()<imageCount)) {
						images.add(imageData);
					} else {
						signal("live stream should be ended");
					}
				}
				
			}, timeout);
			
			condition.await();
			
		} finally {
			lock.unlock();
			stopLiveView();
		}
		
		return images;
	}

	public LiveViewShot takePicture() throws ProtocolError {
		String response = HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MOTION + "?com=starttake");
		return new LiveViewShot("",null);
				/*
				response.getTake(),
				response.getAffocus().equals("ok")
					? new IFocusResult.IFocusOK(
							IPoint.parse(response.getAfframepoint()),
							IDimension.parse(response.getAfframesize()))
					: new IFocusResult.IFocusError(response.getAffocus()) */

		
	}

	public byte[] requestSmallSizeJpeg() throws ProtocolError {
		return HTTPClient.getInstance().doGetBinary("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MISC + "?com=getrecview");
	}
	
	public byte[] requestFullSizeJpeg() throws ProtocolError {
		return HTTPClient.getInstance().doGetBinary("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MISC + "?com=getlastjpg");
	}
	
	public FocusResult acquireFocus(int x, int y) throws ProtocolError {
		if (!LiveViewServer.getInstance().isLiveStreamOpen()) {
			throw new ProtocolError("Live-view must be open to acquire focus");			
		}
		
		String response = HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MOTION + 
				"?com=assignafframe&point="+
				String.format("%04d", x) + "x" +
				String.format("%04d", y));
		Matcher matcher = focusPattern.matcher(response);
		if (!matcher.matches()) {
			throw new ProtocolError("Failed to handle the response of focus acquisition: [" + response + "]");
		}

		String focusStatusStr = matcher.group(1);
		FocusStatus focusStatus;
		if (focusStatusStr.equals("ok")) {
			focusStatus = FocusStatus.OK;
		} else if (focusStatusStr.equals("ng")) {
			focusStatus = FocusStatus.FAILED;
		} else if (focusStatusStr.equals("none")) {
			focusStatus = FocusStatus.DISABLED;
		} else {
			throw new ProtocolError("Failed to handle the response of focus acquisition: [" + response + "] (unexpected status)");
		}

		Optional<Integer> focusFrameTopLeftX;
		Optional<Integer> focusFrameTopLeftY;
		Optional<Integer> focusFrameWidth;
		Optional<Integer> focusFrameHeight;
		
		if (focusStatus==FocusStatus.OK) {
			if (matcher.groupCount()==3) {
				String[] coordinates = matcher.group(2).split("x");
				String[] frameSize = matcher.group(3).split("x");
				focusFrameTopLeftX 	= Optional.of(Integer.parseInt(coordinates[0]));
				focusFrameTopLeftY 	= Optional.of(Integer.parseInt(coordinates[1]));
				focusFrameWidth 	= Optional.of(Integer.parseInt(frameSize[0]));
				focusFrameHeight 	= Optional.of(Integer.parseInt(frameSize[1]));
			} else {
				throw new ProtocolError("Failed to handle the response of focus acquisition: [" + response + "] (unexpected group count)");
			}
		} else {
			focusFrameTopLeftX  = Optional.ofNullable(null);
			focusFrameTopLeftY  = Optional.ofNullable(null);
			focusFrameWidth  	= Optional.ofNullable(null);
			focusFrameHeight 	= Optional.ofNullable(null);
		}
		return new FocusResult(focusStatus,
				focusFrameTopLeftX,focusFrameTopLeftY,
				focusFrameWidth,focusFrameHeight);
	}
	
	public void releaseFocus() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MOTION + "?com=releaseafframe");
	}

}
