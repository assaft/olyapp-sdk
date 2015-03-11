package org.olyapp.sdk.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.olyapp.sdk.CameraProperty;
import org.olyapp.sdk.ControlMode;
import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.FocusResult;
import org.olyapp.sdk.Frame;
import org.olyapp.sdk.Image;
import org.olyapp.sdk.LiveViewControl;
import org.olyapp.sdk.LiveViewShot;
import org.olyapp.sdk.Point;
import org.olyapp.sdk.ProtocolError;
import org.olyapp.sdk.lvsrv.LiveViewServer;
import org.olyapp.sdk.utils.JpegUtils;
import org.olyapp.sdk.xml.CamInfo;
import org.olyapp.sdk.xml.CommandList;
import org.olyapp.sdk.xml.ConnectMode;
import org.olyapp.sdk.xml.Desc;
import org.olyapp.sdk.xml.DescList;
import org.olyapp.sdk.xml.FocusResponse;
import org.olyapp.sdk.xml.SetParam;
import org.olyapp.sdk.xml.StartTake;

public class ICameraProtocol {

	private static final String 		DEF_CAMERA_IP = "192.168.0.10";

	private static final int 			DEF_LIVEVIEW_PORT 	= 50529;
	private static final Dimensions 	DEF_LIVEVIEW_RES  	= new IDimension(640,480);
	private static final int			DEF_CONTROL_TO_MS	= 1000;
	private static final int			DEF_STREAM_TO_MS	= 1000;
	private static final boolean		DEF_DEBUG_CONTROL	= false;
	private static final boolean		DEF_DEBUG_STREAM	= false;
	private static final LiveViewControl DEF_LIVEVIEW_CONTROL = LiveViewControl.SemiAutomatic;
	
	private static final String			KEY_LIVEVIEW_PORT 	= "LiveViewPort";
	private static final String 		KEY_LIVEVIEW_RES  	= "LiveViewDefResolution";
	private static final String			KEY_CONTROL_TO_MS	= "HTTPTimeoutMS";
	private static final String			KEY_STREAM_TO_MS	= "FrameTimeoutMS";
	private static final String			KEY_DEBUG_CONTROL	= "DebugControl";
	private static final String			KEY_DEBUG_STREAM	= "DebugStream";
	private static final String			KEY_LIVEVIEW_CONTROL= "LiveViewControl"; 
	
	private static final String 		DEF_XML_CHAR_SET = "ISO-8859-1";
	
	private static final String GET_CAMERA_INFO = "/get_caminfo.cgi";
	private static final String GET_CONNECT_MODE = "/get_connectmode.cgi";
	private static final String GET_COMMAND_LIST = "/get_commandlist.cgi";
	private static final String SWITCH_MODE = "/switch_cammode.cgi";  // ?mode=play
	//private static final String SET_TIME = "/set_utctimediff.cgi"; //?utctime=20150227T103539&diff=%2B0200
	//private static final String /switch_cammode.cgi?mode=rec&lvqty=0640x0480
	private static final String GET_PROP = "/get_camprop.cgi"; //
	private static final String SET_PROP = "/set_camprop.cgi";
	
	private static final String TAKE_MISC = "exec_takemisc";
	private static final String TAKE_MOTION = "exec_takemotion.cgi";
	
	
	private static final JAXBContext jaxbContext = initContext();
	private static final Unmarshaller jaxbUnmarshaller = initUnmarshaller();
	private static final Marshaller jaxbMarshaller = initMarshaller(); 
	
    private static JAXBContext initContext() {
        try {
			return JAXBContext.newInstance(
					CamInfo.class,
					CommandList.class,
					ConnectMode.class,
					FocusResponse.class,
					SetParam.class,
					Desc.class,
					DescList.class);
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
	
	private static Marshaller initMarshaller() {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_ENCODING, DEF_XML_CHAR_SET);
			return jaxbContext.createMarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static Unmarshaller initUnmarshaller() {
		try {
			return jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static ICameraProtocol instance = null;
	
	public static void init(Properties settings) throws ProtocolError {
		if (instance==null) {
			instance = new ICameraProtocol(settings);
		} else {
			throw new ProtocolError("Camera protocol has already been initialized");
		}
	}
	
	public static void init() throws ProtocolError {
		init(null);
	}
	
	public static ICameraProtocol getInst() {
		return instance;
	}
	
	
	/*

GET /exec_takemotion.cgi?com=starttake HTTP/1.1
Host: 192.168.0.10
Connection: Keep-Alive
User-Agent: OI.Share v2

HTTP/1.1 200 OK
Content-Type: text/xml
Content-Length: 152
Connection: close

<?xml version="1.0"?><response><take>ok</take><affocus>ok</affocus><afframepoint>0412x0346</afframepoint><afframesize>0072x0072</afframesize></response>

	<?xml version="1.0"?><response><take>ok</take><affocus>ok</affocus><afframepoint>0412x0346</afframepoint><afframesize>0072x0072</afframesize></response>
			
	exec_takemisc.cgi?com=getrecview -> small jpeg
	exec_takemisc.cgi?com=getlastjpg -> big jpeg

	*/
	
	//http://192.168.0.10/exec_takemotion.cgi?com=assignafframe&point=0448x0383
	
	private final RequestConfig requestConfig;
	private final VoidHandler voidHandler;

	private final LiveViewServer liveViewServer;
	private ControlMode currentControlMode;
	private Dimensions liveViewRes; 
	
	private final int controlTimeoutMS;
	private final int streamTimeoutMS;
	
	private final boolean debugControl;
	private final boolean debugStream;
	
	private LiveViewControl liveViewControl;
	private boolean liveViewStarted;

	private ICameraProtocol(Properties settings) throws ProtocolError {
		int port;
		if (settings==null) {
			port = DEF_LIVEVIEW_PORT;
			liveViewRes = DEF_LIVEVIEW_RES;
			debugControl = DEF_DEBUG_CONTROL;
			debugStream = DEF_DEBUG_STREAM;
			controlTimeoutMS = DEF_CONTROL_TO_MS;
			streamTimeoutMS = DEF_STREAM_TO_MS;
			liveViewControl = DEF_LIVEVIEW_CONTROL;
		} else {
			port = Integer.parseInt(settings.getProperty(KEY_LIVEVIEW_PORT,Integer.toString(DEF_LIVEVIEW_PORT)));
			liveViewRes = IDimension.parse(settings.getProperty(KEY_LIVEVIEW_RES,DEF_LIVEVIEW_RES.toString()));
			debugControl = Boolean.parseBoolean(settings.getProperty(KEY_DEBUG_CONTROL,Boolean.toString(DEF_DEBUG_CONTROL)));
			debugStream = Boolean.parseBoolean(settings.getProperty(KEY_DEBUG_STREAM,Boolean.toString(DEF_DEBUG_STREAM)));
			controlTimeoutMS = Integer.parseInt(settings.getProperty(KEY_CONTROL_TO_MS,Integer.toString(DEF_CONTROL_TO_MS))); 
			streamTimeoutMS = Integer.parseInt(settings.getProperty(KEY_STREAM_TO_MS,Integer.toString(DEF_STREAM_TO_MS))); 
			liveViewControl = LiveViewControl.valueOf(settings.getProperty(KEY_LIVEVIEW_CONTROL,DEF_LIVEVIEW_CONTROL.toString()));
		}
		liveViewServer = new LiveViewServer(port, debugStream);
		currentControlMode = ControlMode.UnSet;
		liveViewStarted = false;
		requestConfig = RequestConfig.custom().setConnectTimeout(controlTimeoutMS).build();
		voidHandler = new VoidHandler();
	}
	
	private <T> T doGet(String s, Handler<T> handler) throws ProtocolError {
		return request(new HttpGet(s),handler);
	}
	
	private <T> T doPost(String s, Object o, Handler<T> handler) throws ProtocolError {
		try {
			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(o, sw);
			HttpPost httppost = new HttpPost(s);
			httppost.setEntity(new StringEntity(sw.toString(),DEF_XML_CHAR_SET));
			return request(httppost,handler);
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new ProtocolError(e.getMessage());
		}

	}
	
	private <T> T request(HttpUriRequest httpRequest, Handler<T> handler) throws ProtocolError {
		if (debugControl) {
			System.out.println("Requesting: " + httpRequest.getURI());
		}
		CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpRequest);
			if (debugControl) {
				System.out.println("Received: " + response.getStatusLine());
			}
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != 200) {
	            throw new ProtocolError("Failed with HTTP error code : " + statusCode);
	        }
	        return handler.handleResponse(response);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProtocolError(e.getMessage());
		} finally {
			if (response!=null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private abstract static class Handler<T> {
		abstract T handleResponse(HttpResponse response);
	}
	
	private static class VoidHandler extends Handler<Void> {
		@Override
		Void handleResponse(HttpResponse response) {return null;}
	}

	private static class StringHandler extends Handler<String> {
		@Override
		String handleResponse(HttpResponse response) {
			
			try {
		        //pull back the response object
		        HttpEntity httpEntity = response.getEntity();
		        String apiOutput = EntityUtils.toString(httpEntity);
	
		        //Lets see what we got from API
		        System.out.println(apiOutput); //<user id="10"><firstName>demo</firstName><lastName>user</lastName></user>
		        
				return apiOutput;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}				
		}
	}
	
	private static class BinaryHandler extends Handler<byte[]> {
		@Override
		byte[] handleResponse(HttpResponse response) {
			try {
		        //pull back the response object
				return EntityUtils.toByteArray(response.getEntity());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}				
		}
	}
	
	private static class XMLHandler<T> extends Handler<T> {

		//private Class<T> type;
		
		public XMLHandler(Class<T> type) {
		//	this.type = type;
		}
		
		@SuppressWarnings("unchecked")
		T handleResponse(HttpResponse response) {
			
			try {
		        //pull back the response object
		        HttpEntity httpEntity = response.getEntity();
		        String apiOutput = EntityUtils.toString(httpEntity);
	
		        //Lets see what we got from API
		        System.out.println(apiOutput); //<user id="10"><firstName>demo</firstName><lastName>user</lastName></user>
		         
		        //apiOutput = "<?xml version=\"1.0\"?>" + "\n" + "<connectmode>private</connectmode>";
		        
		        //Lets see how to jaxb unmarshal the api response content
		        return (T) jaxbUnmarshaller.unmarshal(new StringReader(apiOutput));
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (JAXBException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	public void remoteShutterTrigger(boolean lock) {
		// TODO Auto-generated method stub
	}

	public void setControlMode(ControlMode controlMode) throws ProtocolError {
		if (controlMode!=currentControlMode) {
			if (currentControlMode==ControlMode.LiveView && liveViewControl!=LiveViewControl.Manual) {
				stopLiveView();
			}
			switch (controlMode) {
			case LiveView:
				doGet("http://"+DEF_CAMERA_IP+SWITCH_MODE + "?mode=rec&lvqty"+liveViewRes.toString(),voidHandler);
				break;
			case Play:
				doGet("http://"+DEF_CAMERA_IP+SWITCH_MODE+"?mode=play",voidHandler);
				break;
			case RemoteShutter:
				doGet("http://"+DEF_CAMERA_IP+SWITCH_MODE+"?mode=shutter",voidHandler);
				break;
			default:
				break;
			}
			currentControlMode = controlMode;
			if (currentControlMode==ControlMode.LiveView && liveViewControl!=LiveViewControl.Manual) {
				startLiveView();
			}
		}
	}

	public ControlMode getControlMode() {
		return currentControlMode;
	}
	
	public List<Dimensions> getLiveViewResolutions() throws ProtocolError {
		String commandList = getCommandList();
		int lvqtyIndex = commandList.indexOf("\"lvqty\"");
		if (lvqtyIndex==-1) {
			throw new ProtocolError("Failed to read live view resolutions");
		}
		
		int openingBracket = commandList.lastIndexOf('<', lvqtyIndex);
		if (lvqtyIndex==-1) {
			throw new ProtocolError("Failed to read live view resolutions");
		}

		String tagName = commandList.substring(openingBracket,commandList.indexOf(' ', openingBracket));

		int endingBracket = commandList.indexOf("<" + tagName + "/>",openingBracket);
		if (lvqtyIndex==-1) {
			throw new ProtocolError("Failed to read live view resolutions");
		}

		String lvqty = commandList.substring(openingBracket,endingBracket);

		List<Dimensions> dimensions = new ArrayList<Dimensions>();		
		
		Pattern lvqtyPattern = Pattern.compile("\\\"(.*)x(.*)\\\"");
		Matcher matcher = lvqtyPattern.matcher(lvqty);
		while(matcher.find()) {
			dimensions.add(new IDimension(
					Integer.parseInt(matcher.group(1)),
					Integer.parseInt(matcher.group(2))));
			
			System.out.println("found: " + matcher.group(1) +
                               " "       + matcher.group(2));
        }
		return dimensions;
	}

	public void setLiveViewResolution(Dimensions res) {
		liveViewRes  = res;
	}
	
	public String getCameraModel() throws ProtocolError {
		return doGet("http://" + DEF_CAMERA_IP + GET_CAMERA_INFO,
				new XMLHandler<CamInfo>(CamInfo.class)).getModel();
	}

	public String getConnectionModel() throws ProtocolError {
		return doGet("http://" + DEF_CAMERA_IP + GET_CONNECT_MODE,
				new XMLHandler<ConnectMode>(ConnectMode.class)).getMode();
	}
	
	public String getCommandList() throws ProtocolError {
		return doGet("http://" + DEF_CAMERA_IP + GET_COMMAND_LIST, new StringHandler());
	}

	public void startLiveView() throws ProtocolError {
		int port = liveViewServer.getPort();
		doGet("http://"+DEF_CAMERA_IP+TAKE_MISC + "?com=startliveview&port="+port,voidHandler);
		liveViewStarted = true;
	}	

	public void stopLiveView() throws ProtocolError {
		doGet("http://" + DEF_CAMERA_IP + TAKE_MISC + "?com=stopliveview",voidHandler);
		liveViewStarted = false;
	}	
	
	public void shutdownCamera() throws ProtocolError {
		doGet("http://" + DEF_CAMERA_IP + TAKE_MISC + "?com=exec_pwoff",voidHandler);
	}

	public FocusResult acquireFocus(Point point) throws ProtocolError {
		FocusResponse response = doGet("http://" + DEF_CAMERA_IP + TAKE_MOTION + 
				"?com=assignafframe&point="+
				String.format("%010d", point.getX()) + "x" +
				String.format("%010d", point.getY()),
				new XMLHandler<FocusResponse>(FocusResponse.class));
		
		return response.getAffocus().equals("ok")
				? new IFocusResult.IFocusOK(
						IPoint.parse(response.getAfframepoint()),
						IDimension.parse(response.getAfframesize()))
				: new IFocusResult.IFocusError(response.getAffocus());
	}

	public void releaseFocus() throws ProtocolError {
		doGet("http://" + DEF_CAMERA_IP + TAKE_MOTION + "?com=releaseafframe",voidHandler);
	}

	public Map<String,CameraProperty> getAllProperties() throws ProtocolError {
		DescList response = doGet("http://" + DEF_CAMERA_IP + GET_PROP + 
				"?com=desc&propname=desclist",
				new XMLHandler<DescList>(DescList.class));

		Map<String,CameraProperty> properties = new HashMap<String, CameraProperty>();
		for (Desc desc : response.getDescriptions()) {
			String enumTag = desc.getEnumTag();
			List<String> enumValues = new ArrayList<String>();
			if (enumTag!=null) {
				StringTokenizer st = new StringTokenizer(enumTag);
				while (st.hasMoreElements()) {
					enumValues.add(st.nextToken());
				}
			}
			properties.put(desc.getPropName(),
					new ICameraProperty(desc.getPropName(),
							desc.getValue(),
							desc.getAttribute(),
							enumValues));
		}
		return properties;
	}
	
	public CameraProperty getProperty(String propertyName) throws ProtocolError {
		Desc response = doGet("http://" + DEF_CAMERA_IP + GET_PROP + 
				"?com=desc&propname="+propertyName,
				new XMLHandler<Desc>(Desc.class));
		
		String enumTag = response.getEnumTag();
		List<String> enumValues = new ArrayList<String>();
		if (enumTag!=null) {
			StringTokenizer st = new StringTokenizer(enumTag);
			while (st.hasMoreElements()) {
				enumValues.add(st.nextToken());
			}
		}
		return new ICameraProperty(response.getPropName(),
				response.getValue(),
				response.getAttribute(),
				enumValues);
		
	}
	
	public void setProperty(CameraProperty property) throws ProtocolError {
		doPost("http://" + DEF_CAMERA_IP + SET_PROP + "?com=set&propname="+property.getPropName(),
				new SetParam(property.getValue()),voidHandler);
	}

	public Frame getNextFrame() throws ProtocolError {
		return liveViewServer.getNextFrame(streamTimeoutMS);		
	}

	public Image getJpeg(String command) throws ProtocolError {
		try {
			boolean liveViewStopped = false;
			if (liveViewStarted && liveViewControl==LiveViewControl.Automatic) {
				stopLiveView();
				liveViewStopped = true;
			}
			byte[] data = doGet("http://" + DEF_CAMERA_IP + TAKE_MISC + "?com="+command,new BinaryHandler()); 
			if (liveViewStopped) {
				startLiveView();
			}
			return new IImage(data,JpegUtils.getDimensions(data));
		} catch (Exception e) {
			throw new ProtocolError(e.getMessage());
		}
	}
	
	public Image getSmallJpeg() throws ProtocolError {
		return getJpeg("getrecview");
	}
		
	public Image getFullJpeg() throws ProtocolError {
		return getJpeg("getlastjpg");
	}

	public LiveViewShot shootLiveView() throws ProtocolError {
		StartTake response = doGet("http://" + DEF_CAMERA_IP + TAKE_MOTION + "?com=starttake",
				new XMLHandler<StartTake>(StartTake.class));
		return new ILiveViewShot(
				response.getTake(),
				response.getAffocus().equals("ok")
					? new IFocusResult.IFocusOK(
							IPoint.parse(response.getAfframepoint()),
							IDimension.parse(response.getAfframesize()))
					: new IFocusResult.IFocusError(response.getAffocus()));
	}
	
}
