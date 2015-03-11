package org.olyapp.sdk.impl;

import lombok.Value;

import org.olyapp.sdk.Frame;
import org.olyapp.sdk.FrameMetadata;

@Value
public class IFrame implements Frame {
	FrameMetadata metadata;
	byte[] data;
}
