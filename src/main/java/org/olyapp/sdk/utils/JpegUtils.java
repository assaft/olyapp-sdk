package org.olyapp.sdk.utils;

import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.impl.IDimension;

public class JpegUtils {

	public static Dimensions getDimensions(byte[] data) throws InvalidJpegFormat {
		Dimensions dimension = null;
		if (data[0]==0xFF && data[1]==0xD8) {
			int length = data.length;
			int i = 2;
			while (data[i] == 255 && i+2<length && dimension==null) {
				int marker = data[i];
				int len = (data[i+1] << 8) | data[i+2];
				i+=2;
				if ((marker == 192 || marker == 193 || marker == 194) && i+4<length) {
					dimension = new IDimension(
							(data[i+1] << 8) | data[i+2],
							(data[i+3] << 8) | data[i+4]);
				}
				i+=len-2;
			}
		} else {
			throw new InvalidJpegFormat("SOI-Segment not found");
		}
		
		if (dimension==null) {
			throw new InvalidJpegFormat("Failed to read image dimensions");
		}

		return dimension;
	}
}
