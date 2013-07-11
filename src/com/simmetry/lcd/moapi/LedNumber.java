/**
 * Copyright 2011 - Simmetry Solutions
 * @author Ryan D Murphy
 */
package com.simmetry.lcd.moapi;

/*
 * Selects the LED, is used in conjunction with LedPower and LedColor to illuminate a specific LED with a selected color
 */
public enum LedNumber {
    NULL,
    ONE,	// Top
    TWO,	// Middle
    THREE,	// Bottom
    ALL		// All three
}
