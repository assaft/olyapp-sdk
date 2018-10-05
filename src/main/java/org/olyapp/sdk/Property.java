package org.olyapp.sdk;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum Property {
    TOUCH_ACTIVE_FRAME	("touchactiveframe",false),
    TAKE_MODE			("takemode",false),
    DRIVE_MODE			("drivemode",false),
    FOCAL_VALUE			("focalvalue",false),
    EXP_COMP			("expcomp",false),
    SHUTTER_SPEED_VALUE	("shutspeedvalue",false),
    ISO_SPEED_VALUE		("isospeedvalue",false),
    WB_VALUE			("wbvalue",false),
    NOISE_REDUCTION		("noisereduction",false),
    LOW_VIB_TIME		("lowvibtime",false),
    BULB_TIME_LIMIT		("bulbtimelimit",false),
    ART_FILTER			("artfilter",false),
    DIGITAL_TELECON		("digitaltelecon",false),
    DESC_LIST			("desclist",true);
    
    String name;
	boolean superProperty;
};







