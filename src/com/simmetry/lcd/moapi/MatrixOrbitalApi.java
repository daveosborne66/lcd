/**
 * Copyright 2011 - Simmetry Solutions
 * @author Ryan D Murphy
 * @author Dave Osborne
 */
package com.simmetry.lcd.moapi;

// Java package imports
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;


// Internal package imports
import com.simmetry.lcd.events.*;

//
// The MatrixOrbitalApi is programmed to interface with a Matrix Orbital LCD
// version LK204-7T-1U. Documentation on this device is accessible via the
// Matrix Orbital corporate website: http://www.matrixorbital.com
//
// Lessons learned:
// 1) The flow control message will be sent by the LCD each time it receives data and is in the state where the
//    buffer is above the 'full' limit set when flow control is turned on. So frequently the debug messages will indicate
//    that additional bytes are being written after the buffer full limit has been exceeded, which seems to indicate
//    that the LCD is not quick to indicate the buffer full case. This means you have to tune the parameters:
//    FLOW_CONTROL_FULL and FLOW_CONTROL_EMPTY in the MatrixOrbitalCommands class, and the 'sleep_time_millis' value set
//    in this.run() method.
// 2) We're using the default baud rate of 19200. Conjecture is that there is no benefit to trying to use a higher
//    baud rate. The receipt of the flow control flags seem to indicate that at the default baud rate we can easily exceed
//    buffer, so increasing the baud rate will only allow us to overflow the buffer faster. The various baud rates that
//    are supported by the LCD are likely only to allow for integration with serial devices that only operate at specific
//    baud rates, rather than any benefit to operating at 115000.
//
public class MatrixOrbitalApi implements Runnable {
    
    // Variables For Socket Communication
    private String ipAddress = ""; // IP Address of Device
    private int port = 0;          // Communication Port of Device
    //private byte baud = 0;
    private Socket apiSocket;      // TCP Communications Socket
    
    // Streams For Socket Communication
    private InputStream apiReader;  // Used for input from LCD
    private OutputStream apiWriter; // Used for output to LCD
    
    // Serial Communications
    private SerialPort serialPort;
	private boolean connected = false;
	private boolean flowing = true;
    
    // Containers for input/output to/from the socket streams
    //private Queue<Byte> inputBytes = new LinkedList<Byte>();
    //private Queue<Byte> outputBytes = new LinkedList<Byte>();
    private ConcurrentLinkedQueue<Byte> inputBytes = new ConcurrentLinkedQueue<Byte>();
    private ConcurrentLinkedQueue<Byte> outputBytes = new ConcurrentLinkedQueue<Byte>();
    
    // Container for event listeners when buttons are pressed
    private Vector<LcdButtonPressEventListener> eventListenerVector =
    	new Vector<LcdButtonPressEventListener>();
    
    // Class Threads
    private Thread serviceThread = new Thread(this); // Thread to monitor socket
    
    // Used to keep track of the colors of the individual LEDs
    private LedColor ledOneColor = LedColor.GREEN;
    private LedColor ledTwoColor = LedColor.GREEN;
    private LedColor ledThreeColor = LedColor.GREEN;
    
    // Used to keep track of the power of the individual LEDs
    private LedPower ledOnePower = LedPower.OFF;
    private LedPower ledTwoPower = LedPower.OFF;
    private LedPower ledThreePower = LedPower.OFF;
    
    // Used to keep track of the blink rate in HZ of the individual LEDs
    private int ledOneFreq = 0;
    private int ledTwoFreq = 0;
    private int ledThreeFreq = 0;
    
    // class logger
    private static final Logger logger = Logger.getLogger(com.simmetry.lcd.moapi.MatrixOrbitalApi.class);
    
    
    public MatrixOrbitalApi() {
        
        // This thread runs in the background to manage communications to and
        // from the Matrix Orbital LCD and to execute events that are caused by
        // data returned from the LCD. We set this thread as daemon so when 
        // we shutdown, the program does not wait for the thread to stop prior
        // to exiting the main program.
        serviceThread.setDaemon(true);
    }
    
    /**
     * Connects the LCD via serial comm, using the provided port name and at the 
     * default baud rate of 19200.
     * @param portName The port name for the connection
     * @return TRUE if connect is successful, FALSE if not
     */
	public boolean connect(String portName) {
		return connect(portName, MatrixOrbitalCommands.BAUD_19200);
	}
	
	/**
	 * 
	 * @param portName The port name for the connection
	 * @param speed The baud rate of the connection.The following discrete baud rates are supported:
     * BAUD_9600 = 0xCF
     * BAUD_14400 = 0x8A
     * BAUD_19200 = 0x67 // LCD Default
     * BAUD_28800 = 0x44
     * BAUD_38400 = 0x33
     * BAUD_57600 = 0x22
     * BAUD_76800 = 0x19
     * BAUD_115200 = 0x10
	 * @return TRUE if connect is successful, FALSE if not
	 */
	public boolean connect(String portName, byte speed) {
		CommPortIdentifier portIdentifier;
		boolean conn = false;
		int baud = 19200;
		int default_baud = 19200;
		
		// switch the baud rate to convert enum to int.
		// 19200 is the default for the LCD, so we skip that in this list.
		switch(speed){
			case MatrixOrbitalCommands.BAUD_9600:
				baud = 9600;
				break;
			case MatrixOrbitalCommands.BAUD_14400:
				baud = 14400;
				break;
			case MatrixOrbitalCommands.BAUD_28800:
				baud = 28800;
				break;
			case MatrixOrbitalCommands.BAUD_38400:
				baud = 38400;
				break;
			case MatrixOrbitalCommands.BAUD_57600:
				baud = 57600;
				break;
			case MatrixOrbitalCommands.BAUD_76800:
				baud = 76800;
				break;
			case MatrixOrbitalCommands.BAUD_115200:
				baud = 115200;
				break;
		}

		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if (portIdentifier.isCurrentlyOwned()) {
				logger.info("Error: Port is currently in use");
			} else {
				serialPort = (SerialPort) portIdentifier.open("Controller_LCD", 2000);
				serialPort.setSerialPortParams(default_baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				//serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);

				apiReader = serialPort.getInputStream();
				apiWriter = serialPort.getOutputStream();
				connected = true;
				
				//
				// test code
				//
				if(baud != default_baud){
					this.setBaudRate(speed);
					Thread.sleep(100);
					serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				}
				
				logger.info("connection on " + portName + " established");
				conn = true;
				
				this.setFlowControlOn();
			}
		} catch (NoSuchPortException e) {
			logger.error("the connection to " + portName + " could not be made", e);
		} catch (PortInUseException e) {
			logger.error("the connection to " + portName + " could not be made", e);
		} catch (UnsupportedCommOperationException e) {
			logger.error("the connection to " + portName + " could not be made", e);
		} catch (IOException e) {
			logger.error("the connection to " + portName + " could not be made", e);
		} catch (InterruptedException e) {
			logger.error("Thread exception on " + portName, e);
		}

        serviceThread.start();
        return conn;
	}
	
	
    /**
     * Performs a warm reset on the LCD hardware.
     */
    public synchronized void warmReset() {
    	
    	byte[] b = new byte[2];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.WARM_RESET;
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
	
	///
	/// This section provides Serial Control functions
	///
    /**
     * Sets LCD serial comm flow control ON.
     */
    public synchronized void setFlowControlOn() {
    	
    	byte[] b = new byte[4];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.FLOW_CONTROL_ON;
        b[2] = MatrixOrbitalCommands.FLOW_CONTROL_FULL;
        b[3] = MatrixOrbitalCommands.FLOW_CONTROL_EMPTY;
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * Sets LCD serial comm flow control OFF.
     */
    public synchronized void setFlowControlOff() {
    	
    	byte[] b = new byte[2];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.FLOW_CONTROL_OFF;
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /*
     * Sets a remembered lock on the filesystem
     */
    public synchronized void setLockFilesystem(){
    	byte[] b = new byte[4];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.SAVE_DATA_LOCK;
        b[2] = MatrixOrbitalCommands.SET_DATA_LOCK;
        b[3] = MatrixOrbitalCommands.LOCK_FILESYSTEM;
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * Sets the LCD serial comm baud rate. The following discrete baud rates are supported:
     * BAUD_9600 = 0xCF
     * BAUD_14400 = 0x8A
     * BAUD_19200 = 0x67 // LCD Default
     * BAUD_28800 = 0x44
     * BAUD_38400 = 0x33
     * BAUD_57600 = 0x22
     * BAUD_76800 = 0x19
     * BAUD_115200 = 0x10
     * @param baudRate The baud rate (from MatrixOrbitalCommand list)
     */
    public synchronized void setBaudRate(byte baudRate) {
    	
    	byte[] b = new byte[3];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.SET_BAUD_RATE;
        b[2] = baudRate;
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    ///
    /// This section provides General Purpose Output functions
    ///
    /**
     * Sets the LED specified by the LED number to the given color. A given value
     * of ALL sets the ON/OFF state for all three LEDs.
     * This class has private attributes for the power state and color of each LED,
     * this method is used to set the color of a give LED which is then updated by
     * a general private method.
     * @param led	The LED number to be modified (defined in the com.simmetry.lcd.enums package)
     * @param color	The color to set (defined in the com.simmetry.lcd.enums package)
     */
    public synchronized void setLedColor(LedNumber led,
    		                             LedColor color) {
        
        switch (led) {
            case ONE:
                ledOneColor = color;
                if (ledOnePower == LedPower.ON) {
                    this.updateLed(LedNumber.ONE);
                }
                break;
            case TWO:
                ledTwoColor = color;
                if (ledTwoPower == LedPower.ON) {
                    this.updateLed(LedNumber.TWO);
                }
                break;
            case THREE:
                ledThreeColor = color;
                if (ledOnePower == LedPower.ON) {
                    this.updateLed(LedNumber.THREE);
                }
                break;
            case ALL:
                ledOneColor = color;
                if (ledOnePower == LedPower.ON) {
                    this.updateLed(LedNumber.ONE);
                }
                ledTwoColor = color;
                if (ledTwoPower == LedPower.ON) {
                    this.updateLed(LedNumber.TWO);
                }
                ledThreeColor = color;
                if (ledOnePower == LedPower.ON) {
                    this.updateLed(LedNumber.THREE);
                }
            	break;
        }
    }

    /**
     * Returns the color setting of the requested LED. A value of ALL will return the value of LED 1.
     * @param led	The LED to retrieve the color value for, a value of ALL will return the value of LED 1.
     * @return	The color value of the requested LED
     */
    public LedColor getLedColor(LedNumber led){
    	LedColor return_color = ledOneColor;
    	
    	switch (led) {
    	case ALL:
    	case ONE:
    		return_color = ledOneColor;
    		break;
    	case TWO:
    		return_color = ledTwoColor;
    		break;
    	case THREE:
    		return_color = ledThreeColor;
    		break;
    	}
    	
    	return return_color;
    }
    
    /**
     * Sets the power state ON/OFF for the LED indicated by the given number. A given value
     * of ALL sets the ON/OFF state for all three LEDs.
     * This class has private attributes for the power state and color of each LED,
     * this method is used to set the power state of a give LED which is then updated by a
     * general private method.
     * @param led	The LED number to be modified (defined in the com.simmetry.lcd.enums package)
     * @param power	The power state to set (defined in the com.simmetry.lcd.enums package)
     */
    public synchronized void setLedPower(LedNumber led,
    		                             LedPower power) {
        
        switch (led) {
            case ONE:
                ledOnePower = power;
                ledOneFreq = 0;
                this.updateLed(LedNumber.ONE);
                break;
            case TWO:
                ledTwoPower = power;
                ledTwoFreq = 0;
                this.updateLed(LedNumber.TWO);
                break;
            case THREE:
                ledThreePower = power;
                ledThreeFreq = 0;
                this.updateLed(LedNumber.THREE);
                break;
            case ALL:
            	ledOnePower = power;
            	ledOneFreq = 0;
                this.updateLed(LedNumber.ONE);
                ledTwoPower = power;
                ledTwoFreq = 0;
                this.updateLed(LedNumber.TWO);
                ledThreePower = power;
                ledThreeFreq = 0;
                this.updateLed(LedNumber.THREE);
                break;
        }
    }
    
    /**
     * Returns the power setting of the requested LED. A value of ALL will return the value of LED 1.
     * @param led	The LED to retrieve the power value for, a value of ALL will return the value of LED 1.
     * @return	The power value of the requested LED
     */
    public LedPower getLedPower(LedNumber led){
    	LedPower return_power = ledOnePower;
    	
    	switch (led) {
    	case ALL:
    	case ONE:
    		return_power = ledOnePower;
    		break;
    	case TWO:
    		return_power = ledTwoPower;
    		break;
    	case THREE:
    		return_power = ledThreePower;
    		break;
    	}
    	
    	return return_power;
    }
    
    /**
     * Sets the blink frequency for the LED indicated by the given number. A given value
     * of ALL sets the ON/OFF state for all three LEDs.
     * This class has private attributes for the power state and color of each LED,
     * this method is used to set the power state of a give LED which is then updated by a
     * general private method.
     * @param led		The LED number to be modified (defined in the com.simmetry.lcd.enums package)
     * @param frequency	The LED blink frequency in Hz. A value of 0 means non-blinking.
     */
    public synchronized void setLedFrequency(LedNumber led, int frequency){
    	switch (led) {
    	case ONE:
    		if( frequency > 0){
    		}
    		else {
    		}
    		ledOneFreq = frequency;
    		break;
    	case TWO:
    		ledTwoFreq = frequency;
    		break;
    	case THREE:
    		ledThreeFreq = frequency;
    		break;
    	case ALL:
    		ledOneFreq = frequency;
    		ledTwoFreq = frequency;
    		ledThreeFreq = frequency;
    		break;
    	}
    }
    
    /**
     * This class has private attributes for the power state and color of each LED,
     * this is a private method used to update a given LED to match the attribute
     * settings.
     * @param led	The LED number to be updated (defined in the com.simmetry.lcd.enums package)
     */
    private synchronized void updateLed(LedNumber led) {
        
        byte[] b = new byte[6];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[3] = MatrixOrbitalCommands.START_BYTE;
        
        LedColor color = LedColor.NULL;
        LedPower power = LedPower.NULL;
        
        switch(led) {
            case ONE:
                color = ledOneColor;
                power = ledOnePower;
                b[2] = MatrixOrbitalCommands.GP_OUTPUT_TWO;
                b[5] = MatrixOrbitalCommands.GP_OUTPUT_ONE;
                break;
            case TWO:
                color = ledTwoColor;
                power = ledTwoPower;
                b[2] = MatrixOrbitalCommands.GP_OUTPUT_FOUR;
                b[5] = MatrixOrbitalCommands.GP_OUTPUT_THREE;
                break;
            case THREE:
                color = ledThreeColor;
                power = ledThreePower;
                b[2] = MatrixOrbitalCommands.GP_OUTPUT_SIX;
                b[5] = MatrixOrbitalCommands.GP_OUTPUT_FIVE;
                break;
        }
        
        if (power == LedPower.OFF) {
            b[1] = MatrixOrbitalCommands.GP_OUTPUT_PWR_ON;
            b[4] = MatrixOrbitalCommands.GP_OUTPUT_PWR_ON;
        }
        else {
            switch (color) {
                case GREEN:
                    b[1] = MatrixOrbitalCommands.GP_OUTPUT_PWR_OFF;
                    b[4] = MatrixOrbitalCommands.GP_OUTPUT_PWR_ON;
                    break;
                case YELLOW:
                    b[1] = MatrixOrbitalCommands.GP_OUTPUT_PWR_OFF;
                    b[4] = MatrixOrbitalCommands.GP_OUTPUT_PWR_OFF;
                    break;
                case RED:
                    b[1] = MatrixOrbitalCommands.GP_OUTPUT_PWR_ON;
                    b[4] = MatrixOrbitalCommands.GP_OUTPUT_PWR_OFF;
                    break;
            }
        }
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    ///
    /// This section provides LCD Display Control functions
    ///
    /**
     * Clears the LCD screen of all current displayed output.
     */
    public synchronized void clearScreen() {
    	
    	byte[] b = new byte[2];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.CLEAR_SCREEN;
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * 
     * @param level
     */
    public synchronized void setBacklightBrightness(BrightnessLevel level) {
    	
    	byte[] b = new byte[3];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.SET_BACKLIGHT_BRIGHTNESS;
        
        switch (level) {
	        case OFF:
	            b[2] = MatrixOrbitalCommands.BACKLIGHT_OFF;
	            break;
	        case LOW:
	            b[2] = MatrixOrbitalCommands.BACKLIGHT_LOW;
	            break;
	        case MEDIUM:
	            b[2] = MatrixOrbitalCommands.BACKLIGHT_MEDIUM;
	            break;
	        case HIGH:
	            b[2] = MatrixOrbitalCommands.BACKLIGHT_HIGH;
	            break;
	    }
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * Sets the LCD brightness level, in percent from 0 to 100. This only sets the brightness
     * for the current use; to set the brightness level permanently use saveBrightness(level).
     * @param level	The level (0-100 percent) of brightness to set
     */
    public synchronized void setBrightness(int level){
    	
    	// convert the int level (percent) to a floating 0.0 - 1.0 factor
    	float flevel = (((float)level)/100.0f);
    	
    	// create the byte array, including calculating the brightness level using the factor
    	byte[] b = new byte[3];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.SET_BACKLIGHT_BRIGHTNESS;
        b[2] = (byte)((int)(flevel * 255.0f));
        
        // write the byte array to the serial interface to the LCD
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * Sets and saves the LCD brightness level, in percent from 0 to 100, both currently and as the
     * default level each time the device is powered up.
     * @param level	The level (0-100 percent) of brightness to set and save
     */
    public synchronized void saveBrightness(int level){
    	
    	// convert the int level (percent) to a floating 0.0 - 1.0 factor
    	float flevel = (((float)level)/100.0f);
    	
    	// create the byte array, including calculating the brightness level using the factor
    	byte[] b = new byte[3];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.SAVE_BACKLIGHT_BRIGHTNESS;
        b[2] = (byte)((int)(flevel * 255.0f));
        
        // write the byte array to the serial interface to the LCD
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * Sets the LCD contrast level, in percent from 0 to 100. This only sets the contrast
     * for the current use; to set the contrast level permanently use saveContrast(level).
     * @param level	The level (0-100 percent) of contrast to set
     */
    public synchronized void setContrast(int level){
    	
    	// convert the int level (percent) to a floating 0.0 - 1.0 factor
    	float flevel = (((float)level)/100.0f);
    	
    	// create the byte array, including calculating the contrast level using the factor
    	byte[] b = new byte[3];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.SET_BACKLIGHT_CONTRAST;
        b[2] = (byte)((int)(flevel * 255.0f));
        
        // write the byte array to the serial interface to the LCD
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * Sets and saves the LCD contrast level, in percent from 0 to 100, both currently and as the
     * default level each time the device is powered up.
     * @param level	The level (0-100 percent) of contrast to set and save
     */
    public synchronized void saveContrast(int level){
    	
    	// convert the int level (percent) to a floating 0.0 - 1.0 factor
    	float flevel = (((float)level)/100.0f);
    	
    	// create the byte array, including calculating the contrast level using the factor
    	byte[] b = new byte[3];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.SAVE_BACKLIGHT_CONTRAST;
        b[2] = (byte)((int)(flevel * 255.0f));
        
        // write the byte array to the serial interface to the LCD
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
 
    ///
    /// This section provides methods to Manage the LCD Cursor
    ///
    
    /**
     * Moves the Cursor to the home position (upper left hand corner).
     */
    public synchronized void moveCursorHome(){
    	byte[] b = new byte[2];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.RESET_CURSOR;
        
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * This method writes the given text to the give row in accordance with the other attributes provided.
     * For the current device and default font, writing is limited to 8 rows, numbered 1-8.
     * The string is limited to 31 characters, and any additional characters will be pruned off.
     * 
     * @param row		The row to write the text to
     * @param text		The text string to write
     * @param alignment	The text alignment (defined 
     * @param clearRow	True if the row should be cleared, false if this is to overwrite existing text
     * @return			True if the write is sucessful, false if an exception was caught
     */
    public synchronized boolean setRowText(int row,
                                           String text,
    		                               TextAlignment alignment,
    		                               boolean clearRow) {
    	
    	// Before attempting to write to the row, first check to make sure that
    	// the request is within the parameters of the LCD module.
    	if (!(row < MatrixOrbitalSpecifications.FIRST_LCD_ROW || row > MatrixOrbitalSpecifications.LAST_LCD_ROW) ||
    	    !(text.length() > MatrixOrbitalSpecifications.ROW_LENGTH)) {
    		
    		// Calculate the starting column based on the alignment and length
    		// of the text that needs to be written
    		int column = 0;
    		
    		switch(alignment) {
	    		case LEFT:
	    			column = 1;
	    			break;
	    		case RIGHT:
	    			column = (MatrixOrbitalSpecifications.ROW_LENGTH -
	    					     text.length()) + 1;
	    			break;
	    		case CENTER:
	    			column = ((MatrixOrbitalSpecifications.ROW_LENGTH -
   					             text.length()) / 2) + 1;
	    			break;
    		}
    		
    		// If the entire row needs to be cleared then we are going to write
    		// spaces to the remaining characters. Otherwise, we simply write
    		// the minimum number of characters that need to be sent based on
    		// the length of the input text. We also need to calculate where
    		// to begin writing the text if we are going to clear the row.
            byte[] b;
            int startText;
            
            if (clearRow) {
            	b = new byte[MatrixOrbitalSpecifications.ROW_LENGTH + 4];
            	
                for (int i = 0; i < b.length; i++) {
                    b[i] = (byte)' ';
                }
                
                startText = (column - 1) + 4;
            }
            else {
            	b = new byte[text.length() + 4];
            	
            	startText = 4;
            }

            b[0] = MatrixOrbitalCommands.START_BYTE;
            b[1] = MatrixOrbitalCommands.SET_CURSOR;
            
            if (clearRow) {
            	b[2] = (byte)1;
            }
            else {
                b[2] = (byte)column;            	
            }

            b[3] = (byte)row;
            
            int stopText = startText + text.length();
            if( stopText > startText + MatrixOrbitalSpecifications.ROW_LENGTH)
            	stopText = startText + MatrixOrbitalSpecifications.ROW_LENGTH;
            
            for (int i = startText; i < stopText; i++) {
                b[i] = (byte)text.charAt(i-startText);
            }
            
            for (int i = 0; i < b.length; i++) {
                inputBytes.add(new Byte(b[i]));
            }
            
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * Clears the given row text display
     * @param row	The row to clear
     */
    public synchronized void clearRowText(int row) {
    	// If we call this method with the option to clear the row enabled and
    	// send it an empty string, then the row will be cleared.
    	this.setRowText(row, "", TextAlignment.CENTER, true);
    }
    
    ///
    /// This section provide the LCD Drawing primitives functions
    ///
    
    /**
     * Sets the drawing color, either WHITE or BLACK
     * @param color	The color, as defined by the DrawingColor enum
     */
    public synchronized void setDrawingColor(DrawingColor color) {
    	
    	// create the byte array
    	byte[] b = new byte[3];
        b[0] = MatrixOrbitalCommands.START_BYTE;
        b[1] = MatrixOrbitalCommands.SET_DRAWING_COLOR;
        if( color == DrawingColor.WHITE ){
        	b[2] = (byte)0x0; // zero is white
        }
        else{
        	b[2] = (byte)0xFE; // any non-zero number is black
        }
        
        // write the byte array to the serial interface to the LCD
        for (int i = 0; i < b.length; i++) {
            inputBytes.add(new Byte(b[i]));
        }
    }
    
    /**
     * Draw a pixel at the given location, using the current drawing color (set using setDrawingColor).
     * The X and Y locations provided must be within the limits of the lcd. 
     * @param x_loc	X location of the pixel, measured left (0) to right (191)
     * @param y_loc	Y location of the pixel, measured from bottom (0) to top (63)
     */
    public synchronized void drawPixel(int x_loc, int y_loc){
    	
    	if( (x_loc >= 0 && x_loc < MatrixOrbitalSpecifications.LCD_WIDTH) && 
    		(y_loc >= 0 && y_loc < MatrixOrbitalSpecifications.LCD_HEIGHT) ){
    		
    		// create the byte array
    		byte[] b = new byte[4];
    		b[0] = MatrixOrbitalCommands.START_BYTE;
    		b[1] = MatrixOrbitalCommands.DRAW_PIXEL;
    		b[2] = (byte)x_loc;
    		b[3] = (byte)y_loc;

    		// write the byte array to the serial interface to the LCD
    		for (int i = 0; i < b.length; i++) {
    			inputBytes.add(new Byte(b[i]));
    		}
    	}
    }
    
    /**
     * Draw a line from location A to location B, using the current drawing color
     * (set using setDrawingColor). The X and Y locations provided must be within the 
     * limits of the LCD. If either value falls outside of the limits the line will not
     * be drawn. note that the line may interpolate differently right to left, or left to right.
     * This means that a line drawn in white from right to left may not fully erase the same line
     * drawn in black from left to right.
     * @param x_loc	X location of the pixel, measured left (0) to right (191)
     * @param y_loc	Y location of the pixel, measured from bottom (0) to top (63)
     */
    public synchronized void drawLine(int x_loc_a, int y_loc_a, int x_loc_b, int y_loc_b){
    	
    	if( (x_loc_a >= 0 && x_loc_a < MatrixOrbitalSpecifications.LCD_WIDTH) && 
    		(y_loc_a >= 0 && y_loc_a < MatrixOrbitalSpecifications.LCD_HEIGHT) &&
    		(x_loc_b >= 0 && x_loc_b < MatrixOrbitalSpecifications.LCD_WIDTH) && 
    		(y_loc_b >= 0 && y_loc_b < MatrixOrbitalSpecifications.LCD_HEIGHT) ){
    		
    		// create the byte array
    		byte[] b = new byte[6];
    		b[0] = MatrixOrbitalCommands.START_BYTE;
    		b[1] = MatrixOrbitalCommands.DRAW_LINE;
    		b[2] = (byte)x_loc_a;
    		b[3] = (byte)y_loc_a;
    		b[4] = (byte)x_loc_b;
    		b[5] = (byte)y_loc_b;

    		// write the byte array to the serial interface to the LCD
    		for (int i = 0; i < b.length; i++) {
    			inputBytes.add(new Byte(b[i]));
    		}
    	}
    }
    
    public synchronized void drawStylizedLine(DrawingStyle style, Point lower_left, Point upper_right){
    	
    	if(style == DrawingStyle.SOLID) {
    		this.drawLine(lower_left.x_loc, lower_left.y_loc, upper_right.x_loc, upper_right.y_loc);
    	}
    	else if(style == DrawingStyle.DOTTED) {
 
    	}
    	else if(style == DrawingStyle.DASHED) {
    		
    	}
    	else if(style == DrawingStyle.DASHED_LONG) {
    		
    	}
    	
    }
    
    /**
     * Draw a rectangle from corner A to corner B, using the provided drawing color
     * (set using setDrawingColor). The X and Y locations provided must be within the 
     * limits of the LCD. If either value falls outside of the limits the line will not
     * be drawn.
     * @param x_loc	X location of the pixel, measured left (0) to right (191)
     * @param y_loc	Y location of the pixel, measured from bottom (0) to top (63)
     */
    public synchronized void drawRectangle(DrawingColor color, int x_loc_a, int y_loc_a, int x_loc_b, int y_loc_b){
    	
    	if( (x_loc_a >= 0 && x_loc_a < MatrixOrbitalSpecifications.LCD_WIDTH) && 
    		(y_loc_a >= 0 && y_loc_a < MatrixOrbitalSpecifications.LCD_HEIGHT) &&
    		(x_loc_b >= 0 && x_loc_b < MatrixOrbitalSpecifications.LCD_WIDTH) && 
    		(y_loc_b >= 0 && y_loc_b < MatrixOrbitalSpecifications.LCD_HEIGHT) ){
    		
    		// create the byte array
    		byte[] b = new byte[7];
    		b[0] = MatrixOrbitalCommands.START_BYTE;
    		b[1] = MatrixOrbitalCommands.DRAW_RECTANGLE;
            if( color == DrawingColor.WHITE ){
            	b[2] = (byte)0x0; // zero is white
            }
            else{
            	b[2] = (byte)0xFE; // any non-zero number is black
            }
    		b[3] = (byte)x_loc_a;
    		b[4] = (byte)y_loc_a;
    		b[5] = (byte)x_loc_b;
    		b[6] = (byte)y_loc_b;

    		// write the byte array to the serial interface to the LCD
    		for (int i = 0; i < b.length; i++) {
    			inputBytes.add(new Byte(b[i]));
    		}
    	}
    }
    
    /**
     * Draw a solid (filled) rectangle from corner A to corner B, using the provided drawing color
     * (set using setDrawingColor). The X and Y locations provided must be within the 
     * limits of the LCD. If either value falls outside of the limits the line will not
     * be drawn.
     * @param x_loc	X location of the pixel, measured left (0) to right (191)
     * @param y_loc	Y location of the pixel, measured from bottom (0) to top (63)
     */
    public synchronized void drawSolidRectangle(DrawingColor color, int x_loc_a, int y_loc_a, int x_loc_b, int y_loc_b){
    	
    	if( (x_loc_a >= 0 && x_loc_a < MatrixOrbitalSpecifications.LCD_WIDTH) && 
    		(y_loc_a >= 0 && y_loc_a < MatrixOrbitalSpecifications.LCD_HEIGHT) &&
    		(x_loc_b >= 0 && x_loc_b < MatrixOrbitalSpecifications.LCD_WIDTH) && 
    		(y_loc_b >= 0 && y_loc_b < MatrixOrbitalSpecifications.LCD_HEIGHT) ){
    		
    		// create the byte array
    		byte[] b = new byte[7];
    		b[0] = MatrixOrbitalCommands.START_BYTE;
    		b[1] = MatrixOrbitalCommands.DRAW_SOLID_RECTANGLE;
            if( color == DrawingColor.WHITE ){
            	b[2] = (byte)0x0; // zero is white
            }
            else{
            	b[2] = (byte)0xFE; // any non-zero number is black
            }
    		b[3] = (byte)x_loc_a;
    		b[4] = (byte)y_loc_a;
    		b[5] = (byte)x_loc_b;
    		b[6] = (byte)y_loc_b;

    		// write the byte array to the serial interface to the LCD
    		for (int i = 0; i < b.length; i++) {
    			inputBytes.add(new Byte(b[i]));
    		}
    	}
    }
    
    ///
    /// This section provides the LCD serial communications main loop
    ///

    /**
     * This loop continuously monitors input and output of the LCD device. This is
     * the main loop for the communicating with the LCD, and it will run continuously
     * as long as communications with the LCD are established.
     */
    public void run() {
    	int led_one_counter = 0;
    	int led_two_counter = 0;
    	int led_three_counter = 0;
    	int sleep_time_millis = 4;
    	
        while (true) {
        	
			try {
				// sleep the thread for 'sleep_time_millis' milliseconds. so we will be checking for read/writes
				// 1000/'sleep_time_millis' times a second.
				Thread.sleep(sleep_time_millis);
			}
			catch (InterruptedException ieError) {
				logger.error(ieError.toString(), ieError);
			}
			
    		// Ensure that comms line is connected. If we are not connected,
    		// there is no point in servicing the input/output queues.
    		if (connected == true) {
    			
    			try {
    				// if the blink frequency is non-zero then test for blink state changes
    				if( this.ledOneFreq > 0 ){
    					if( led_one_counter > this.ledOneFreq ){
    						// toggle the power state
    						if(ledOnePower == LedPower.ON){
    							ledOnePower = LedPower.OFF;
    						}
    						else{
    							ledOnePower = LedPower.ON;
    						}
    						updateLed(LedNumber.ONE);
    						// reset the counter
    						led_one_counter = 0;
    					}
    					else{
    						// increment same amount as the sleep counter above
    						led_one_counter += sleep_time_millis; 
    					}
    				}
    				if( this.ledTwoFreq > 0 ){
    					if( led_two_counter > this.ledTwoFreq ){
    						// toggle the power state
    						if(ledTwoPower == LedPower.ON){
    							ledTwoPower = LedPower.OFF;
    						}
    						else{
    							ledTwoPower = LedPower.ON;
    						}
    						updateLed(LedNumber.TWO);
    						// reset the counter
    						led_two_counter = 0;
    					}
    					else{
    						// increment same amount as the sleep counter above
    						led_two_counter += sleep_time_millis; 
    					}
    				}
    				if( this.ledThreeFreq > 0 ){
    					if( led_three_counter > this.ledThreeFreq ){
    						// toggle the power state
    						if(ledThreePower == LedPower.ON){
    							ledThreePower = LedPower.OFF;
    						}
    						else{
    							ledThreePower = LedPower.ON;
    						}
    						updateLed(LedNumber.THREE);
    						// reset the counter
    						led_three_counter = 0;
    					}
    					else{
    						// increment same amount as the sleep counter above
    						led_three_counter += sleep_time_millis; 
    					}
    				}
    			}
    			catch (Exception ioeError) {
    				logger.error(ioeError.toString(), ioeError);
    				continue;
    			}

    			// Check if there is any data from the LCD waiting to be read. If
    			// there is data, then read it and trigger any events.
    			try {
    				int readBytes = apiReader.available();
    				byte b;
    				for (int i = 0; i < readBytes; i++) {
    					b = (byte)apiReader.read();
    					outputBytes.add(new Byte(b));
    				}
    				if (outputBytes.size() > 0){
    					executeEventHandlers();
    				}
    			}
    			catch (IOException ioeError) {
    				logger.error(ioeError.toString(), ioeError);
    				continue;
    			}


    			// Check if any commands are waiting to be written to the LCD. If
    			// there are commands to be written, then send them.
    			try {
    				Byte b;
    				// if flow control is ok and the buffer is not empty, write up to 8 bytes to the LCD
    				if(flowing==true && !inputBytes.isEmpty()){
    					int size = inputBytes.size();
    					int max = (size<8?size:8);
    					int counter = 0;
    					while(counter < max){
    						b = inputBytes.poll();
    						apiWriter.write(b.byteValue());
    						counter++;
    					}
    					apiWriter.flush();
    				}
    			}
    			catch (IOException ioeError) {
    				logger.error(ioeError.toString(), ioeError);
    				this.IPdisconnect();
    				continue;
    			}

    		} // end if(connected==true)
    		
        } // end while(true)
        
    } // end run()

    
    ///
    /// This section provides the LCD push button event handler functions
    ///
    
    /**
     * Registers a LCD push button listener.
     * @param listener New listener to register
     */
    public synchronized void addLcdButtonPressEventListener(
    		        LcdButtonPressEventListener listener) {
    	
    	eventListenerVector.add(listener);
    }
    
    /**
     * Deregisters an LCD push button listener.
     * @param listener Existing listener to be deregistered
     */
    public synchronized void removeLcdButtonPressEventListener(
    		        LcdButtonPressEventListener listener) {
    	eventListenerVector.remove(listener);
    }
    
    /**
     * This method handles the LCD push button events, and translates them from Hex to
     * the API values and creates a new event to be passed to all registered listeners.
     */
    private void executeEventHandlers() {
    	
    	while( outputBytes.size() > 0 ){
    		try{
	    		byte b = outputBytes.poll();
	    		
	    		// if the buffer is almost full
	    		if(b == (byte)0xFE){
	    			//logger.debug("LCD Buffer almost full, flow control is ON.");
	    			this.flowing = false;
	    		}
	    		// else if the buffer is almost empty
	    		else if(b == (byte)0xFF){
	    			//logger.debug("LCD Buffer almost empty, flow control is OFF.");
	    			this.flowing = true;
	    		}
	    		// else it is a keypad event
	    		else {
	    			LcdButtonPressEvent event = null;
	    			switch(b){
	    			case 65:
	    				event = new LcdButtonPressEvent(this, LcdButton.TOP_LEFT);
	    				break;
	    			case 66:
	    				event = new LcdButtonPressEvent(this, LcdButton.UP);
	    				break;
	    			case 67:
	    				event = new LcdButtonPressEvent(this, LcdButton.RIGHT);
	    				break;
	    			case 68:
	    				event = new LcdButtonPressEvent(this, LcdButton.LEFT);
	    				break;
	    			case 69:
	    				event = new LcdButtonPressEvent(this, LcdButton.CENTER);
	    				break;
	    			case 71:
	    				event = new LcdButtonPressEvent(this, LcdButton.BOTTOM_LEFT);
	    				break;
	    			case 72:
	    				event = new LcdButtonPressEvent(this, LcdButton.DOWN);
	    				break;
	    			}

	    			if( event != null ){
	    				for (int i = 0; i < eventListenerVector.size(); i++) {
	    					try {
		    					((LcdButtonPressEventListener)eventListenerVector.elementAt(i)).
		    						lcdButtonPressAction(event);
	    					}
	    					catch (Exception e) {
	    		    			logger.error("Caught exception while executing button press event.");
	    		    			logger.error(e.toString(), e);
	    					}
	    				}
	    			}
	    		}
	    		
    		}
    		catch(NoSuchElementException e){
    			logger.error(e.toString(), e);
    		}
    	}
    }
    
    ///
    /// This section provides the LCD development/test functions, not used by production STTIC
    ///
    
    /**
     * This is a development/test method used to connect to the LCD via an
     * ethernet to serial converter. Not currently used in the production STTIC.
     * @param i	IP address
     * @param p	Port number
     * @return	true if connection was sucessful, false if connection attempt encountered an exception
     */
    public boolean IPconnect(String i, int p) {

        this.ipAddress = i;
        this.port = p;
        
        // Try to establish communication with the device using the parameters
        // passed into the method
        try {
            apiSocket = new Socket(ipAddress,port);
            apiReader = apiSocket.getInputStream();
            apiWriter = apiSocket.getOutputStream();
            connected = true;
        }
        catch (IOException ioeError) {
            return false;
        }
        serviceThread.start();
        return true;
    }
    
    /**
     * This is a development/test method used when connecting to the card via
     * an ethernet to serial converter. Not currently used in the production STTIC.
     * @return	true if disconnection was clean, false if disconnection attempt encountered an exception
     */
    public boolean IPdisconnect() {
        
        // Close all stream communication
        try {          
            apiReader.close();
            apiWriter.close();
            apiSocket.close();
            connected = false;
        }
        catch (IOException ioeError) {
            return false;
        }
        
        return true;
    }
    
}
