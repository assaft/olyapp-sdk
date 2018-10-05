package org.olyapp.sdk;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.olyapp.sdk.utils.SimpleImageInfo;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class LiveViewImageMetadata {

	private static boolean DEBUG_METADATA = false;
	
	byte[] buffer;
	
	Dimensions dimensions;
	
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
	int orientationDegrees;
	
	int focusField;
	boolean autoFocus;
	
	public LiveViewImageMetadata(LiveViewImageBuffer imageBuffer, int imageId) throws IOException {
		this.buffer = imageBuffer.getMetadata().clone();
		orientation					= interpret(buffer,0x12,2);
		orientationDegrees			= getOrientationDeg(orientation);
		shutterSpeedNumerator  		= interpret(buffer,0x58,2); 
		shutterSpeedDenominator  	= interpret(buffer,0x5A,2);
		focalValueNumerator  		= interpret(buffer,0x6A,2);
		focalValueDenominator  		= interpret(buffer,0x6C,2); 
		expCompNumerator			= interpret(buffer,0x78,4);
		expCompDenominator			= 10;
		isoSpeed					= interpret(buffer,0x82,2);
		autoIsoSpeed				= interpret(buffer,0x84,2)==1;
		isoSpeedWarning				= interpret(buffer,0x8A,2)==1;
		whiteBalance				= interpret(buffer,0x92,2);
		autoWhiteBalance			= interpret(buffer,0x94,2)==1;
		aspectRatioWidth			= interpret(buffer,0x9D,2);
		aspectRatioHeight			= interpret(buffer,0x9F,2);
		expWarning 					= interpret(buffer,0xA6,2)==1;
		focalLength					= interpret(buffer,0xB6,2);
		
		SimpleImageInfo	simpleImageInfo = new SimpleImageInfo(imageBuffer.getImage());
		dimensions = new Dimensions(simpleImageInfo.getWidth(),simpleImageInfo.getHeight());

		focusField = 30;
		autoFocus = true;
		
		if (DEBUG_METADATA) {
			log.debug(toString());
		}
	}
	
	public String toString() {
		return	"shutter speed: " + shutterSpeedNumerator + "/" + shutterSpeedDenominator + "\n" +
				"focalValue: " + focalValueNumerator + "/" + focalValueDenominator + "\n" +
				"expComp: " + expCompNumerator + "/" + expCompDenominator + "\n" +
				"iso-speed: " + isoSpeed + " (auto? " + autoIsoSpeed + ", warning? " + isoSpeedWarning + ")" + "\n" +
				"white-balance: " + whiteBalance + " (auto? " + autoWhiteBalance + ")" + "\n" +
				"aspect-ratio: " + aspectRatioWidth + "x" + aspectRatioHeight + "\n" +
				"focalLength: " + focalLength + "mm" + "\n" +
				"exposure warning: " + expWarning + "\n" +
				"orientation: " + orientation + "(deg: " + orientationDegrees + ")" + "\n" +
				"dimensions: " + dimensions;
	}
	
	private int getOrientationDeg(int orientation) {
		int result;
		switch (orientation) {
			case 1: result = 0;		break;			
			case 3: result = 180;	break;			
			case 6: result = 90; 	break;			
			case 8: result = 270;	break;			
			default:result = -1;	break;
		}
		return result;
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