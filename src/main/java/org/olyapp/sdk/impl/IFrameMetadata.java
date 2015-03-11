package org.olyapp.sdk.impl;

import lombok.Value;

import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.Fraction;
import org.olyapp.sdk.FrameMetadata;

@Value
public class IFrameMetadata implements FrameMetadata {

	Dimensions dimensions;
	
	Fraction shutterSpeed;
	Fraction aperture;
	int expComp;
	boolean expCompWarning;
	
	int whiteBalance;
	boolean autoWhiteBalance;
	
	int ISO;
	boolean ISOWarning;
	
	int availableCapacity;

	Fraction aspectRatio;
	
	int batteryLevel;
	
	boolean rotated;
	
	int focusField;
	boolean autoFocus;

}
