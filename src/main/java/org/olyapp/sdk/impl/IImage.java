package org.olyapp.sdk.impl;

import lombok.Value;

import org.olyapp.sdk.Dimensions;
import org.olyapp.sdk.Image;

@Value
public class IImage implements Image {
	byte[] data;
	Dimensions dimensions;
}
