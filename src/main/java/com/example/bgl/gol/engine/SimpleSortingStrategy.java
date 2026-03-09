package com.example.bgl.gol.engine;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.example.bgl.gol.config.ApplicationConfigKeys;
import com.example.bgl.gol.config.SortingStrategyOptions;
import com.example.bgl.gol.model.Cell;

/**
 * A simple {@link CellSortingStrategy} that ignores the previous generation's order and
 * simply returns the current live cells sorted by (row, col). 
 */
@Component
@Primary
@ConditionalOnProperty(
    value = ApplicationConfigKeys.SORTING_STRATEGY, 
    havingValue = SortingStrategyOptions.SIMPLE,
    matchIfMissing = true
)
public class SimpleSortingStrategy implements CellSortingStrategy {

    @Override
    public List<Cell> sort(List<Cell> previousOrder, Set<Cell> currentLive) {

        // Ignores previous order and simply returns the current live cells sorted by (row, col).
        return currentLive.stream().sorted().collect(Collectors.toList());
    }
}
