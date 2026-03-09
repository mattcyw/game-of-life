package com.example.bgl.gol.engine;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.bgl.gol.model.Cell;
import com.example.bgl.gol.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class GameOfLifeSimulator {

    private static final Logger logger = LoggerFactory.getLogger(GameOfLifeSimulator.class);

    private final JsonUtil jsonUtil;
    private final CellSortingStrategy cellSortingStrategy;

    public GameOfLifeSimulator(JsonUtil jsonUtil, CellSortingStrategy cellSortingStrategy) {
        this.jsonUtil = jsonUtil;
        this.cellSortingStrategy = cellSortingStrategy;
        logger.debug("cellSortingStrategy: {}", cellSortingStrategy.getClass().getSimpleName());
    }

    /**
     * Simulates the Game of Life and emits each generation as a GameStateOutput.
     * The simulation stops when either the specified number of generations is reached,
     * all cells are dead, or a stable state is detected (consecutive identical states).
     *
     * <h3>Validation design</h3>
     * <ul>
     *   <li><b>rows, cols, numGenerations</b> — must be positive integers (≥ 1). These are
     *       configuration values; a violation is a programming/configuration error and causes
     *       an {@link IllegalArgumentException} to be thrown immediately.</li>
     *   <li><b>initialLiveCells</b> — each cell must be within the grid bounds. A violation is
     *       a user-input error; an informative message is written to {@code outputStream} and
     *       the method returns without running the simulation.</li>
     * </ul>
     *
     * @param rows              Number of rows in the grid (must be ≥ 1)
     * @param cols              Number of columns in the grid (must be ≥ 1)
     * @param initialLiveCells  Initial live cells represented as an array of [row, col] pairs
     * @param numGenerations    Number of generations to simulate (must be ≥ 1)
     * @param outputStream      OutputStream to emit results; caller is responsible for closing
     * @throws JsonProcessingException if JSON serialization fails during output
     * @throws IllegalArgumentException if {@code rows}, {@code cols}, or {@code numGenerations} < 1
     */
    public void simulate(int rows, int cols, int[][] initialLiveCells, int numGenerations, OutputStream outputStream) 
            throws JsonProcessingException, IllegalArgumentException {

        // --- Configuration validation: programming/configuration error → throw exception ---
        if (rows < 1 || cols < 1 || numGenerations < 1) {
            throw new IllegalArgumentException(
                "rows, cols and numGenerations must all be positive integers (≥ 1). " +
                "Got: rows=" + rows + ", cols=" + cols + ", numGenerations=" + numGenerations);
        }

        // --- GOL input validation: user-input error → write to stream and return ---
        List<String> outOfBounds = new ArrayList<>();
        for (int[] cell : initialLiveCells) {
            if (cell[0] < 0 || cell[0] >= rows || cell[1] < 0 || cell[1] >= cols) {
                outOfBounds.add(Arrays.toString(cell));
            }
        }
        if (!outOfBounds.isEmpty()) {
            String msg = "Input rejected: the following cells are out of bounds for a " +
                rows + "×" + cols + " grid" +
                " (valid row [0, " + rows + "), col [0, " + cols + ")):\n" +
                "  " + String.join(", ", outOfBounds) + "\n";
            try {
                outputStream.write(msg.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                logger.error("Error writing validation message to output stream: {}", e.getMessage());
            }
            return;
        }

        // Create the Game
        GameGridStateMachine gameGrid = new GameGridStateMachine(rows, cols, initialLiveCells);

        // Preserve the original input order so some more complicated sorting strategies like SlotPreservingSortingStrategy
        // can use it as the reference slot list when computing the very first generation.
        List<Cell> previousStateSorted = Arrays.stream(initialLiveCells)
            .map(cell -> new Cell(cell[0], cell[1]))
            .collect(Collectors.toList());
        List<Cell> currentStateSorted = previousStateSorted;

        String previousStateOutput = "";
        String currentStateOutput = jsonUtil.toJsonString(currentStateSorted);

        boolean isStableCondition = false;

        // Core simulation logic
        for (int generation = 1; generation <= numGenerations; generation++) {

            // Check for stable conditions for performance optimization
            if (isStableCondition) {

                // Already in stable condition, just continue emitting the same state
                // Do nothing else

            } else if (previousStateOutput.equals(currentStateOutput)) {

                // Just reached the stable condition
                logger.debug("Generation {}: Consecutive states are identical. Stopping simulation logic.", generation);
                isStableCondition = true;

            } else if (currentStateSorted.isEmpty()){

                // All cells are dead: stop simulation immediately
                logger.debug("All cells are dead at generation {}. Stopping simulation.", generation);
                break;

            } else {

                // Normal case: calculate next generation
                previousStateSorted = currentStateSorted;
                previousStateOutput = currentStateOutput;
                gameGrid.nextGeneration();
                currentStateSorted = this.cellSortingStrategy.sort(previousStateSorted, gameGrid.getCurrentLiveState());
                currentStateOutput = jsonUtil.toJsonString(currentStateSorted);
            }

            // Emit current state to output stream
            try {
                outputStream.write((generation + ": " + currentStateOutput + "\n").getBytes());
                outputStream.flush();

            } catch (IOException e) {
                logger.error("Error writing output for generation {}: {}", generation, e.getMessage());
                break; // Stop simulation on output error
            }
        }
    }
}
