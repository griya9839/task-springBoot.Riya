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
