/**
 * Copyright 2011 - Simmetry Solutions
 * @author Ryan D Murphy
 */
package com.simmetry.lcd.moapi;

// The MatrixOrbitalCommands have been programmed from the Matrix Orbital LCD
// version LK204-7T-1U Manual. Documentation on this device is accessible via
// the Matrix Orbital corporate website: http://www.matrixorbital.com
public class MatrixOrbitalCommands {

    // All Matrix Orbital commands must start with the START_BYTE. It is a
    // special command to the LCD that notifies the processor not to print the
    // incoming command to the screen as text. If the START_BYTE is not sent,
    // any commands will be processed for display on the LCD.
    public static final byte START_BYTE = (byte)0xFE;
    
    // Undocumented feature of the LCD provided by Matrix Orbital technical staff
    // to perform a warm reset of the LCD.
    public static final byte WARM_RESET = (byte)0xFD;
    
    // Text Function Command Bytes
    public static final byte CLEAR_SCREEN = (byte)0x58; // This command clears the LCD screen of all characters
    public static final byte RESET_CURSOR = (byte)0x48; // Resets the cursors position to the upper left corner of the LCD
    public static final byte SET_CURSOR = (byte)0x47; // Sets the cursors position to the entered column and row
    
    // Serial Control Command Bytes
    // Baud Rate Bytes
    public static final byte SET_BAUD_RATE = (byte)0x39;
    public static final byte BAUD_9600 = (byte)0xCF;
    public static final byte BAUD_14400 = (byte)0x8A;
    public static final byte BAUD_19200 = (byte)0x67; // LCD Default
    public static final byte BAUD_28800 = (byte)0x44;
    public static final byte BAUD_38400 = (byte)0x33;
    public static final byte BAUD_57600 = (byte)0x22;
    public static final byte BAUD_76800 = (byte)0x19;
    public static final byte BAUD_115200 = (byte)0x10;
    // Sets serial comms flow control
    public static final byte FLOW_CONTROL_ON = (byte)0x3A;
    public static final byte FLOW_CONTROL_OFF = (byte)0x3B;
    public static final byte FLOW_CONTROL_FULL = (byte)0x20; // flag when 32 bytes are left in buffer
    public static final byte FLOW_CONTROL_EMPTY = (byte)0x78; // flag when 120 bytes are left in buffer
    // File System Data Lock
    public static final byte SAVE_DATA_LOCK = (byte)0xCB;
    public static final byte SET_DATA_LOCK = (byte)0xF5;
    public static final byte LOCK_RESERVED0 = (byte)0x00;
    public static final byte LOCK_RESERVED1 = (byte)0x01;
    public static final byte LOCK_RESERVED2 = (byte)0x02;
    public static final byte LOCK_SPEED = (byte)0x03;
    public static final byte LOCK_SETTINGS = (byte)0x04;
    public static final byte LOCK_FILESYSTEM = (byte)0x05;
    public static final byte LOCK_COMMAND = (byte)0x06;
    public static final byte LOCK_DISPLAY = (byte)0x07;
    
    // General Output Command Bytes
    // The following commands control the general purpose outputs to the left
    // of the LCD. There are separate commands for turning the outputs ON/OFF.
    // Two outputs control the color each tri-color LED as follows:
    //   LED 1 = OUTPUT 2 & OUTPUT 1
    //   LED 2 = OUTPUT 4 & OUTPUT 3
    //   LED 3 = OUTPUT 6 & OUTPUT 5
    // The LED colors are controlled as follows:
    //   YELLOW = OFF & OFF
    //   GREEN = OFF & ON
    //   RED = ON & OFF
    //   OFF = ON & ON
    public static final byte GP_OUTPUT_PWR_OFF = (byte)0x56;
    public static final byte GP_OUTPUT_PWR_ON = (byte)0x57;
    public static final byte GP_OUTPUT_ONE = (byte)0x01;
    public static final byte GP_OUTPUT_TWO = (byte)0x02;
    public static final byte GP_OUTPUT_THREE = (byte)0x03;
    public static final byte GP_OUTPUT_FOUR = (byte)0x04;
    public static final byte GP_OUTPUT_FIVE = (byte)0x05;
    public static final byte GP_OUTPUT_SIX = (byte)0x06;
    
    // Display Control Command Bytes
    // Used to set the LED brightness
    public static final byte SAVE_BACKLIGHT_BRIGHTNESS = (byte)0x98;
    public static final byte SET_BACKLIGHT_BRIGHTNESS = (byte)0x99;
    public static final byte BACKLIGHT_OFF = (byte)0x00;
    public static final byte BACKLIGHT_LOW = (byte)0x55;
    public static final byte BACKLIGHT_MEDIUM = (byte)0xAA;
    public static final byte BACKLIGHT_HIGH = (byte)0xFF;
    // Used to set the LED contrast
    public static final byte SAVE_BACKLIGHT_CONTRAST = (byte)0x91;
    public static final byte SET_BACKLIGHT_CONTRAST = (byte)0x50;
    
    // Drawing Command Bytes
    public static final byte SET_DRAWING_COLOR = (byte)0x63;
    public static final byte DRAW_PIXEL = (byte)0x70;
    public static final byte DRAW_LINE = (byte)0x6C;
    public static final byte DRAW_RECTANGLE = (byte)0x72;
    public static final byte DRAW_SOLID_RECTANGLE = (byte)0x78;
    
    // Key Pad Event Bytes
    // Bytes are sent back from the keypad when buttons are pressed
    public static final byte KEYPAD_UP_ARROW = (byte)0x42;
    public static final byte KEYPAD_DOWN_ARROW = (byte)0x48;
    public static final byte KEYPAD_LEFT_ARROW = (byte)0x44;
    public static final byte KEYPAD_RIGHT_ARROW = (byte)0x43;
    public static final byte KEYPAD_CENTER_BUTTON = (byte)0x45;
    public static final byte KEYPAD_TOPLEFT_BUTTON = (byte)0x41;
    public static final byte KEYPAD_BOTTOMLEFT_BUTTON = (byte)0x47;
}
