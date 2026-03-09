package com.example.bgl.gol.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.bgl.gol.model.Cell;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private final ObjectMapper gameStateOutputObjectMapper;
    private final CustomSingleLineSpacePrettyPrinter customPrettyPrinter;

    public JsonUtil(ObjectMapper gameStateOutputObjectMapper, CustomSingleLineSpacePrettyPrinter customPrettyPrinter) {
        this.gameStateOutputObjectMapper = gameStateOutputObjectMapper;
        this.customPrettyPrinter = customPrettyPrinter;
    }

    /**
     * Parses a JSON string representation of a 2D array of integers
     * into a primitive int[][] array.
     * @param jsonString The JSON string, e.g., "[[1,1], [2,3], [4,5]]"
     * @return An int[][] array representing the parsed 2D array,
     * @throws  JsonProcessingException if parsing fails
     */
    public int[][] parse2DIntArray(String jsonString) throws JsonProcessingException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            logger.error("Input JSON string is null or empty. Returning empty array.");
            return new int[0][0];
        }

        try {
            return gameStateOutputObjectMapper.readValue(jsonString, int[][].class);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON string into int[][]: {}", e.getMessage());
            throw e;
        }
    }

    /** Converts a 2D int array to its JSON string representation.
     * @param listOfCells The 2D int array to convert.
     * @return The JSON string representation of the array.
     * @throws JsonProcessingException if conversion fails.
     */
    public String toJsonString(List<Cell> listOfCells) throws JsonProcessingException {
        if (listOfCells == null) {
            return "[]";
        }
        try {

            return gameStateOutputObjectMapper.writer(customPrettyPrinter).writeValueAsString(listOfCells);

        } catch (JsonProcessingException e) {
            logger.error("Error converting int[][] to JSON string: {}", e.getMessage());
            throw e;
        }
    }

}