package com.example.bgl.gol.engine;

import java.util.List;
import java.util.Set;

import com.example.bgl.gol.model.Cell;

/**
 * Strategy for ordering the live cells returned by
 *
 * <p>Implementations receive the full context needed to make ordering decisions:
 * the previous generation's ordered list and the current live-cell set.
 * They are responsible for deriving whatever additional information they need
 * (e.g. new births, survivors) from those two inputs.</p>
 *
 * <p>Being a {@code @FunctionalInterface}, simple strategies can be supplied
 * as lambdas at the call site.</p>
 */
@FunctionalInterface
public interface CellSortingStrategy {

    /**
     * Produces an ordered list of the current generation's live cells.
     *
     * @param previousOrder the slot-stable ordered list from the previous generation
     *                      (or the original input order for generation 0)
     * @param currentLive   all live cells in the current generation
     * @return              an ordered list containing exactly the cells in {@code currentLive}
     */
    List<Cell> sort(List<Cell> previousOrder, Set<Cell> currentLive);
}
