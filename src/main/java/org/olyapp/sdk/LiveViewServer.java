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

import org.olyapp.sdk.utils.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiveViewServer {

	private final static int SOCKET_TIMEOUT_MS = 500;

	private final ExecutorService listenerExecutor;
	private final ExecutorService handlerExecutor;
	private final AtomicBoolean liveStreamOpen;
	private final LoadingCache<Integer, LiveViewImageBuffer> packetCache;
	private final LoadingCache<Integer, Boolean> errorCache;
	private final LinkedBlockingDeque<LiveViewImageData> imageQueue;
	private final byte[] buffer;
	
	private final Lock lock;
	private final Condition liveStreamStopped; 

	private final static LiveViewServer instance = new LiveViewServer();
	
	public static LiveViewServer getInstance() {
		return instance;
	}
	
	private LiveViewServer() {
		this.listenerExecutor = Executors.newSingleThreadExecutor();
		this.handlerExecutor = Executors.newSingleThreadExecutor();
		this.liveStreamOpen = new AtomicBoolean(false);
		this.packetCache = CacheBuilder.newBuilder()
			    .maximumSize(1000)
			    .expireAfterAccess(2, TimeUnit.SECONDS)
			    .build(
			        new CacheLoader<Integer, LiveViewImageBuffer>() {
			          public LiveViewImageBuffer load(Integer i) {
			        	  return new LiveViewImageBuffer();
			          }
			        });
		this.errorCache = CacheBuilder.newBuilder()
			    .maximumSize(1000)
			    .expireAfterAccess(2, TimeUnit.SECONDS)
			    .build(
			        new CacheLoader<Integer, Boolean>() {
			          public Boolean load(Integer i) {
			        	  return false;
			          }
			        });
		this.imageQueue = new LinkedBlockingDeque<>(1000);
		this.lock = new ReentrantLock();
		this.liveStreamStopped = lock.newCondition();
		this.buffer = new byte[1536];
	}
	
	@Synchronized
	public void start(int port, LiveViewHandler handler, long timeout) throws InterruptedException, ProtocolError {

		// sanity
		if (liveStreamOpen.get()) {
			log.error("Live stream is already open");
			return;
		}

		try {
			log.info("start() - Passing task to worker thread");

			listenerExecutor.execute(()->{
				log.debug("Live stream thread invoked");					
	
				int currentSessionId = -1;
				liveStreamOpen.set(true);

				boolean timeoutExpired = false;
				long lastPacketTime = System.currentTimeMillis();
				long timeSinceLastPacket = 0;
				
				try (DatagramSocket serverSocket = new DatagramSocket(port)) {
					serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
					log.debug("UDP socket is ready - entering mainloop");
					while(liveStreamOpen.get() && !timeoutExpired) {
						try {
							DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
							serverSocket.receive(receivePacket);
							lastPacketTime = System.currentTimeMillis();
							
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
		
								if (!errorCache.get(imageId)) {
									LiveViewImageBuffer imageBuffer= packetCache.get(imageId);
									try {
										if (imageBuffer.addPacket(type, packetId, imageId, data, length)) {
											byte[] imageBytes = imageBuffer.getImage();
											LiveViewImageMetadata imageMetadata = new LiveViewImageMetadata(imageBuffer,imageId);
											log.debug("Image " + StringUtils.toHex(imageId) + " is ready");
											handlerExecutor.execute(()->{
												handler.onImage(new LiveViewImageData(imageId, imageMetadata, imageBytes));
											});
											packetCache.invalidate(imageId);
										}
									} catch (Exception e) {
										log.info("Image: " + StringUtils.toHex(imageId) + " has an error: [" + e.getMessage() + "]");
										errorCache.put(imageId,true);
										packetCache.invalidate(imageId);
									}
								} else {
									log.debug("Ignoring packet " + StringUtils.toHex(packetId) + " of bad image: " + StringUtils.toHex(imageId));
								}
							}
						} catch (SocketTimeoutException e) {
							// we set a socket timeout and catch this 
							// exception just in case stop() was called
							// and we don't want the caller to wait too
							// long in case no packet has arrived.
							log.debug("No data in socket (camera is out-of-range or stopped sending)");
							timeSinceLastPacket = System.currentTimeMillis()-lastPacketTime;
							timeoutExpired = timeSinceLastPacket > timeout;
						}
					}
					
					if (timeoutExpired) {
						log.debug("Out of mainloop because timeout expired");
					} else {
						log.debug("Out of mainloop based on request");					
						lock.lock();
						try {
							log.debug("Signaling live stream stop");
							liveStreamStopped.signal();
						} finally {
							lock.unlock();
					    }
					}
				} catch (Exception e) {
					log.error("Exception: " + e.getMessage());
					throw new RuntimeException(e);
				} finally {
					packetCache.invalidateAll();
					errorCache.invalidateAll();
					imageQueue.clear();
					liveStreamOpen.set(false);
					if (timeoutExpired) {
						handler.onTimeout(timeSinceLastPacket);
					}
				}
			});
		} catch (Exception e) {
			throw new ProtocolError(e.getMessage());
		}
	}
	
	@Synchronized
	public void stop() throws InterruptedException {
		if (liveStreamOpen.get()) {
			liveStreamOpen.set(false);
			lock.lock();
			try {
				log.debug("Waiting for live stream ending signal");
				liveStreamStopped.await(2,TimeUnit.SECONDS);
				log.debug("Signal received");
			} finally {
				lock.unlock();
		    }
		}
	}

	public boolean isLiveStreamOpen() {
		return liveStreamOpen.get();
	}
	
}
