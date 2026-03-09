package com.example.bgl.gol;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.bgl.gol.model.Cell;

/**
 * Unit tests for the {@link Cell} record.
 */
class CellTest {

    @Test
    void recordAccessors_returnCorrectValues() {
        Cell c = new Cell(3, 7);
        assertEquals(3, c.row());
        assertEquals(7, c.col());
    }

    @Test
    void equality_sameCoordinates_areEqual() {
        assertEquals(new Cell(1, 2), new Cell(1, 2));
    }

    @Test
    void equality_differentRow_notEqual() {
        assertNotEquals(new Cell(1, 2), new Cell(2, 2));
    }

    @Test
    void equality_differentCol_notEqual() {
        assertNotEquals(new Cell(1, 2), new Cell(1, 3));
    }

    @Test
    void hashCode_sameCoordinates_sameHash() {
        assertEquals(new Cell(5, 5).hashCode(), new Cell(5, 5).hashCode());
    }

    @Test
    void compareTo_sortsByRowThenCol() {
        List<Cell> cells = new ArrayList<>(List.of(
            new Cell(5, 8), new Cell(1, 1), new Cell(5, 2), new Cell(1, 9)));
        cells.sort(null);
        assertEquals(List.of(
            new Cell(1, 1), new Cell(1, 9), new Cell(5, 2), new Cell(5, 8)), cells);
    }

    @Test
    void compareTo_smallerRow_isLess() {
        assertTrue(new Cell(1, 5).compareTo(new Cell(2, 5)) < 0);
    }

    @Test
    void compareTo_sameRow_smallerCol_isLess() {
        assertTrue(new Cell(3, 2).compareTo(new Cell(3, 9)) < 0);
    }

    @Test
    void compareTo_equalCells_isZero() {
        assertEquals(0, new Cell(4, 4).compareTo(new Cell(4, 4)));
    }

    @Test
    void toString_containsCoordinates() {
        String s = new Cell(3, 7).toString();
        assertTrue(s.contains("3"));
        assertTrue(s.contains("7"));
    }
}
