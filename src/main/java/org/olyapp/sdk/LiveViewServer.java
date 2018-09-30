package org.olyapp.sdk;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.olyapp.sdk.lvsrv.ImageBuffer;
import org.olyapp.sdk.utils.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.Value;

public class LiveViewServer {

	private final static int SOCKET_TIMEOUT_MS = 200;

	private final ExecutorService executor;
	private final AtomicBoolean liveStreamOpen;
	private final LoadingCache<Integer, ImageBuffer> packetCache;
	private final LinkedBlockingDeque<ImageData> imageQueue;
	
	private final Lock lock;
	private final Condition liveStreamStopped; 

	public LiveViewServer() {
		this.executor = Executors.newSingleThreadExecutor();
		this.liveStreamOpen = new AtomicBoolean(false);
		this.packetCache = CacheBuilder.newBuilder()
			    .maximumSize(1000)
			    .expireAfterAccess(2, TimeUnit.SECONDS)
			    .build(
			        new CacheLoader<Integer, ImageBuffer>() {
			          public ImageBuffer load(Integer i) {
			        	  return new ImageBuffer();
			          }
			        });
		this.imageQueue = new LinkedBlockingDeque<>(1000);
		this.lock = new ReentrantLock();
		this.liveStreamStopped = lock.newCondition();
	}
	
	public void start(int port) {

		executor.submit(()->{

			byte[] buffer = new byte[1536];
			int currentSessionId = -1;
			liveStreamOpen.set(true);

			try (DatagramSocket serverSocket = new DatagramSocket(port)) {
				serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
				while(liveStreamOpen.get()) {
					try {
						DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
						serverSocket.receive(receivePacket);
						byte[] data = receivePacket.getData();
						int offset = receivePacket.getOffset();
						int length = receivePacket.getLength();
	
						int sessionId = (((data[offset+8]  & 0xff) << 24) | 
								((data[offset+9]  & 0xff) << 16) |
								(data[offset+10] & 0xff) << 8) | 
								(data[offset+11] & 0xff);
						
						if (currentSessionId==-1) {
							currentSessionId = sessionId;
						}
	
						if (sessionId==currentSessionId) {
							int type = (((data[offset+0] & 0xff) << 8) | (data[offset+1] & 0xff));
							int packetId = (((data[offset+2] & 0xff) << 8) | (data[offset+3] & 0xff));
							int imageId = (((data[offset+4] & 0xff) << 24) | 
									((data[offset+5] & 0xff) << 16) |
									(data[offset+6] & 0xff) << 8) | 
									(data[offset+7] & 0xff);
	
							String imageIdStr = StringUtils.toHex(imageId);
							String packetIdStr = StringUtils.toHex(packetId);
							System.out.println("Image: " + imageIdStr + "; packet=" + packetIdStr);
							
							
							ImageBuffer imageBuffer= packetCache.get(imageId);
							if (imageBuffer.addPacket(type, packetId, imageId, data, length)) {
								System.out.println("Image ready: " + imageId);
								packetCache.invalidate(imageId);
								imageQueue.add(new ImageData(null, imageBuffer.getImage()));
							}
						}
					} catch (SocketTimeoutException e) {
						// we set a socket timeout and catch this 
						// exception just in case stop() was called
						// and we don't want the caller to wait too
						// long in case no packet is arriving.
					}
				}
			} finally {
				packetCache.invalidateAll();
				imageQueue.clear();
				liveStreamStopped.signal();				
			}
			return null;
		});

	}
	
	public void stop() throws InterruptedException {
		if (liveStreamOpen.get()) {
			liveStreamOpen.set(true);
			liveStreamStopped.await();
		}
	}
	
	ImageData getNextImage() {
		return imageQueue.remove();
	}
	
	void clearImageBuffer() {
		packetCache.invalidateAll();
		imageQueue.clear();
	}

	@Value
	public class ImageMetadata {
		int width;
		int height;
		
		int shutterSpeedNumerator;
		int shutterSpeedDenominator;
		
		int focalValueNumerator;
		int focalValueDenominator;
		
		int expComp;
		boolean expCompWarning;
		
		int whiteBalance;
		boolean autoWhiteBalance;
		
		int isoSpeed;
		boolean isoSpeedWarning;
		
		int availableCapacity;
		int aspectRatioWidth;
		int aspectRatioHeight;
		
		int batteryLevel;
		
		boolean rotated;
		
		int focusField;
		boolean autoFocus;
	}
	
	@Value
	public class ImageData {
		ImageMetadata metadata;
		byte[]	data;
	}
	
	
}
