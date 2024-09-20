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




package com.example.xmlparser.controller;

import com.example.xmlparser.service.XmlParsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class XmlParserController {

    @Autowired
    private XmlParsingService xmlParsingService;

    @GetMapping("/parse-xml")
    public Map<String, String> parseXml(@RequestParam String filePath) {
        return xmlParsingService.parseXml(filePath);
    }
}




package com.example.xmlparser;

import com.example.xmlparser.service.XmlParsingService;
import java.util.Map;

public class XmlParserApplication {

    public static void main(String[] args) {
        XmlParsingService xmlParsingService = new XmlParsingService();
        String xmlFilePath = "path_to_your_xml_file.xml"; // Replace with your actual XML file path

        Map<String, String> parsedData = xmlParsingService.parseXml(xmlFilePath);

        // Print the parsed key-value pairs
        parsedData.forEach((key, value) -> {
            System.out.println("Key: " + key + ", Value: " + value);
        });
    }
}
