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
public class Rectangle {
	Point upperLeft = new Point(0,0);
	Point lowerRight = new Point(0,0);

	public Rectangle() {
		upperLeft = new Point(Point.minX, Point.minY);
		lowerRight = new Point(Point.maxX, Point.maxY);
	}
	
	public Rectangle(int upper_left_x, int upper_left_y, int lower_right_x, int lower_right_y) {
		upperLeft.x_loc = upper_left_x;
		upperLeft.y_loc = upper_left_y;
		lowerRight.x_loc = lower_right_x;
		lowerRight.y_loc = lower_right_y;
	}
	
	public Rectangle(Point upper_left, Point lower_right){
		upperLeft.x_loc = upper_left.x_loc;
		upperLeft.y_loc = upper_left.y_loc;
		lowerRight.x_loc = lower_right.x_loc;
		lowerRight.y_loc = lower_right.y_loc;
	}
}
