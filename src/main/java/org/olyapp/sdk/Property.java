package org.olyapp.sdk;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum Property {
    TOUCH_ACTIVE_FRAME	("touchactiveframe"),
    TAKE_MODE			("takemode"),
    DRIVE_MODE			("drivemode"),
    FOCAL_VALUE			("focalvalue"),
    EXP_COMP			("expcomp"),
    SHUTTER_SPEED_VALUE	("shutspeedvalue"),
    ISO_SPEED_VALUE		("isospeedvalue"),
    WB_VALUE			("wbvalue"),
    NOISE_REDUCTION		("noisereduction"),
    LOW_VIB_TIME		("lowvibtime"),
    BULB_TIME_LIMIT		("bulbtimelimit"),
    ART_FILTER			("artfilter"),
    DIGITAL_TELECON		("digitaltelecon"),
    DESC_LIST			("desclist");
    
    String name;
};







