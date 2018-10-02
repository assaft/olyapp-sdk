package org.olyapp.sdk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.olyapp.sdk.utils.SimpleImageInfo;
import org.olyapp.sdk.utils.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiveViewServer {

	private final static int SOCKET_TIMEOUT_MS = 200;

	private final ExecutorService listenerExecutor;
	private final ExecutorService handlerExecutor;
	private final AtomicBoolean liveStreamOpen;
	private final LoadingCache<Integer, ImageBuffer> packetCache;
	private final LoadingCache<Integer, Boolean> errorCache;
	private final LinkedBlockingDeque<ImageData> imageQueue;
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
			        new CacheLoader<Integer, ImageBuffer>() {
			          public ImageBuffer load(Integer i) {
			        	  return new ImageBuffer();
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
	
	public void start(int port, Consumer<ImageData> consumer) throws InterruptedException, ProtocolError {
		log.info("start() - Started");
		
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
	
				try (DatagramSocket serverSocket = new DatagramSocket(port)) {
					serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
					log.debug("UDP socket is ready - entering mainloop");					
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
		
								if (!errorCache.get(imageId)) {
									ImageBuffer imageBuffer= packetCache.get(imageId);
									try {
										if (imageBuffer.addPacket(type, packetId, imageId, data, length)) {
											byte[] imageBytes = imageBuffer.getImage();
											ImageMetadata imageMetadata = new ImageMetadata(imageBuffer,imageId);
											log.debug("Image " + StringUtils.toHex(imageId) + " is ready");
											handlerExecutor.execute(()->{
												consumer.accept(new ImageData(imageId, imageMetadata, imageBytes));
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
						}
					}
					log.debug("Out of mainloop based on request");					
					lock.lock();
					try {
						log.debug("Signaling live stream stop");
						liveStreamStopped.signal();
					} finally {
						lock.unlock();
				    }
				} catch (Exception e) {
					log.error("Exception: " + e.getMessage());
					throw new RuntimeException(e);
				} finally {
					packetCache.invalidateAll();
					errorCache.invalidateAll();
					imageQueue.clear();
					liveStreamOpen.set(false);
					log.debug("Clean-ups");
				}
			});
		} catch (Exception e) {
			throw new ProtocolError(e.getMessage());
		}
		log.info("start() - Finished");
	}
	
	public void stop() throws InterruptedException {
		log.info("stop() - Started");
		if (liveStreamOpen.get()) {
			liveStreamOpen.set(false);
			lock.lock();
			try {
				log.debug("Waiting for live stream signal");
				liveStreamStopped.await(2,TimeUnit.SECONDS);
				log.debug("Signal received");
			} finally {
				lock.unlock();
		    }
		}
		log.info("stop() - Finished");
	}

	@Value
	public class ImageMetadata {
		
		byte[] metadata;
		
		int width;
		int height;
		
		int shutterSpeedNumerator;
		int shutterSpeedDenominator;
		
		int focalValueNumerator;
		int focalValueDenominator;
		
		int expCompNumerator;
		int expCompDenominator;

		boolean expWarning;
		
		int whiteBalance;
		boolean autoWhiteBalance;
		
		int isoSpeed;
		boolean autoIsoSpeed;
		boolean isoSpeedWarning;
		
		int aspectRatioWidth;
		int aspectRatioHeight;
		
		int focalLength;

		int orientation;
		
		int focusField;
		boolean autoFocus;
		
		public ImageMetadata(ImageBuffer imageBuffer, int imageId) throws IOException {
			this.metadata = imageBuffer.getMetadata().clone();
			
			try {
				Files.write(Paths.get(imageId + ".bin"), metadata);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			orientation					= interpret(metadata,0x12,2);
			shutterSpeedNumerator  		= interpret(metadata,0x58,2); 
			shutterSpeedDenominator  	= interpret(metadata,0x5A,2);
			focalValueNumerator  		= interpret(metadata,0x6A,2);
			focalValueDenominator  		= interpret(metadata,0x6C,2); 
			expCompNumerator			= interpret(metadata,0x78,4);
			expCompDenominator			= 10;
			isoSpeed					= interpret(metadata,0x82,2);
			autoIsoSpeed				= interpret(metadata,0x84,2)==1;
			isoSpeedWarning				= interpret(metadata,0x8A,2)==1;
			whiteBalance				= interpret(metadata,0x92,2);
			autoWhiteBalance			= interpret(metadata,0x94,2)==1;
			aspectRatioWidth			= interpret(metadata,0x9D,2);
			aspectRatioHeight			= interpret(metadata,0x9F,2);
			expWarning 					= interpret(metadata,0xA6,2)==1;
			focalLength					= interpret(metadata,0xB6,2);
			
			SimpleImageInfo	simpleImageInfo = new SimpleImageInfo(imageBuffer.getImage());
			width = simpleImageInfo.getWidth();
			height = simpleImageInfo.getHeight();
			
			log.debug("shutter speed: " + shutterSpeedNumerator + "/" + shutterSpeedDenominator);
			log.debug("focalValue: " + focalValueNumerator + "/" + focalValueDenominator);
			log.debug("expComp: " + expCompNumerator + "/" + expCompDenominator);
			log.debug("iso-speed: " + isoSpeed + " (auto? " + autoIsoSpeed + ") + (warning? " + isoSpeedWarning + ")");
			log.debug("white-balance: " + whiteBalance + " (auto? " + autoWhiteBalance + ")");
			log.debug("aspect-ratio: " + aspectRatioWidth + "x" + aspectRatioHeight);
			log.debug("focalLength: " + focalLength + "mm");
			log.debug("exposure warning: " + expWarning);
			log.debug("orientation: " + orientation);
			log.debug("dimensions: " + width + "x" + height);

			focusField = 30;
			autoFocus = true;
		}
		
		private int interpret(byte[] buffer, int offset, int count) {
			int result;
			if (count==1) {
				result = buffer[offset] & 0xFF;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.allocate(count);
				byteBuffer.put(buffer, offset, count);
				byteBuffer.rewind();
				result = count==2 ? byteBuffer.getShort() : byteBuffer.getInt();
			}
			return result;
		}
	}
	
	@Value
	public class ImageData {
		int imageId;
		ImageMetadata metadata;
		byte[]	data;
	}
	
	
}
