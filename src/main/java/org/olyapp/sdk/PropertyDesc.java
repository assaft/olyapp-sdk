package org.olyapp.sdk;

import java.util.List;

import lombok.Value;

@Value
public class PropertyDesc {
	Property property;
	String value;
	String type;
	List<String> values;
}
