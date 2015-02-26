package org.olyapp.sdk.impl;

import org.olyapp.sdk.Frame;
import org.olyapp.sdk.ImageProperties;

public class IFrame implements Frame {
	private Object data;
	private ImageProperties iamgeProperties;
	
	public IFrame(ImageProperties imageProperties, Object data) {
		super();
		this.data = data;
		this.iamgeProperties = imageProperties;
	}
	
	@Override
	public ImageProperties getImageProperties() {
		return iamgeProperties;
	}

	@Override
	public Object getData() {
		return data;
	}
	
}
