package org.olyapp.sdk.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="desc")
public class Desc {

	String propName;
	String attribute;
	String value;
	String enumTag;

    public String getPropName() {
        return propName;
    }

    @XmlElement(name = "propname")
    public void setPropName(String propName) {
        this.propName = propName;
    }

	public String getAttribute() {
		return attribute;
	}

    @XmlElement(name = "attribute")
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

    @XmlElement(name = "value")
	public void setValue(String value) {
		this.value = value;
	}

	public String getEnumTag() {
		return enumTag;
	}

    @XmlElement(name = "enum")
    public void setEnumTag(String enumTag) {
		this.enumTag = enumTag;
	}
    
    
    
}
