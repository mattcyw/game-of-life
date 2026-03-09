package com.example.bgl.gol.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.bgl.gol.config.ApplicationConfigKeys;
import com.example.bgl.gol.config.SortingStrategyOptions;
import com.example.bgl.gol.model.Cell;

/**
 * A {@link CellSortingStrategy} that applies the slot-preservation algorithm:
 *
 * <ol>
 *   <li>Walk the previous generation's ordered list slot by slot.</li>
 *   <li>Survivor  → keeps its slot position exactly.</li>
 *   <li>Dead cell → the closest unused new birth in the same row fills the
 *                   slot (ties broken by smallest |Δcol|).
 *                   If no same-row birth exists the slot is dropped.</li>
 *   <li>Births not used as substitutes are appended in (row, col) order.</li>
 * </ol>
 *
 * <p>This class is stateless — all work is done inside {@link #sort}, so
 * the same instance can be reused across generations and shared between
 * multiple state machines.</p>
 */
@Component
@ConditionalOnProperty(
    value = ApplicationConfigKeys.SORTING_STRATEGY, 
    havingValue = SortingStrategyOptions.SLOT_PRESERVING
)
public class SlotPreservingSortingStrategy implements CellSortingStrategy {

    @Override
    public List<Cell> sort(List<Cell> previousOrder, Set<Cell> currentLive) {

        if (previousOrder == null || previousOrder.isEmpty()) {
            // First generation: no previous order to preserve, so just sort by (row, col).
            return currentLive.stream().sorted().collect(Collectors.toList());
        }

        // Derive new births: cells alive now that were not in the previous ordered list.
        Set<Cell> previousLive = new HashSet<>(previousOrder);
        Set<Cell> newBirths = currentLive.stream()
            .filter(c -> !previousLive.contains(c))
            .collect(Collectors.toCollection(HashSet::new));

        return buildOrderedList(previousOrder, currentLive, newBirths);
    }

    // -------------------------------------------------------------------------
    // Slot-preservation algorithm
    // -------------------------------------------------------------------------

    private List<Cell> buildOrderedList(
            List<Cell> previousOrder,
            Set<Cell>  currentLive,
            Set<Cell>  newBirths) {

        List<Cell> ordered    = new ArrayList<>();
        Set<Cell>  usedBirths = new HashSet<>();

        // Pass 1 — walk previous slots in order.
        for (Cell slot : previousOrder) {
            if (currentLive.contains(slot)) {
                // Survived — preserve slot position exactly.
                ordered.add(slot);
            } else {
                // Died — substitute the nearest same-row birth.
                Cell substitute = closestBirthSameRow(slot, newBirths, usedBirths);
                if (substitute != null) {
                    ordered.add(substitute);
                    usedBirths.add(substitute);
                }
                // No same-row birth available → slot is dropped (net population loss).
            }
        }

        // Pass 2 — append births not consumed as substitutes, in (row, col) order.
        newBirths.stream()
                 .filter(b -> !usedBirths.contains(b))
                 .sorted(Comparator.comparingInt(Cell::row)
                                   .thenComparingInt(Cell::col))
                 .forEach(ordered::add);

        return ordered;
    }

    /**
     * Returns the unused birth that shares {@code dead}'s row and has the
     * smallest column distance to it, or {@code null} if none exists.
     */
    private Cell closestBirthSameRow(Cell dead, Set<Cell> births, Set<Cell> usedBirths) {
        return births.stream()
                     .filter(b -> !usedBirths.contains(b))
                     .filter(b -> b.row() == dead.row())
                     .min(Comparator.comparingInt(b -> Math.abs(b.col() - dead.col())))
                     .orElse(null);
    }
}
