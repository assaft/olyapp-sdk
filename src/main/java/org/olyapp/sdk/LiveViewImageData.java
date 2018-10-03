package org.olyapp.sdk;

import lombok.Value;

@Value
public class LiveViewImageData {
	int imageId;
	LiveViewImageMetadata metadata;
	byte[]	data;
}