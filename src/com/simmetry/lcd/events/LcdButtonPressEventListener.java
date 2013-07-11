/**
 * Copyright 2011 - Simmetry Solutions
 * @author Ryan D Murphy
 */
package com.simmetry.lcd.events;

import java.util.*;

/*
 * @author Ryan Murphy
 * An interface that provides an event listener for classes that wish to be
 * notified when a button is pressed on the Matrix Orbital LCD.
 */
public interface LcdButtonPressEventListener extends EventListener {

	public void lcdButtonPressAction(LcdButtonPressEvent e);
	
}
