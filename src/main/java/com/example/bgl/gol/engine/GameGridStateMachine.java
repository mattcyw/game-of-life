package com.example.bgl.gol.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.bgl.gol.model.Cell;

/**
 * Manages the 2D grid state for Conway's Game of Life using a pure sparse representation (Set<Cell>).
 * - Processes only active cells and their neighbors.
 * - Uses toroidal (wrapping) boundaries: edges connect to the opposite edge.
 * - Input coordinates that are out of bounds are wrapped toroidally rather than rejected,
 *   so any integer coordinates are accepted.
 */
public class GameGridStateMachine {

    private final int rows;
    private final int cols;
    private Set<Cell> liveCells;

    /**
     * Constructs a new grid state machine.
     * Any cell coordinate outside {@code [0, rows) x [0, cols)} is silently wrapped
     * to its toroidal equivalent, e.g. row {@code -1} on a 10-row grid becomes row {@code 9}.
     */
    public GameGridStateMachine(int rows, int cols, int[][] initialLiveCells) {
        this.rows = rows;
        this.cols = cols;

        // Wrap any out-of-bounds coordinates toroidally and deduplicate via Set.
        this.liveCells = java.util.Arrays.stream(initialLiveCells)
            .map(cell -> new Cell(
                ((cell[0] % rows) + rows) % rows,
                ((cell[1] % cols) + cols) % cols
            ))
            .collect(Collectors.toSet());
    }

    /**
     * @return A sorted list of live cells in the current generation.
     */
    public Set<Cell> getCurrentLiveState() {
        return this.liveCells;
    }

    /**
     * Calculates the next generation of the grid using toroidal (wrapping) boundaries.
     * Only processes cells in the active region (live cells + their toroidal neighbours).
     * Updates the internal {@code liveCells} set.
     * @return A sorted list of live cells in the new generation.
     */
    public List<Cell> nextGeneration() {

        Set<Cell> newLiveCells = new HashSet<>();
        Set<Cell> cellsToConsider = new HashSet<>();

        // 1. Identify the "active region": all current live cells and their 8 neighbors
        for (Cell cell : liveCells) {
            cellsToConsider.add(cell); // Add live cell itself
            // Add all 8 neighbors using toroidal (wrapping) coordinates
            for (int dRow = -1; dRow <= 1; dRow++) {
                for (int dCol = -1; dCol <= 1; dCol++) {
                    if (dRow == 0 && dCol == 0) continue; // Skip the cell itself

                    int nRow = ((cell.row() + dRow) % rows + rows) % rows;
                    int nCol = ((cell.col() + dCol) % cols + cols) % cols;
                    cellsToConsider.add(new Cell(nRow, nCol));
                }
            }
        }

        // 2. Apply Game of Life rules ONLY to the cells within the active region
        for (Cell cell : cellsToConsider) {
            int liveNeighbors = countLiveNeighbors(cell);
            boolean isLive = liveCells.contains(cell); // Check current state from the set

            if (isLive) {
                // Live cell rules
                if (liveNeighbors == 2 || liveNeighbors == 3) {
                    newLiveCells.add(cell);
                }
            } else {
                // Dead cell rules
                if (liveNeighbors == 3) {
                    newLiveCells.add(cell);
                }
            }
        }

        this.liveCells = newLiveCells; // Update for the next generation
        return newLiveCells.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Counts the number of live neighbors for a given cell.
     * Relies on the 'liveCells' Set for checking neighbor states.
     * Uses toroidal (wrapping) boundary conditions.
     */
    private int countLiveNeighbors(Cell cell) {
        int liveNeighbors = 0;
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) continue; // Skip the cell itself

                int neighborRow = ((cell.row() + dRow) % rows + rows) % rows; 
                int neighborCol = ((cell.col() + dCol) % cols + cols) % cols;

                // Check if it's a live cell
                if (liveCells.contains(new Cell(neighborRow, neighborCol))) {
                    liveNeighbors++;
                }
            }
        }
        return liveNeighbors;
    }
}
