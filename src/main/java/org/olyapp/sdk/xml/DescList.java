package org.olyapp.sdk.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "desclist")
@XmlAccessorType (XmlAccessType.FIELD)
public class DescList
{
    @XmlElement(name = "desc")
    private List<Desc> descList = null;
 
    public List<Desc> getDescriptions() {
        return descList;
    }
 
    public void setDescriptions(List<Desc> descList) {
        this.descList = descList;
    }
}