package com.rtrk.xmlgenerator;

import com.rtrk.atcommand.xmlgenerator.ATCommandXMLGenerator;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) {

		String inputFilePath = args[0];
		String outputFilePath = args[1];
		
		ATCommandXMLGenerator.generateXML(inputFilePath, outputFilePath);

	}

}
