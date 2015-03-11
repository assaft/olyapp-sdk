package org.olyapp.sdk.impl;

import org.olyapp.sdk.Fraction;

import lombok.Value;

@Value
public class IFraction implements Fraction {
	int numerator;
	int denominator;
}
