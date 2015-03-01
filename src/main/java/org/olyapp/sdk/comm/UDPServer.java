package org.olyapp.sdk.comm;

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

import org.olyapp.sdk.utils.StringUtils;

public class UDPServer extends Thread {

	private final static int SOCKET_TIMEOUT_MS = 4000;
	
	private final int port;
	private final boolean debugMode;
	
	private Map<Integer,ImageBuffer> receivedImageData;

	private AtomicBoolean stop;
	DatagramSocket serverSocket;
	
	private SynchronousQueue<ImageBuffer> readyImageQ;
	
	public UDPServer(int port, boolean debugMode) throws SocketException {
		this.port = port;
		this.debugMode = debugMode;
		receivedImageData = new HashMap<Integer, ImageBuffer>();
		stop = new AtomicBoolean(false);		
		readyImageQ = new SynchronousQueue<ImageBuffer>();
		initSocket();
		start();
	}
	
	private void initSocket() throws SocketException {
		serverSocket = new DatagramSocket(port);
		serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
	}
	
	public void run() {
		
		byte[] buffer = new byte[1536];
		int currentSessionId = -1;
		
		System.out.println("Motion JPEG UDP Server - Entering mainloop");
		
		while(!stop.get()) {
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
					throw new RuntimeException(e);
				}
			}
		}
		System.out.println("Motion JPEG UDP Server - Exiting mainloop");
	}
	
	public void halt() {
		stop.set(true);
	}
	
	public byte[] getNextFrame(int ms) throws InterruptedException {
		ImageBuffer image = readyImageQ.poll(ms,TimeUnit.MILLISECONDS);
		return image!=null ? image.getImage() : null;
	}

}