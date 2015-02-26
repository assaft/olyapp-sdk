package org.olyapp.sdk;

import java.util.NoSuchElementException;

public enum ShotType {

	PreShot,
	PostShot,
	Done {
		@Override
        	public ShotType next() {
				throw new NoSuchElementException();
			};
	};

	public ShotType next() {
	    // No bounds checking required here, because the last instance overrides
	    return values()[ordinal() + 1];
	}

}
