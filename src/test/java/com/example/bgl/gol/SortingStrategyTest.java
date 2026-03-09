package com.example.bgl.gol;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.bgl.gol.engine.CellSortingStrategy;
import com.example.bgl.gol.engine.SimpleSortingStrategy;
import com.example.bgl.gol.engine.SlotPreservingSortingStrategy;
import com.example.bgl.gol.model.Cell;

/**
 * Unit tests for {@link SimpleSortingStrategy} and {@link SlotPreservingSortingStrategy}.
 */
class SortingStrategyTest {

    // =======================================================================
    // SimpleSortingStrategy
    // =======================================================================

    @Nested
    class SimpleSortingStrategyTests {

        private final CellSortingStrategy strategy = new SimpleSortingStrategy();

        @Test
        void emptySet_returnsEmptyList() {
            List<Cell> result = strategy.sort(Collections.emptyList(), Set.of());
            assertTrue(result.isEmpty());
        }

        @Test
        void singleCell_returnsSingletonList() {
            List<Cell> result = strategy.sort(Collections.emptyList(), Set.of(new Cell(3, 7)));
            assertEquals(List.of(new Cell(3, 7)), result);
        }

        @Test
        void multipleCells_returnedInRowMajorOrder() {
            Set<Cell> current = Set.of(
                new Cell(5, 8), new Cell(1, 1), new Cell(5, 2), new Cell(3, 4));
            List<Cell> result = strategy.sort(Collections.emptyList(), current);
            assertEquals(
                List.of(new Cell(1, 1), new Cell(3, 4), new Cell(5, 2), new Cell(5, 8)),
                result);
        }

        @Test
        void previousOrder_isCompletelyIgnored() {
            Set<Cell> current = Set.of(new Cell(2, 2), new Cell(1, 1));
            List<Cell> withEmptyPrev  = strategy.sort(Collections.emptyList(), current);
            List<Cell> withDifferentPrev = strategy.sort(List.of(new Cell(9, 9)), current);
            assertEquals(withEmptyPrev, withDifferentPrev);
        }

        @Test
        void result_containsExactlyLiveCells() {
            Set<Cell> current = Set.of(new Cell(0, 0), new Cell(1, 1), new Cell(2, 2));
            List<Cell> result = strategy.sort(Collections.emptyList(), current);
            assertEquals(3, result.size());
            assertTrue(result.containsAll(current));
        }
    }

    // =======================================================================
    // SlotPreservingSortingStrategy
    // =======================================================================

    @Nested
    class SlotPreservingSortingStrategyTests {

        private final CellSortingStrategy strategy = new SlotPreservingSortingStrategy();

        // ----- fall-back to simple sort when no previous order -----

        @Test
        void nullPreviousOrder_fallsBackToSimpleSort() {
            Set<Cell> current = Set.of(new Cell(5, 8), new Cell(1, 1));
            List<Cell> result = strategy.sort(null, current);
            assertEquals(List.of(new Cell(1, 1), new Cell(5, 8)), result);
        }

        @Test
        void emptyPreviousOrder_fallsBackToSimpleSort() {
            Set<Cell> current = Set.of(new Cell(5, 8), new Cell(1, 1));
            List<Cell> result = strategy.sort(Collections.emptyList(), current);
            assertEquals(List.of(new Cell(1, 1), new Cell(5, 8)), result);
        }

        // ----- all cells survive -----

        @Test
        void allSurvivors_preservesPreviousOrder() {
            List<Cell> previous = List.of(new Cell(5, 8), new Cell(1, 1), new Cell(3, 4));
            Set<Cell> current = Set.of(new Cell(5, 8), new Cell(1, 1), new Cell(3, 4));
            List<Cell> result = strategy.sort(previous, current);
            assertEquals(previous, result);
        }

        @Test
        void allSurvivors_orderPreservedEvenIfNotSortedByRowCol() {
            // previous order is desc; result must keep that order
            List<Cell> previous = List.of(new Cell(9, 9), new Cell(5, 5), new Cell(1, 1));
            Set<Cell> current = Set.of(new Cell(9, 9), new Cell(5, 5), new Cell(1, 1));
            assertEquals(previous, strategy.sort(previous, current));
        }

        // ----- survivor + new birth -----

        @Test
        void oneSurvivor_oneBirthAppended() {
            List<Cell> previous = List.of(new Cell(5, 5));
            Set<Cell> current = Set.of(new Cell(5, 5), new Cell(7, 7)); // 7,7 is a new birth
            List<Cell> result = strategy.sort(previous, current);
            assertEquals(List.of(new Cell(5, 5), new Cell(7, 7)), result);
        }

        // ----- dead cell substituted by same-row birth -----

        @Test
        void deadCell_sameRowBirth_fillsSlot() {
            List<Cell> previous = List.of(new Cell(5, 5), new Cell(5, 8));
            // Cell(5,8) dies; Cell(5,6) is born in the same row — should fill slot
            Set<Cell> current = Set.of(new Cell(5, 5), new Cell(5, 6));
            List<Cell> result = strategy.sort(previous, current);
            assertEquals(List.of(new Cell(5, 5), new Cell(5, 6)), result);
        }

        @Test
        void deadCell_multipleChoicesInSameRow_closestFillsSlot() {
            // Dead cell at (5,5); births at (5,3) dist-2 and (5,8) dist-3 → (5,3) wins
            List<Cell> previous = List.of(new Cell(5, 5));
            Set<Cell> current   = Set.of(new Cell(5, 3), new Cell(5, 8));
            List<Cell> result   = strategy.sort(previous, current);
            // Closest (5,3) takes the slot; (5,8) is appended
            assertEquals(new Cell(5, 3), result.get(0));
            assertTrue(result.contains(new Cell(5, 8)));
        }

        // ----- dead cell with no same-row birth — slot dropped -----

        @Test
        void deadCell_noBirthInSameRow_slotDroppedAndBirthAppended() {
            List<Cell> previous = List.of(new Cell(5, 5), new Cell(6, 5));
            // Cell(6,5) dies; new birth Cell(7,5) is in a DIFFERENT row
            Set<Cell> current = Set.of(new Cell(5, 5), new Cell(7, 5));
            List<Cell> result = strategy.sort(previous, current);
            assertEquals(List.of(new Cell(5, 5), new Cell(7, 5)), result);
        }

        @Test
        void allCellsDie_returnsEmptyList() {
            List<Cell> previous = List.of(new Cell(5, 5), new Cell(6, 5));
            Set<Cell> current = Set.of();
            List<Cell> result = strategy.sort(previous, current);
            assertTrue(result.isEmpty());
        }

        @Test
        void result_containsExactlyCurrentLiveCells() {
            List<Cell> previous = List.of(new Cell(1, 1), new Cell(2, 2));
            Set<Cell> current   = Set.of(new Cell(1, 1), new Cell(3, 3));
            List<Cell> result   = strategy.sort(previous, current);
            assertEquals(2, result.size());
            assertTrue(result.containsAll(current));
        }

        // ----- births not used as substitutes are appended in (row,col) order -----

        @Test
        void unusedBirths_appendedInSortedOrder() {
            List<Cell> previous = List.of(new Cell(5, 5));           // dies
            Set<Cell> current   = Set.of(new Cell(9, 9), new Cell(3, 3), new Cell(7, 7));
            // No same-row birth for dead (5,5) → slot dropped; all three births appended sorted
            List<Cell> result = strategy.sort(previous, current);
            assertEquals(List.of(new Cell(3, 3), new Cell(7, 7), new Cell(9, 9)), result);
        }
    }
}
