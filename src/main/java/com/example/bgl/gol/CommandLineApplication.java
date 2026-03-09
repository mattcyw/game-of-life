package com.example.bgl.gol;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.example.bgl.gol.engine.GameOfLifeSimulator;
import com.example.bgl.gol.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootApplication
public class CommandLineApplication {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineApplication.class);

    private final JsonUtil jsonUtil;
    private final GameOfLifeSimulator gameOfLifeSimulator;
    private final int width;
    private final int height;
    private final int generations;

    public CommandLineApplication(JsonUtil jsonUtil, GameOfLifeSimulator gameOfLifeSimulator,
            @Value("${gol.width}") int width,
            @Value("${gol.height}") int height,
            @Value("${gol.generations}") int generations) {
        this.jsonUtil = jsonUtil;
        this.gameOfLifeSimulator = gameOfLifeSimulator;
        this.width = width;
        this.height = height;
        this.generations = generations;
    }

    public static void main(String[] args) {
        SpringApplication.run(CommandLineApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            // Input validation 1: exactly one argument is expected (the initial live cells as a JSON 2D array)
            if (args.length != 1) {
                logger.error("Input Validation Error: args.length != 1");
                System.err.println("Please provide the initial live cells of the Game of Life as the ONLY argument.");
                System.err.println("Sample input: [[5, 5], [6, 5], [7, 5], [5, 6], [6, 6], [7, 6]]");
                System.exit(1);
            }

            // Parse the input JSON string into a 2D int array
            try {
                int[][] initialLiveCells = jsonUtil.parse2DIntArray(args[0]);

                // Input validation 2: must contain at least one cell
                if (initialLiveCells == null || initialLiveCells.length == 0) {
                    logger.error("No initial live cells provided: args[0] = {}", args[0]);
                    System.err.println("At least one initial live cell must be provided. Exiting the simulation...");
                    System.exit(1);
                }

                // Input validation 3: each cell must be represented by exactly two integers [row, col]
                Arrays.stream(initialLiveCells).forEach(coords -> {
                    if (coords == null || coords.length != 2) {
                        logger.error("Invalid cell coordinates: {}. Each cell must be represented by exactly two integers [row, col]. Exiting.",
                            Arrays.toString(coords));
                        System.exit(1);
                    }
                });

                logger.debug("Game of Life Simulation Output (Next {} States).", this.generations);

                try {
                    gameOfLifeSimulator.simulate(this.height, this.width, initialLiveCells, this.generations, System.out);

                } catch (IllegalArgumentException e) {
                    logger.error("Configuration error: {} Exiting.", e.getMessage());
                    System.exit(1);
                }

            } catch (JsonProcessingException e) {
                logger.error("Invalid input format: {}.", e.getMessage());
                logger.debug("Error details: ", e);
                System.err.println("Invalid input format. Please provide a valid JSON 2D array of integers.");
                System.exit(1);
            }



            logger.debug("Simulation completed.");
        };
    }
}