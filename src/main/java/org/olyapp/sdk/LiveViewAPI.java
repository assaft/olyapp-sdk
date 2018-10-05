package org.olyapp.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olyapp.sdk.FocusResult.FocusStatus;
import org.olyapp.sdk.TakeResult.TakeStatus;
import org.olyapp.sdk.utils.HTTPClient;

import com.google.common.collect.Lists;

import lombok.Synchronized;

public class LiveViewAPI {

	private static final Pattern propertyDescPattern = Pattern.compile("<.?xml.*><desc><propname>(.*)</propname><attribute>(.*)</attribute><value>(.*)</value>(?:<enum>(.*)</enum>)?</desc>");
	private static final Pattern focusPattern = Pattern.compile("<.?xml.*><response><affocus>(.*)</affocus>(?:<afframepoint>(.*)</afframepoint><afframesize>(.*)</afframesize>)?</response>");
	private static final Pattern shotPattern = Pattern.compile("<.?xml.*><response><take>(.*)</take><affocus>(.*)</affocus>(?:<afframepoint>(.*)</afframepoint><afframesize>(.*)</afframesize>)?</response>");
	
	private static final String TAKE_MISC = "exec_takemisc.cgi";
	private static final String TAKE_MOTION = "exec_takemotion.cgi";

	private static final String GET_CAM_PROP = "get_camprop.cgi";
	private static final String SET_CAM_PROP = "set_camprop.cgi";
	
	private final int port;
	private final long timeout;
	
	public LiveViewAPI(int port, long timeout) {
		this.port = port;
		this.timeout = timeout;
	}
	
	@Synchronized
	public String getPropertyValue(Property property) throws ProtocolError {
		return getPropertyDesc(property).getValue();
	}

	@Synchronized
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

	@Synchronized
	public void setProperty(Property property, String value) throws ProtocolError {
		String request = "<?xml version=\"1.0\"?><set><value>"+value+"</value></set>";
		HTTPClient.getInstance().doPost("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + SET_CAM_PROP + "?prop=set&propname=" + property.getName(),request);
	}
	
	@Synchronized
	public void startLiveView(LiveViewHandler handler) throws ProtocolError, InterruptedException {
		try {
			LiveViewServer.getInstance().start(port, handler, timeout);
			HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP +  "/" + TAKE_MISC + "?com=startliveview&port="+port);
		} catch (Exception e) {
			LiveViewServer.getInstance().stop();
			throw new ProtocolError(e.getMessage());
		}
	}	

	@Synchronized
	public void stopLiveView() throws ProtocolError, InterruptedException {
		try {
			HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP +  "/" + TAKE_MISC + "?com=stopliveview");
		} finally {
			LiveViewServer.getInstance().stop();
		}
	}

	@Synchronized
	public LiveViewImageData runLiveViewForSingleImage() throws ProtocolError, InterruptedException {
		List<LiveViewImageData> images = runLiveView(1, -1);
		return images.size()==1 ? images.get(0) : null;
	}

	@Synchronized
	public List<LiveViewImageData> runLiveView(int imageCount, long time) throws ProtocolError, InterruptedException {
		final List<LiveViewImageData> images = Lists.newArrayList();
		Lock lock = new ReentrantLock();
		Condition condition = lock.newCondition();
		lock.lock();
		try {
			
			final AtomicBoolean timeoutExpired = new AtomicBoolean(false);
			
			startLiveView(new LiveViewHandler() {

				long firstImageTime = 0;
				boolean notified = false;
				
				public void signal() {
					lock.lock();
					try {
						if (!notified) {
							notified = true;
							condition.signal();
						}
					} finally {
						lock.unlock();
					}
				}
				
				@Override
				public void onTimeout(long ms) {
					timeoutExpired.set(true);
					signal();
				}
				
				@Override
				public void onImage(LiveViewImageData imageData) {
					long timeNow = System.currentTimeMillis();
					if (images.size()==0) {
						firstImageTime = timeNow;
					}
					if ((time==-1 || timeNow-firstImageTime<time) && 
							(imageCount==-1 || images.size()<imageCount)) {
						images.add(imageData);
					} else {
						signal();
					}
				}
				
			});
			
			condition.await();
			
			if (timeoutExpired.get()) {
				throw new ProtocolError("Timeout expired");
			} 
			
		} finally {
			lock.unlock();
			stopLiveView();
		}
		
		return images;
	}

	@Synchronized
	public FocusResult acquireFocus(int x, int y) throws ProtocolError {
		if (!LiveViewServer.getInstance().isLiveStreamOpen()) {
			throw new ProtocolError("Live-view must be open to acquire focus");			
		}
		
		String response = HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MOTION + 
				"?com=assignafframe&point="+
				String.format("%04d", x) + "x" +
				String.format("%04d", y));
		try {
			Matcher matcher = focusPattern.matcher(response);
			if (!matcher.matches()) {
				throw new ProtocolError("regex unmatched");
			}
			
			String focusStatusStr = matcher.group(1);
			FocusStatus focusStatus = parseFocusStatus(focusStatusStr);
			String coordinatesStr;
			String frameSizeStr;
			if (matcher.groupCount()==3) {
				coordinatesStr = matcher.group(2);
				frameSizeStr = matcher.group(3);
			} else {
				coordinatesStr = null;
				frameSizeStr = null;
			}
			
			return parseFocusResult(focusStatus, coordinatesStr, frameSizeStr);
		} catch (Exception e) {
			throw new ProtocolError("Failed to handle the response of focus acquisition: [" + response + "] - " + e.getMessage());
		}
	}
	
	@Synchronized
	public void releaseFocus() throws ProtocolError {
		HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MOTION + "?com=releaseafframe");
	}
	
	@Synchronized
	public TakeResult takePicture() throws ProtocolError {
		String response = HTTPClient.getInstance().doGet("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MOTION + "?com=starttake");
		try {
			Matcher matcher = shotPattern.matcher(response);
			if (!matcher.matches()) {
				throw new ProtocolError("regex unmatched");
			}
			String takeStatusStr = matcher.group(1);
			String focusStatusStr = matcher.group(2);
			TakeStatus takeStatus = parseTakeStatus(takeStatusStr);
			FocusStatus focusStatus = parseFocusStatus(focusStatusStr);
			String coordinatesStr;
			String frameSizeStr;
			if (matcher.groupCount()==4) {
				coordinatesStr = matcher.group(3);
				frameSizeStr = matcher.group(4);
			} else {
				coordinatesStr = null;
				frameSizeStr = null;
			}
			FocusResult focusResult = parseFocusResult(focusStatus,coordinatesStr,frameSizeStr);
			return new TakeResult(takeStatus,focusResult);
		} catch (Exception e) {
			throw new ProtocolError("Failed to handle the response of picture-taking: [" + response + "] - " + e.getMessage());
		}
	}

	private TakeStatus parseTakeStatus(String takeStatusStr) throws ProtocolError {
		TakeStatus takeStatus;
		if (takeStatusStr.equals("ok")) {
			takeStatus = TakeStatus.OK;
		} else if (takeStatusStr.equals("ng")) {
			takeStatus = TakeStatus.FAILED;
		} else {
			throw new ProtocolError("Failed to handle the response of picture taking - unexpected status: " + takeStatusStr);
		}
		return takeStatus;
	}

	private FocusStatus parseFocusStatus(String focusStatusStr) throws ProtocolError {
		FocusStatus focusStatus;
		if (focusStatusStr.equals("ok")) {
			focusStatus = FocusStatus.OK;
		} else if (focusStatusStr.equals("ng")) {
			focusStatus = FocusStatus.FAILED;
		} else if (focusStatusStr.equals("none")) {
			focusStatus = FocusStatus.DISABLED;
		} else {
			throw new ProtocolError("Failed to handle the response of focus acquisition - unexpected status: " + focusStatusStr);
		}
		return focusStatus;
	}
	
	private FocusResult parseFocusResult(FocusStatus focusStatus, String coordinatesStr, String frameSizeStr) throws ProtocolError {

		Optional<Coordinate> focusFrameTopLeftCoordinate;
		Optional<Dimensions> focusFrameDimensions;
		
		if (focusStatus==FocusStatus.OK) {
			if (coordinatesStr==null || frameSizeStr==null) {
				throw new ProtocolError("Failed to handle the response of focus acquisition - missing parameters");
			} else {
				String[] coordinates = coordinatesStr.split("x");
				String[] frameSize = frameSizeStr.split("x");
				focusFrameTopLeftCoordinate = Optional.of(new Coordinate(
						Integer.parseInt(coordinates[0]),
						Integer.parseInt(coordinates[1])));
				focusFrameDimensions = Optional.of(new Dimensions(
						Integer.parseInt(frameSize[0]),
						Integer.parseInt(frameSize[1])));
			}
		} else {
			focusFrameTopLeftCoordinate  = Optional.ofNullable(null);
			focusFrameDimensions 	= Optional.ofNullable(null);
		}
		
		return new FocusResult(focusStatus,
				focusFrameTopLeftCoordinate,
				focusFrameDimensions);
	}

	@Synchronized
	public byte[] requestLastTakenSmallSizeJpeg() throws ProtocolError {
		return HTTPClient.getInstance().doGetBinary("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MISC + "?com=getrecview");
	}
	
	@Synchronized
	public byte[] requestLastTakenFullSizeJpeg() throws ProtocolError {
		return HTTPClient.getInstance().doGetBinary("http://" + CameraMainAPI.DEF_CAMERA_IP + "/" + TAKE_MISC + "?com=getlastjpg");
	}

	@Synchronized
	public ImageResult takeSmallSizeJpeg() throws ProtocolError, InterruptedException {
		return takeJpeg(true);
	}

	@Synchronized
	public ImageResult takeFullSizeJpeg() throws ProtocolError, InterruptedException {
		return takeJpeg(false);
	}

	private ImageResult takeJpeg(boolean smallSize) throws ProtocolError, InterruptedException {
		Lock lock = new ReentrantLock();
		Condition condition = lock.newCondition();
		lock.lock();
		try {
			final AtomicBoolean timeoutExpired = new AtomicBoolean(false);
			
			startLiveView(new LiveViewHandler() {

				boolean notified = false;
				
				public void signal() {
					lock.lock();
					try {
						if (!notified) {
							notified = true;
							condition.signal();
						}
					} finally {
						lock.unlock();
					}
				}
				
				@Override
				public void onTimeout(long ms) {
					timeoutExpired.set(true);
					signal();
				}
				
				@Override
				public void onImage(LiveViewImageData imageData) {
					signal();
				}
				
			});
			
			condition.await();

			if (timeoutExpired.get()) {
				throw new ProtocolError("Timeout expired");
			} 

			TakeResult takeResult = takePicture();
			
			stopLiveView();
			
			byte[] image;
			if (takeResult.getTakeStatus()==TakeStatus.OK) {
				image = smallSize 
					? requestLastTakenSmallSizeJpeg()  
					: requestLastTakenFullSizeJpeg();
			} else {
				image = null;
			}
			return new ImageResult(image,takeResult);
					
		} finally {
			lock.unlock();
			stopLiveView();
		}
	}
	
}
