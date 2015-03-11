package org.olyapp.sdk.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.olyapp.sdk.FocusResult;
import org.olyapp.sdk.Frame;
import org.olyapp.sdk.Image;
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


	private static final String CAMERA_IP = "192.168.0.10";
	
	private static final int 	LV_PORT = 50529;

	private static final String CHAR_SET = "ISO-8859-1";
	
	private static final String GET_CAMERA_INFO = "/get_caminfo.cgi";
	private static final String GET_CONNECT_MODE = "/get_connectmode.cgi";
	private static final String GET_COMMAND_LIST = "/get_commandlist.cgi";
	private static final String SWITCH_MODE = "/switch_cammode.cgi";  // ?mode=play
	private static final String SET_TIME = "/set_utctimediff.cgi"; //?utctime=20150227T103539&diff=%2B0200
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
		    marshaller.setProperty(Marshaller.JAXB_ENCODING, CHAR_SET);
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


	/*
	 *
	 * 
	 * 
	 * 



GET /exec_takemotion.cgi?com=starttake HTTP/1.1
Host: 192.168.0.10
Connection: Keep-Alive
User-Agent: OI.Share v2

HTTP/1.1 200 OK
Content-Type: text/xml
Content-Length: 152
Connection: close

<?xml version="1.0"?><response><take>ok</take><affocus>ok</affocus><afframepoint>0412x0346</afframepoint><afframesize>0072x0072</afframesize></response>


	 * 
	 * 
	 * 
	
	<?xml version="1.0"?><response><take>ok</take><affocus>ok</affocus><afframepoint>0412x0346</afframepoint><afframesize>0072x0072</afframesize></response>
			
	exec_takemisc.cgi?com=getrecview -> small jpeg
	exec_takemisc.cgi?com=getlastjpg -> big jpeg
	

	*/
	
	//http://192.168.0.10/exec_takemotion.cgi?com=assignafframe&point=0448x0383
	

	private String maxLiveViewQuality = "0640x0480"; 
	
	private final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3 * 1000).build();
	private final VoidHandler voidHandler = new VoidHandler();

	private LiveViewServer liveViewServer; 
	
	private ICameraProtocol() {
	}
	
	private <T> T doGet(String s, Handler<T> handler) throws ProtocolError {
		return request(new HttpGet(s),handler);
	}
	
	private <T> T doPost(String s, Object o, Handler<T> handler) throws ProtocolError {
		try {
			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(o, sw);
			HttpPost httppost = new HttpPost(s);
			httppost.setEntity(new StringEntity(sw.toString(),CHAR_SET));
			return request(httppost,handler);
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new ProtocolError(e.getMessage());
		}

	}
	
	private <T> T request(HttpUriRequest httpRequest, Handler<T> handler) throws ProtocolError {
		CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpRequest);
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

		private Class<T> type;
		
		public XMLHandler(Class<T> type) {
			this.type = type;
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

	private static ICameraProtocol instance = new ICameraProtocol();

	public static ICameraProtocol getInst() {
		return instance;
	}

	public void setControlMode(ControlMode controlMode, int timeoutMS) throws ProtocolError {
		long startTime = System.currentTimeMillis();
		long endTime = timeoutMS > 0 ? startTime + timeoutMS : Long.MAX_VALUE;
		if (controlMode==ControlMode.RemoteShutter) {
			if (liveViewServer!=null) {
				liveViewServer.halt();
				liveViewServer = null;
			}
			doGet("http://"+CAMERA_IP+SWITCH_MODE+"?mode=shutter",voidHandler);
					
					/*,
					System.currentTimeMillis()-startTime); */
		} else {
			doGet("http://"+CAMERA_IP+SWITCH_MODE + "?mode=rec&lvqty"+maxLiveViewQuality,voidHandler);
			doGet("http://"+CAMERA_IP+TAKE_MISC + "?com=startliveview&port="+LV_PORT,voidHandler);

			liveViewServer = new LiveViewServer(LV_PORT, false);
			
			// live view request
			// open udp sockets, create queues...
		}

		long remainingTime;
		while (System.currentTimeMillis()>endTime && getControlMode()!=controlMode) {
			if ((remainingTime = System.currentTimeMillis()-endTime)>0) {
				//				Thread.sleep(Math.min(200, remainingTime));
			}
		}

		if (getControlMode()!=controlMode) {
			//throw new TimeoutExpired();
		}

	}

	public ControlMode getControlMode() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getCameraModel() throws ProtocolError {
		return doGet("http://" + CAMERA_IP + GET_CAMERA_INFO,
				new XMLHandler<CamInfo>(CamInfo.class)).getModel();
	}

	public String getConnectionModel() throws ProtocolError {
		return doGet("http://" + CAMERA_IP + GET_CONNECT_MODE,
				new XMLHandler<ConnectMode>(ConnectMode.class)).getMode();
	}
	
	public String getCommandList() throws ProtocolError {
		return doGet("http://" + CAMERA_IP + GET_COMMAND_LIST,
				new StringHandler());
	}

	public StartTake startLiveView() throws ProtocolError {
		return doGet("http://" + CAMERA_IP + TAKE_MOTION + "?com=starttake",
				new XMLHandler<StartTake>(StartTake.class));
	}

	public void stopLiveView() throws ProtocolError {
		doGet("http://" + CAMERA_IP + TAKE_MISC + "?com=stopliveview",
				new VoidHandler());
	}	
	
	public void shutdownCamera() throws ProtocolError {
		doGet("http://" + CAMERA_IP + TAKE_MISC + "?com=exec_pwoff",
				new VoidHandler());
	}

	public FocusResult acquireFocus(Point point) throws ProtocolError {
		FocusResponse response = doGet("http://" + CAMERA_IP + TAKE_MOTION + 
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
		doGet("http://" + CAMERA_IP + TAKE_MOTION + "?com=releaseafframe",
			new VoidHandler());
	}

	public Map<String,CameraProperty> getAllProperties() throws ProtocolError {
		DescList response = doGet("http://" + CAMERA_IP + GET_PROP + 
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
		Desc response = doGet("http://" + CAMERA_IP + GET_PROP + 
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
		doPost("http://" + CAMERA_IP + SET_PROP + "?com=set&propname="+property.getPropName(),
				new SetParam(property.getValue()),new VoidHandler());
	}

	public Frame getNextFrame() throws ProtocolError {
		return liveViewServer.getNextFrame(1000);		
	}

	public Image getSmallJpeg() throws ProtocolError {
		try {
			byte[] data = doGet("http://" + CAMERA_IP + TAKE_MISC + "?com=getrecview",new BinaryHandler()); 
			return new IImage(data,JpegUtils.getDimensions(data));
		} catch (Exception e) {
			throw new ProtocolError(e.getMessage());
		}
	}
	
	public Image getFullJPG() throws ProtocolError {
		try{
			byte[] data = doGet("http://" + CAMERA_IP + TAKE_MISC + "?com=getlastjpg",new BinaryHandler()); 
			return new IImage(data,JpegUtils.getDimensions(data));
		} catch (Exception e) {
			throw new ProtocolError(e.getMessage());
		}
	}
	
}
