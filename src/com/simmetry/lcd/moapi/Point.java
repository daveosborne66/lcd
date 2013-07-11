/**
 * Copyright 2012 - Simmetry Solutions
 * @author daveosborne
 */
package com.simmetry.lcd.moapi;

/**
 * The Matrix Orbital LCD reference system is (0,0) in the upper left side of the LCD.
 * For the GLK19264-7T-1U model:
 * X location of the pixel, measured left (0) to right (191)
 * Y location of the pixel, measured from bottom (0) to top (63)
 * All use of x,y pairs or quads in passing data to the LCD to render depends the use of coordinate pairs
 * within these bounds.
 */
public class Point {
	static final public int minX = 0;
	static final public int minY = 0;
	static final public int maxX = MatrixOrbitalSpecifications.LCD_WIDTH-1;
	static final public int maxY = MatrixOrbitalSpecifications.LCD_HEIGHT-1;
	
	int x_loc = 0;
	int y_loc = 0;
	
	public Point (){
		x_loc = 0;
		y_loc = 0;
	}
	
	public Point( int x, int y){
		x_loc = x;
		y_loc = y;
	}

}
