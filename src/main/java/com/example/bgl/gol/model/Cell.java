package com.example.bgl.gol.model;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Immutable record representing a cell's coordinates (row, col).
 * Implements Comparable for consistent output sorting (row-major).
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public record Cell(int row, int col) implements Comparable<Cell> {

    @Override
    public int compareTo(Cell other) {
        int rowComparison = Integer.compare(this.row, other.row);
        if (rowComparison != 0) {
            return rowComparison;
        }
        return Integer.compare(this.col, other.col);
    }
}