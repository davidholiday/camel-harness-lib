package com.davidholiday.camel.harness.helpers;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.IOException;

import java.util.Map;

import com.google.gson.Gson;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * functions to make it easy to load file contents into useful constructs when running tests
 *
 * *NOTE* when it comes time to include xml file parsing use the jsoup library defined in the pom
 */
public class MockFileHelpers {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockFileHelpers.class);


    /**
     * @param filename
     * @param template
     * @return
     */
    public static String getFileContentsAsString(String filename, ProducerTemplate template) {
        CamelContext camelContext = template.getCamelContext();
        String fileContentsAsString = getFileContentsAsString(filename, camelContext);
        return fileContentsAsString;
    }


    /**
     * @param filename
     * @param camelContext
     * @return
     */
    public static String getFileContentsAsString(String filename, CamelContext camelContext) {
        String filePathString = camelContext.getClassResolver()
                .loadResourceAsURL(filename)
                .getPath();

        Path filePath = Paths.get(filePathString);
        String fileContentsAsString = null;

        try {
            byte[] getOrderDetailsResponseMockBytes = Files.readAllBytes(filePath);
            fileContentsAsString = new String(getOrderDetailsResponseMockBytes);
        } catch (IOException e) {
            LOGGER.error("test failure due to exception loading file", e);
            assert false;
        }

        LOGGER.debug("fileContentsAsString is: {}", fileContentsAsString);
        return fileContentsAsString;
    }


    /**
     * @param filename
     * @param template
     * @return
     */
    public static Map<String, Object> getJsonFileContentsAsMap(String filename, ProducerTemplate template) {
        String jsonFileContentsAsString = getFileContentsAsString(filename, template);
        Map<String, Object> jsonFileContentsAsMap = new Gson().fromJson(jsonFileContentsAsString, Map.class);
        return jsonFileContentsAsMap;
    }


    /**
     * @param filename
     * @param camelContext
     * @return
     */
    public static Map<String, Object> getJsonFileContentsAsMap(String filename, CamelContext camelContext) {
        String jsonFileContentsAsString = getFileContentsAsString(filename, camelContext);
        Map<String, Object> jsonFileContentsAsMap = new Gson().fromJson(jsonFileContentsAsString, Map.class);
        return jsonFileContentsAsMap;
    }

}
