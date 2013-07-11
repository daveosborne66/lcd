// Copyright 2010 - Simmetry Solutions
// Author - Ryan D Murphy
package com.simmetry.lcd.unittest;

import com.simmetry.lcd.moapi.DrawingColor;
import com.simmetry.lcd.moapi.LedColor;
import com.simmetry.lcd.moapi.LedNumber;
import com.simmetry.lcd.moapi.LedPower;
import com.simmetry.lcd.moapi.MatrixOrbitalApi;
import com.simmetry.lcd.moapi.MatrixOrbitalCommands;
import com.simmetry.lcd.moapi.TextAlignment;

public class LcdInterfaceTest {
	
	/**
	 * 
	 * @param api
	 */
	private void basicBrightnessContrastExample(MatrixOrbitalApi api){
		try{
			//api.clearScreen();
			
	   		//
    		// Test brightness level
    		//
    		for (int i = 0; i <= 20; i++) {
    			api.setBrightness(5*i);
    			Thread.sleep(100);
    		}

    		//
    		// Test Contrast level
    		//
    		for (int i = 0; i <= 20; i++) {
    			api.setContrast(5*i);
    			Thread.sleep(100);
    		}
    		api.setContrast(50);
    		//Thread.sleep(100);
		}
		catch (Exception ieError) {
			System.out.println(ieError);
		}
	}
	
	
	/**
	 * 
	 * @param api
	 */
	private void basicTextExample(MatrixOrbitalApi api){
		try {
    		api.clearScreen();
    		
    		for( int i = 1; i <= 8; i++ ){
    			api.setRowText(i, "LEFT", TextAlignment.LEFT, true);
    			//Thread.sleep(250);
    		}
    		//Thread.sleep(1000);
    		
    		for( int i = 1; i <= 8; i++ ){
    			api.setRowText(i, "CENTER", TextAlignment.CENTER, false);
    			//Thread.sleep(250);
    		}
    		//Thread.sleep(1000);
    		
    		for( int i = 1; i <= 8; i++ ){
    			api.setRowText(i, "RIGHT", TextAlignment.RIGHT, false);
    			//Thread.sleep(250);
    		}
    		//Thread.sleep(1000);

    		api.clearScreen();
    		for( int i = 1; i <= 8; i++ ){
				api.setRowText(i, "Testing", TextAlignment.LEFT, false);
    			//Thread.sleep(250);
				api.setRowText(i, "Testing", TextAlignment.RIGHT, false);
    			//Thread.sleep(250);
    		}
    		//Thread.sleep(1000);

    		api.clearScreen();
    		for( int i = 1; i <= 8; i++ ){
				api.setRowText(i, "Testing", TextAlignment.CENTER, true);
    			//Thread.sleep(250);
    		}

    		//Thread.sleep(1000);
		}
		catch (Exception ieError) {
			System.out.println(ieError);
		}
	}
	
	
	/**
	 * 
	 * @param api
	 */
	private void basicGraphicExample(MatrixOrbitalApi api){
		try {
    		//
    		// Test Drawing
    		//
 
    		// vertical line test
    		api.clearScreen();
    		api.setDrawingColor(DrawingColor.BLACK);
    		for( int x = 0; x < 192; x++){
    			api.drawLine(x, 0, x, 63);
    		}
    		
    		// horizontal line test
    		api.setDrawingColor(DrawingColor.WHITE);
    		for( int y = 0; y < 63; y=y+2){
    			api.drawLine(0, y, 191, y);
    		}
    		
    		// hollow rectangle test
    		api.clearScreen();
    		for( int x = 0; x < 192; x=x+7){
    			for( int y = 0; y < 63; y=y+7){
    				api.drawRectangle(DrawingColor.BLACK, x, y, x+5, y+5);
    			}
    		}
    		
    		// solid rectangle test
    		api.clearScreen();
    		for( int x = 0; x < 192; x=x+7){
    			for( int y = 0; y < 63; y=y+7){
    				api.drawSolidRectangle(DrawingColor.BLACK, x, y, x+5, y+5);
    			}
    		}
    		
    		for( int x = 0; x < 192; x=x+7){
    			for( int y = 0; y < 63; y=y+7){
    				api.drawSolidRectangle(DrawingColor.WHITE, x, y, x+5, y+5);
    			}
    		}
		}
		catch (Exception ieError) {
			System.out.println(ieError);
		}
	}
	
	
	/**
	 * 
	 * @param api
	 */
	private void ledExample(MatrixOrbitalApi api){
		try {
    		//
    		// Lots of tests for the General Output LEDs
    		//
    		api.setLedPower(LedNumber.ONE, LedPower.OFF);
    		api.setLedPower(LedNumber.TWO, LedPower.OFF);
    		api.setLedPower(LedNumber.THREE, LedPower.OFF);
    		Thread.sleep(1000);

    		api.setLedColor(LedNumber.ONE, LedColor.GREEN);
    		api.setLedColor(LedNumber.TWO, LedColor.YELLOW);
    		api.setLedColor(LedNumber.THREE, LedColor.RED);
    		api.setLedPower(LedNumber.ONE, LedPower.ON);
    		api.setLedPower(LedNumber.TWO, LedPower.ON);
    		api.setLedPower(LedNumber.THREE, LedPower.ON);
    		Thread.sleep(1000);

    		api.setLedColor(LedNumber.ONE, LedColor.GREEN);
    		api.setLedPower(LedNumber.ONE, LedPower.ON);
    		api.setLedColor(LedNumber.TWO, LedColor.GREEN);
    		api.setLedPower(LedNumber.TWO, LedPower.ON);
    		api.setLedColor(LedNumber.THREE, LedColor.GREEN);
    		api.setLedPower(LedNumber.THREE, LedPower.ON);
    		Thread.sleep(1000);

    		api.setLedColor(LedNumber.ONE, LedColor.YELLOW);
    		api.setLedPower(LedNumber.ONE, LedPower.ON);
    		api.setLedColor(LedNumber.TWO, LedColor.YELLOW);
    		api.setLedPower(LedNumber.TWO, LedPower.ON);
    		api.setLedColor(LedNumber.THREE, LedColor.YELLOW);
    		api.setLedPower(LedNumber.THREE, LedPower.ON);
    		Thread.sleep(1000);

    		api.setLedColor(LedNumber.ONE, LedColor.RED);
    		api.setLedPower(LedNumber.ONE, LedPower.ON);
    		api.setLedColor(LedNumber.TWO, LedColor.RED);
    		api.setLedPower(LedNumber.TWO, LedPower.ON);
    		api.setLedColor(LedNumber.THREE, LedColor.RED);
    		api.setLedPower(LedNumber.THREE, LedPower.ON);
    		Thread.sleep(1000);

    		for (int i = 0; i < 5; i++) {
    			api.setLedPower(LedNumber.TWO, LedPower.OFF);
    			api.setLedPower(LedNumber.THREE, LedPower.OFF);
    			api.setLedColor(LedNumber.ONE, LedColor.GREEN);
    			api.setLedColor(LedNumber.TWO, LedColor.YELLOW);
    			api.setLedColor(LedNumber.THREE, LedColor.RED);
    			api.setLedPower(LedNumber.ONE, LedPower.ON);
    			Thread.sleep(100);
    			api.setLedPower(LedNumber.ONE, LedPower.OFF);
    			api.setLedPower(LedNumber.THREE, LedPower.OFF);
    			api.setLedColor(LedNumber.ONE, LedColor.RED);
    			api.setLedColor(LedNumber.TWO, LedColor.GREEN);
    			api.setLedColor(LedNumber.THREE, LedColor.YELLOW);
    			api.setLedPower(LedNumber.TWO, LedPower.ON);
    			Thread.sleep(100);
    			api.setLedPower(LedNumber.ONE, LedPower.OFF);
    			api.setLedPower(LedNumber.TWO, LedPower.OFF);
    			api.setLedColor(LedNumber.ONE, LedColor.YELLOW);
    			api.setLedColor(LedNumber.TWO, LedColor.RED);
    			api.setLedColor(LedNumber.THREE, LedColor.GREEN);
    			api.setLedPower(LedNumber.THREE, LedPower.ON);
    			Thread.sleep(100);
    		}

    		api.setLedPower(LedNumber.ONE, LedPower.OFF);
    		api.setLedPower(LedNumber.TWO, LedPower.OFF);
    		api.setLedPower(LedNumber.THREE, LedPower.OFF);
		}
		catch (InterruptedException ieError) {
			System.out.println(ieError);
		}
	}
	
	
	/**
	 * 
	 * @param api
	 */
	private void menuExample(MatrixOrbitalApi api){
		
	}
	
	
	/**
	 * This class draws an example LcdDialog to the given LCD. In this case the example
	 * mimics the Display Settings dialog for the STTIC
	 * 
	 * @param api	The LCD to draw to
	 */
	private void dialogExample(MatrixOrbitalApi api){

		try{
			// mockup brightness and contrast screen
			api.clearScreen();
			api.setRowText(1, "Display Settings", TextAlignment.LEFT, false);
			api.setRowText(1, "<BK>", TextAlignment.RIGHT, false);
			api.setRowText(3, " > Brightness", TextAlignment.LEFT, true);
			// outer rectangle
			int y_start = 29; 
			int x_start = 23;
			int height = 4;
			int width = 128;
			api.drawRectangle(DrawingColor.BLACK, (x_start-2), (y_start-(height/2)-2),
					(x_start+width+2), (y_start+(height/2)+2) );

			// filled blocks
			for( int x = x_start; x < (x_start+(width/2)); x=x+(width/20)){
				api.drawSolidRectangle(DrawingColor.BLACK, x, (y_start-(height/2)), (x+(width/20)), (y_start+(height/2)));
				//Thread.sleep(15);
			}

			api.setRowText(6, " > Contrast", TextAlignment.LEFT, true);
			// outer rectangle
			y_start = 53; 
			api.drawRectangle(DrawingColor.BLACK, (x_start-2), (y_start-(height/2)-2),
					(x_start+width+2), (y_start+(height/2)+2) );

			// filled blocks
			for( int x = x_start; x < (x_start+(width/2)); x=x+(width/20)){
				api.drawSolidRectangle(DrawingColor.BLACK, x, (y_start-(height/2)), (x+(width/20)), (y_start+(height/2)));
				//Thread.sleep(15);
			}

			api.setRowText(8, "<OK>", TextAlignment.RIGHT, false);	
		}
		catch (Exception ieError) {
			System.out.println(ieError);
		}
	}
	
    
    public static void main(String[] args) {
        
    	try {
    		LcdInterfaceTest tester = new LcdInterfaceTest();
    		MatrixOrbitalApi api = new MatrixOrbitalApi();

			// Change the lcd_port value to match the device path on your
			// system.
			String lcd_port = "/dev/tty.usbserial-0bFAXP31";
    		
    		api.connect(lcd_port, MatrixOrbitalCommands.BAUD_19200);
    		
    		// run through basic tests
    		tester.basicBrightnessContrastExample(api);
    		tester.basicTextExample(api);
    		tester.basicGraphicExample(api);
    		
    		// run through example tests
    		tester.ledExample(api);
    		tester.dialogExample(api);
    		tester.basicBrightnessContrastExample(api);
			
        }
        catch (Exception ieError) {
        	System.out.println(ieError);
        }
    }
}
