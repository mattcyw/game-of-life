package com.example.bgl.gol;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.bgl.gol.engine.GameGridStateMachine;
import com.example.bgl.gol.model.Cell;

/**
 * Unit tests for {@link GameGridStateMachine}.
 *
 * <h2>Test strategy — JUnit vs. Cucumber split</h2>
 * <p>These JUnit tests cover <em>white-box</em> concerns that belong here and not in
 * the Cucumber feature file:</p>
 * <ul>
 *   <li><b>Constructor contract</b> — toroidal wrapping of OOB input, deduplication, empty input.</li>
 *   <li><b>Isolated Conway rules</b> — one rule per test, interior cells only (toroidal
 *       wrapping has no effect), surgical assertions.</li>
 *   <li><b>Toroidal boundary behaviour</b> — exact cell coordinates after wrapping; needs
 *       precise numeric assertions that would be awkward to express in Gherkin.</li>
 *   <li><b>Return-value contract</b> — {@code nextGeneration()} must return a sorted list.</li>
 * </ul>
 */
class GameGridStateMachineTest {

    // -----------------------------------------------------------------------
    // Constructor — toroidal wrapping of out-of-bounds input coordinates
    // (GameGridStateMachine never throws; it normalises any integer coordinate.)
    // -----------------------------------------------------------------------

    @Test
    void constructor_oobRow_isWrappedToroidally() {
        // Row 10 on a 10-row grid → wraps to row 0
        GameGridStateMachine grid = new GameGridStateMachine(10, 10, new int[][]{{10, 3}});
        assertTrue(grid.getCurrentLiveState().contains(new Cell(0, 3)),
            "Row 10 should wrap to row 0 on a 10-row grid");
    }

    @Test
    void constructor_oobColumn_isWrappedToroidally() {
        // Column 12 on a 10-col grid → wraps to column 2
        GameGridStateMachine grid = new GameGridStateMachine(10, 10, new int[][]{{3, 12}});
        assertTrue(grid.getCurrentLiveState().contains(new Cell(3, 2)),
            "Column 12 should wrap to column 2 on a 10-col grid");
    }

    @Test
    void constructor_negativeRow_isWrappedToroidally() {
        // Row -1 on a 10-row grid → wraps to row 9
        GameGridStateMachine grid = new GameGridStateMachine(10, 10, new int[][]{{-1, 3}});
        assertTrue(grid.getCurrentLiveState().contains(new Cell(9, 3)),
            "Row -1 should wrap to row 9 on a 10-row grid");
    }

    @Test
    void constructor_negativeColumn_isWrappedToroidally() {
        // Column -3 on a 10-col grid → wraps to column 7
        GameGridStateMachine grid = new GameGridStateMachine(10, 10, new int[][]{{3, -3}});
        assertTrue(grid.getCurrentLiveState().contains(new Cell(3, 7)),
            "Column -3 should wrap to column 7 on a 10-col grid");
    }

    // -----------------------------------------------------------------------
    // Constructor — valid inputs
    // -----------------------------------------------------------------------

    @Test
    void constructor_acceptsAllFourCornerCells() {
        GameGridStateMachine grid = new GameGridStateMachine(200, 200,
            new int[][]{{0, 0}, {0, 199}, {199, 0}, {199, 199}});
        assertEquals(4, grid.getCurrentLiveState().size());
    }

    @Test
    void constructor_emptyInput_givesEmptyLiveState() {
        GameGridStateMachine grid = new GameGridStateMachine(10, 10, new int[][]{});
        assertTrue(grid.getCurrentLiveState().isEmpty());
    }

    @Test
    void constructor_duplicateCells_deduplicatedInLiveState() {
        GameGridStateMachine grid = new GameGridStateMachine(10, 10,
            new int[][]{{3, 3}, {3, 3}, {3, 3}});
        assertEquals(1, grid.getCurrentLiveState().size());
    }

    @Test
    void getCurrentLiveState_returnsLiveCellsSet() {
        GameGridStateMachine grid = new GameGridStateMachine(10, 10,
            new int[][]{{1, 1}, {2, 2}});
        Set<Cell> state = grid.getCurrentLiveState();
        assertTrue(state.contains(new Cell(1, 1)));
        assertTrue(state.contains(new Cell(2, 2)));
    }

    // -----------------------------------------------------------------------
    // Conway's rules — isolated, interior cells (no toroidal effect)
    // Note: loner-dies, blinker, glider, and 3-gen-stabilise are in Cucumber.
    // -----------------------------------------------------------------------

    @Test
    void rule_underpopulation_cellWithOneNeighbor_dies() {
        GameGridStateMachine grid = new GameGridStateMachine(20, 20,
            new int[][]{{5, 5}, {5, 6}});
        grid.nextGeneration();
        assertTrue(grid.getCurrentLiveState().isEmpty());
    }

    @Test
    void rule_survival_cellWithTwoNeighbors_lives() {
        // Block — each cell has exactly 3 live neighbours; entire block survives.
        GameGridStateMachine grid = new GameGridStateMachine(20, 20,
            new int[][]{{5, 5}, {5, 6}, {6, 5}, {6, 6}});
        Set<Cell> before = Set.copyOf(grid.getCurrentLiveState());
        grid.nextGeneration();
        assertEquals(before, grid.getCurrentLiveState());
    }

    @Test
    void rule_overpopulation_cellWithFourNeighbors_dies() {
        // Centre cell [5,5] has 4 live neighbours → dies of overcrowding.
        GameGridStateMachine grid = new GameGridStateMachine(20, 20,
            new int[][]{{5, 5}, {4, 5}, {6, 5}, {5, 4}, {5, 6}});
        grid.nextGeneration();
        assertFalse(grid.getCurrentLiveState().contains(new Cell(5, 5)));
    }

    @Test
    void rule_reproduction_deadCellWithThreeNeighbors_isBorn() {
        // [5,5],[5,6],[6,5] → dead corner [6,6] has exactly 3 live neighbours → born.
        GameGridStateMachine grid = new GameGridStateMachine(20, 20,
            new int[][]{{5, 5}, {5, 6}, {6, 5}});
        grid.nextGeneration();
        assertTrue(grid.getCurrentLiveState().contains(new Cell(6, 6)));
    }

    @Test
    void nextGeneration_returnsSortedList() {
        GameGridStateMachine grid = new GameGridStateMachine(20, 20,
            new int[][]{{5, 7}, {5, 5}, {5, 6}});
        List<Cell> gen1 = grid.nextGeneration();
        for (int i = 0; i < gen1.size() - 1; i++) {
            assertTrue(gen1.get(i).compareTo(gen1.get(i + 1)) <= 0,
                "nextGeneration() result must be sorted");
        }
    }

    // -----------------------------------------------------------------------
    // Toroidal boundary — generic wrapping logic (not pattern-specific)
    // (Pattern-specific toroidal behavior is covered in Cucumber scenarios)
    // -----------------------------------------------------------------------

    @Test
    void toroidal_singleCellAtTopRow_neighborsBelowWrapFromBottom() {
        // Cell at [0, 5] should count neighbors from row 1 (below) and row 199 (wrapped from top)
        GameGridStateMachine grid = new GameGridStateMachine(200, 200,
            new int[][]{{0, 5}, {1, 5}, {199, 5}});
        grid.nextGeneration();
        // After one generation, the cell at [0,5] has 2 live neighbors [1,5] and [199,5],
        // so it survives (exactly 2 neighbors). All three cells have neighbors and survive.
        assertTrue(grid.getCurrentLiveState().contains(new Cell(0, 5)),
            "Cell at top row with 2 neighbors should survive");
    }

    @Test
    void toroidal_singleCellAtLeftColumn_neighborsFromRightWrapAround() {
        // Cell at [5, 0] should count neighbors from columns 1 (right) and 199 (wrapped from left)
        GameGridStateMachine grid = new GameGridStateMachine(200, 200,
            new int[][]{{5, 0}, {5, 1}, {5, 199}});
        grid.nextGeneration();
        assertTrue(grid.getCurrentLiveState().contains(new Cell(5, 0)),
            "Cell at left column with 2 neighbors should survive");
    }

    @Test
    void toroidal_cellAtBottomRightCorner_neighborsWrapFromAllSides() {
        // Cell at [199, 199] (bottom-right corner) should have neighbors from wrapped boundaries
        GameGridStateMachine grid = new GameGridStateMachine(200, 200,
            new int[][]{{199, 199}, {0, 199}, {199, 0}});
        grid.nextGeneration();
        // Each corner cell has 2 neighbors via wrapping, so all survive
        assertTrue(grid.getCurrentLiveState().contains(new Cell(199, 199)),
            "Cell at bottom-right corner with wrapped neighbors should survive");
    }

    @Test
    void toroidal_cellNearBoundary_countedCorrectlyWithWrapping() {
        // A 3x3 block straddling boundaries to verify wrapping neighbor counts
        GameGridStateMachine grid = new GameGridStateMachine(10, 10,
            new int[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}});
        Set<Cell> initial = Set.copyOf(grid.getCurrentLiveState());
        grid.nextGeneration();
        // Block is stable (each cell has 3 neighbors)
        assertEquals(initial, grid.getCurrentLiveState(),
            "Small block with wrapping boundaries should be stable");
    }
}
