package org.olyapp.sdk.xml;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="connectmode")
public class ConnectMode {
    
    protected String mode;
    
	@XmlValue
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
