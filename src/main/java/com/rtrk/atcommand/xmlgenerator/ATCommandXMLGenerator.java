package com.rtrk.atcommand.xmlgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * Utility class for generating .xml file from .proto file. The class contains
 * static method generateXML for creating .xml file which contains description
 * of AT Command defined in .proto file.
 * 
 * @author djekanovic
 * 
 */
public class ATCommandXMLGenerator {

	public static String inputFliePath;
	public static String outputFilePath;

	/**
	 * 
	 * Generating .xml file from .proto file
	 * 
	 * @param inputFilePath
	 *            Path of .proto file.
	 * 
	 * @param outputFilePath
	 *            Path of .xml file.
	 * 
	 */
	public static void generateXML(String inputFilePath, String outputFilePath) {
		try {

			ATCommandXMLGenerator.inputFliePath = inputFilePath;
			ATCommandXMLGenerator.outputFilePath = outputFilePath;

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			// cmds commnet
			Comment cmdsComment = document.createComment("// TODO Auto-generated XML - Set regular expression");
			document.appendChild(cmdsComment);

			// cmds element
			Element cmds = document.createElement("cmds");
			cmds.setAttribute("regex", "");
			document.appendChild(cmds);

			// cmd element
			for (String commandType : getCommandTypes()) {
				// cmd comment
				Comment cmdComment = document
						.createComment("// TODO Auto-generated XML - Set command delimiter and prefix");
				cmds.appendChild(cmdComment);

				Element cmd = document.createElement("cmd");
				cmds.appendChild(cmd);
				cmd.setAttribute("name", commandType);
				cmd.setAttribute("prefix", "");
				cmd.setAttribute("delimiter", "");

				// first type element
				Element firstOrder = null;

				// others type element
				ArrayList<String> messageTypes = getMessageTypesByCommandType(commandType);
				for (int i = 0; i < messageTypes.size(); i++) {
					// type comment
					Comment typeComment = document
							.createComment("// TODO Auto-generated XML - Set command type prefix");
					cmd.appendChild(typeComment);

					Element type = document.createElement("type");
					cmd.appendChild(type);
					type.setAttribute("name", messageTypes.get(i));
					type.setAttribute("prefix", "");

					// class comment
					Comment classComment = document.createComment(
							"//TODO Auto-generated XML - Set class name [TEST, READ, WRITE, EXECUTION] Set class prefix, parser class and optional if exists");
					type.appendChild(classComment);

					// empty class element
					Element emptyClass = document.createElement("class");
					type.appendChild(emptyClass);
					emptyClass.setAttribute("name", "");
					emptyClass.setAttribute("prefix", "");
					emptyClass.setAttribute("parser", "");
					emptyClass.setAttribute("optional", "true");

					// class element with order
					Element fullClass = document.createElement("class");
					fullClass.setAttribute("name", "");
					fullClass.setAttribute("prefix", "");
					type.appendChild(fullClass);

					// order element
					Element order = document.createElement("order");
					fullClass.appendChild(order);

					// set first order
					if (i == 0) {
						firstOrder = order;
					} else {
						Comment orderComment = document
								.createComment("// TODO Auto-generated XML - Copy command params here");
						order.appendChild(orderComment);
					}
				}

				// param element for first order
				Comment paramComment = document.createComment(
						"// TODO Auto-generated XML - Set param optionality and parser class  Remove unused params and attributes");
				firstOrder.appendChild(paramComment);

				ArrayList<String> params = getParametersByCommandType(commandType);
				for (int i = 0; i < params.size(); i++) {
					Element param = document.createElement("param");
					param.setAttribute("name", params.get(i));
					param.setAttribute("optional", "true");
					param.setAttribute("parser", "");
					param.setAttribute("environmet", "true");
					firstOrder.appendChild(param);

					// set first and second param
					if (i == 0) {

						Element minElement = document.createElement("min");
						Element maxElement = document.createElement("max");
						param.appendChild(minElement);
						param.appendChild(maxElement);

						// set min and max comment
						Comment minComment = document
								.createComment("// TODO Auto-generated XML - Set min value if exists");
						Comment maxComment = document
								.createComment("// TODO Auto-generated XML - Set max value if exists");
						minElement.appendChild(minComment);
						maxElement.appendChild(maxComment);
					}
					if (i == 1) {

						Element trueElement = document.createElement("true");
						Element falseElement = document.createElement("false");
						param.appendChild(trueElement);
						param.appendChild(falseElement);

						// set true and false comment
						Comment trueComment = document
								.createComment("// TODO Auto-generated XML - Set true value [default = 1]");
						Comment falseComment = document
								.createComment("// TODO Auto-generated XML - Set false value [default = 0]");
						trueElement.appendChild(trueComment);
						falseElement.appendChild(falseComment);
					}
				}

			}
			// write content into .xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(outputFilePath));
			transformer.transform(source, result);
			System.out.println("XML file successfully generated.");
		} catch (ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Returns parameters by command type defined in .proto file
	 * 
	 * @param commandType
	 *            command type
	 * 
	 * @return parameter names as array
	 * 
	 */
	private static ArrayList<String> getParametersByCommandType(String commandType) {
		ArrayList<String> parameters = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File(inputFliePath)), Charset.forName("UTF-8")));

			String line;

			// skip until message ???Command {
			while (!reader.readLine().equalsIgnoreCase("message " + commandType + " {"))
				;

			// skip messageType and action
			reader.readLine();
			reader.readLine();

			// read until }
			while (!(line = reader.readLine()).startsWith("}")) {
				if (line.contains("optional")) {
					String parameter = line.trim().split(" ")[2];
					parameters.add(parameter);
				}
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parameters;
	}

	/**
	 * 
	 * Returs message types by command type defined in .proto file
	 * 
	 * @param commandType
	 *            command type
	 * 
	 * @return message types as array
	 * 
	 */
	private static ArrayList<String> getMessageTypesByCommandType(String commandType) {
		ArrayList<String> messageTypes = new ArrayList<String>();
		commandType = commandType.substring(0, commandType.length() - 7);
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File(inputFliePath)), Charset.forName("UTF-8")));

			String line;

			// skip until enum ???MessageType {
			while (!reader.readLine().equalsIgnoreCase("enum " + commandType + "MessageType {"))
				;

			// read until }
			while (!(line = reader.readLine()).startsWith("}")) {
				String messageType = line.trim().split(" ")[0];
				messageTypes.add(messageType);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return messageTypes;
	}

	/**
	 * 
	 * Returns command types defined in .proto file.
	 * 
	 * @return command types as array
	 * 
	 */
	private static ArrayList<String> getCommandTypes() {
		ArrayList<String> commandTypes = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File(inputFliePath)), Charset.forName("UTF-8")));

			String line;

			// skip until message Command {
			while (!reader.readLine().equalsIgnoreCase("message Command {"))
				;

			// skip commandType
			reader.readLine();

			// read until }
			while (!(line = reader.readLine()).equalsIgnoreCase("}")) {
				String commandType = line.trim().split(" ")[2];
				commandTypes.add(commandType);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return commandTypes;
	}

}
