// XML Parsing Service
// Now, let’s write the service that parses the XML file,
// checks the "xxx" path first, and falls back to the "x2x" path if the "xxx" section is not found.

package com.example.xmlparser.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class XMLParserService {

    public Map<String, String> parseXML(String filePath) {
        Map<String, String> keyValueMap = new HashMap<>();
        String primaryPath = "xxx";
        String fallbackPath = "x2x";

        try {
            // Parse the XML file using the DOM parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(filePath));
            document.getDocumentElement().normalize();

            // Try to get data from the primary path ("xxx")
            NodeList nodeList = document.getElementsByTagName(primaryPath);

            // If "xxx" path is empty, fall back to "x2x" path
            if (nodeList.getLength() == 0) {
                nodeList = document.getElementsByTagName(fallbackPath);
            }

            // If fallback path is also empty, throw an exception
            if (nodeList.getLength() == 0) {
                throw new RuntimeException("Neither 'xxx' nor 'x2x' paths were found in the XML.");
            }

            // Process the selected path's entries
            Node node = nodeList.item(0); // Get the first node for the path
            NodeList entries = ((Element) node).getElementsByTagName("entry");

            // Iterate through each entry and add to the map
            for (int i = 0; i < entries.getLength(); i++) {
                Node entryNode = entries.item(i);
                if (entryNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element entryElement = (Element) entryNode;
                    String key = entryElement.getElementsByTagName("key").item(0).getTextContent();
                    String value = entryElement.getElementsByTagName("value").item(0).getTextContent();
                    keyValueMap.put(key, value);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing XML file", e);
        }

        return keyValueMap;
    }
}




package com.example.xmlparser.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class XmlParsingService {

    public Map<String, String> parseXml(String filePath) {
        Map<String, String> keyValueMap = new HashMap<>();

        try {
            // Load and parse the XML file
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Get the desired MappingConfiguration section
            NodeList mappingConfigurations = doc.getElementsByTagName("MappingConfiguration");

            for (int i = 0; i < mappingConfigurations.getLength(); i++) {
                Node node = mappingConfigurations.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    // Check if the name attribute is "MSS_EOD_Next"
                    if ("MSS_EOD_Next".equals(element.getAttribute("name")) &&
                        "MSS_EOD_Europe".equals(element.getAttribute("base"))) {

                        // Retrieve key-value pairs within this section
                        NodeList keys = element.getElementsByTagName("Key");
                        NodeList values = element.getElementsByTagName("Value");

                        for (int j = 0; j < keys.getLength(); j++) {
                            String key = keys.item(j).getTextContent();
                            String value = values.item(j).getTextContent();
                            keyValueMap.put(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return keyValueMap;
    }
}

