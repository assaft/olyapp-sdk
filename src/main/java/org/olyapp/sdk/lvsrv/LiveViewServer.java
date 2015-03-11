package org.olyapp.sdk.lvsrv;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.Fraction;
import org.olyapp.sdk.Frame;
import org.olyapp.sdk.FrameMetadata;
import org.olyapp.sdk.ProtocolError;
import org.olyapp.sdk.impl.IFraction;
import org.olyapp.sdk.impl.IFrame;
import org.olyapp.sdk.impl.IFrameMetadata;
import org.olyapp.sdk.utils.JpegUtils;
import org.olyapp.sdk.utils.StringUtils;

public class LiveViewServer extends Thread {

	private final static int SOCKET_TIMEOUT_MS = 4000;
	
	private final int port;
	private final boolean debugMode;
	
	private Map<Integer,ImageBuffer> receivedImageData;

	private AtomicBoolean halt;
	private AtomicBoolean error;
	
	private DatagramSocket serverSocket;
	
	private SynchronousQueue<ImageBuffer> readyImageQ;
	
	public LiveViewServer(int port, boolean debugMode) throws ProtocolError {
		try {
			this.port = port;
			this.debugMode = debugMode;
			receivedImageData = new HashMap<Integer, ImageBuffer>();
			halt = new AtomicBoolean(false);
			error = new AtomicBoolean(false);
			readyImageQ = new SynchronousQueue<ImageBuffer>();
			initSocket();
			start();
		} catch (Exception e) {
			throw new ProtocolError(e.getMessage());
		}
	}
	
	private void initSocket() throws SocketException {
		serverSocket = new DatagramSocket(port);
		serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
	}
	
	public void run() {
		
		byte[] buffer = new byte[1536];
		int currentSessionId = -1;
		
		System.out.println("Motion JPEG UDP Server - Entering mainloop");
		
		while(!halt.get() && !error.get()) {
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
			try {
				serverSocket.receive(receivePacket);

				byte[] data = receivePacket.getData();
				int offset = receivePacket.getOffset();
				int length = receivePacket.getLength();
				
				// extract the meta data
				int type = (((data[offset+0] & 0xff) << 8) | (data[offset+1] & 0xff));
				int packetId = (((data[offset+2] & 0xff) << 8) | (data[offset+3] & 0xff));
				int imageId = (((data[offset+4] & 0xff) << 24) | 
						((data[offset+5] & 0xff) << 16) |
						(data[offset+6] & 0xff) << 8) | 
						(data[offset+7] & 0xff);
				int sessionId = (((data[offset+8]  & 0xff) << 24) | 
						((data[offset+9]  & 0xff) << 16) |
						(data[offset+10] & 0xff) << 8) | 
						(data[offset+11] & 0xff);
	
				String imageIdStr = StringUtils.toHex(imageId);
				String packetIdStr = StringUtils.toHex(packetId);
				
				// read more...
				
				if (currentSessionId==-1) {
					currentSessionId = sessionId;
				}
	
				// sanity
				if (sessionId==currentSessionId) {
	
					// obtain an image buffer object
					ImageBuffer imageBuffer;
					if (!receivedImageData.containsKey(imageId)) {
						imageBuffer = new ImageBuffer();
						receivedImageData.put(imageId, imageBuffer);
					} else {
						imageBuffer = receivedImageData.get(imageId);
					}
	
					// add the packet to the image buffer and check if the image is full
					try {
						if (imageBuffer.addPacket(type,packetId,imageId,data,length)) {
							
							//System.out.println("Image " + imageIdStr + " is ready");
							receivedImageData.remove(imageId);
							
							// remove images with lower ids
							Iterator<Entry<Integer, ImageBuffer>> iter = receivedImageData.entrySet().iterator();
							while (iter.hasNext()) {
							    Entry<Integer, ImageBuffer> entry = iter.next();
							    if (entry.getKey()<imageId) {
									System.out.println("Removing data of past image " + StringUtils.toHex(entry.getKey()));
							        iter.remove();
							    }
							}
							
							boolean received = readyImageQ.offer(imageBuffer);
							
							if (debugMode) {
								System.out.println("Offering " + imageIdStr + ": " + (received ? "received" : "ignored"));
							}
						}
					} catch (InvalidPacket e) {
						System.err.println("Packet " + packetIdStr + " is invalid: " + e.getMessage() + "; image " + imageIdStr + " will be ignored");
					}
				}
			} catch (SocketTimeoutException e) {
				if (currentSessionId!=-1) {
					if (debugMode) {
						System.out.println("Timeout expired");
					}
					int size = receivedImageData.size();
					if (size>0) {
						if (debugMode) {
							System.out.println("Cleaning temporary buffer of " + size + " images");
						}
						receivedImageData.clear();
					}
				}
				currentSessionId = -1;				
			} catch (IOException e) {
				e.printStackTrace();
				try {
					initSocket();
				} catch (SocketException e1) {
					e1.printStackTrace();
					error();
				}
			}
		}
		System.out.println("Motion JPEG UDP Server - Exiting mainloop");
	}
	
	public void halt() {
		halt.set(true);
	}
	
	public void error() {
		error.set(true);
	}
	
	public Frame getNextFrame(int ms) throws ProtocolError {
		if (!error.get()) {
			try {
				ImageBuffer image = readyImageQ.poll(ms,TimeUnit.MILLISECONDS);
				Frame frame = null;
				if (image!=null) {
					byte[] metadata = image.getMetadata();
					Dimensions dimension = JpegUtils.getDimensions(metadata);
					Fraction shutterSpeed 	= new IFraction((metadata[0x64]<<8) | metadata[0x65], (metadata[0x66]<<8) | metadata[0x67]);
					Fraction aperture 		= new IFraction((metadata[0x76]<<8) | metadata[0x77], (metadata[0x78]<<8) | metadata[0x79]);
					int expComp				= ~(metadata[0x84]<<24 + metadata[0x85]<<16 + metadata[0x86]<<8 + metadata[0x87]);
					FrameMetadata frameMetadata = new IFrameMetadata(
							dimension, 
							shutterSpeed,aperture,expComp,
							false,0,false,0,false,0,new IFraction(0,0),0,false,0,false);
					frame = new IFrame(frameMetadata,image.getImage());
				}
				return frame;
			} catch (Exception e) {
				throw new ProtocolError(e.getMessage());
			}
		} else {
			throw new ProtocolError("General error in UDP Server");
		}
	}

	public int getPort() throws ProtocolError {
		if (!error.get()) {
			return port;
		} else {
			throw new ProtocolError("General error in UDP Server");
		}
	}

}