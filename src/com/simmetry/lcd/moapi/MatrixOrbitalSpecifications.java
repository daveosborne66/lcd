/**
 * Copyright 2011 - Simmetry Solutions
 * @author Ryan D Murphy
 */
package com.simmetry.lcd.moapi;

/*
 * The MatrixOrbitalSpecifications have been programmed from the Matrix Orbital LCD
 * version LK204-7T-1U Manual. Documentation on this device is accessible via
 * the Matrix Orbital corporate website: http://www.matrixorbital.com
 */
public class MatrixOrbitalSpecifications {

	// Defines the numerical value of the first row of the Matrix Orbital
	// character LCD
	public static final int FIRST_LCD_ROW = 1;
	
	// Defines the numerical value of the last row of the Matrix Orbital
	// character LCD
	public static final int LAST_LCD_ROW = 7;
	
	// Defines the length in characters of each row of the Matrix Orbital LCD
	public static final int ROW_LENGTH = 27;
	
	// Defines the pixel size in width and height of the LCD
	public static final int LCD_WIDTH = 192;
	public static final int LCD_HEIGHT = 64;
}
