package org.olyapp.sdk.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="set")
public class SetParam {

	private SetParam() {
	}
	
	public SetParam(String value) {
		this();
		this.value = value;
	}
	
	String value;

    public String getValue() {
        return value;
    }

    @XmlElement
    public void setValue(String value) {
        this.value = value;
    }

}
