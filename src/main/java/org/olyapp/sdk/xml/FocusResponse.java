package org.olyapp.sdk.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="response")
public class FocusResponse {

	String affocus;
	String afframepoint;
	String afframesize;
	
	public String getAffocus() {
		return affocus;
	}
	
	@XmlElement
	public void setAffocus(String affocus) {
		this.affocus = affocus;
	}
	
	public String getAfframepoint() {
		return afframepoint;
	}
	
	@XmlElement
	public void setAfframepoint(String afframepoint) {
		this.afframepoint = afframepoint;
	}
	
	public String getAfframesize() {
		return afframesize;
	}
	@XmlElement
	public void setAfframesize(String afframesize) {
		this.afframesize = afframesize;
	}
	
}
