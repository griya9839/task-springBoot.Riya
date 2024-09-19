REST Controller to Expose the Service
This controller will expose a REST API to trigger the XML parsing.

package com.example.xmlparser.controller;

import com.example.xmlparser.service.XMLParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/xml")
public class XMLParserController {

    private final XMLParserService xmlParserService;

    @Autowired
    public XMLParserController(XMLParserService xmlParserService) {
        this.xmlParserService = xmlParserService;
    }

    @GetMapping("/parse")
    public ResponseEntity<Map<String, String>> parseXML() {
        // File path to the XML file (adjust as needed)
        String filePath = "src/main/resources/sample.xml";

        // Call the service to parse the XML
        Map<String, String> keyValueMap = xmlParserService.parseXML(filePath);

        // Return the result as a JSON response
        return ResponseEntity.ok(keyValueMap);
    }
}
