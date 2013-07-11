/**
 * Copyright 2011 - Simmetry Solutions
 * @author Ryan D Murphy
 */
package com.simmetry.lcd.events;

// Java package imports
import java.util.*;

// Internal package imports
import com.simmetry.lcd.moapi.LcdButton;

/*
 * @author Ryan Murphy
 * This class defines the LCD button press event
 */
public class LcdButtonPressEvent extends EventObject {

	// The EventObject class that we extend is serializable. We create this
	// because if we do not the compiler will display a warning that it is
	// serializable, but does not have a UUID which is used during the
	// deserialization process. It doesn't matter if we aren't serializing
	// the class in this implementation.
	public static final long serialVersionUID = 
		(UUID.randomUUID()).getLeastSignificantBits();
	
	LcdButton pressed = LcdButton.NULL;
	
	public LcdButtonPressEvent(Object source, LcdButton button) {
		
		super(source);
		pressed = button;
	}
	
	public LcdButton getButton() {
		
		return pressed;
	}
}
