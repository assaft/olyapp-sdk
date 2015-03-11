package org.olyapp.sdk.impl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

import org.olyapp.sdk.CameraProperty;

@Value
@AllArgsConstructor 
public class ICameraProperty implements CameraProperty {
	
	String propName;
	String value;
	String propertyType;
	List<String> values;

	public ICameraProperty(CameraProperty property, String value) {
		this(property.getPropName(),value,property.getPropertyType(),property.getValues());
	}
	
	@Override
	public String toString() {
		return propName + ": " + value + " ("+values.toString()+")";
	}
	
}
