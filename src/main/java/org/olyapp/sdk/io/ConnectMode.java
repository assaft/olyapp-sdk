package org.olyapp.sdk.io;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="connectmode")
public class ConnectMode {
    
	@XmlValue
    protected String mode;
    
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
