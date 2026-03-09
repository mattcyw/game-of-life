package com.example.bgl.gol.config;

/**
 * Constants representing the available sorting strategies for ordering live cells in the Game of Life simulator.
 * This is used to map configuration values (from application.yml) to specific implementations of CellSortingStrategy.
 */
public class SortingStrategyOptions {
    
    public static final String SIMPLE = "simple";
    public static final String SLOT_PRESERVING = "slot-preserving";
    
    private SortingStrategyOptions() {
        // Private constructor to prevent instantiation
    }

}
