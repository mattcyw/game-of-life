package com.example.bgl.gol.steps;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.bgl.gol.engine.CellSortingStrategy;
import com.example.bgl.gol.engine.GameGridStateMachine;
import com.example.bgl.gol.engine.SimpleSortingStrategy;
import com.example.bgl.gol.model.Cell;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class GameGridEngineSteps {

    // Test State
    private int rows;
    private int cols;
    private GameGridStateMachine grid;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CellSortingStrategy sortingStrategy = new SimpleSortingStrategy();

    // =======================================================
    // GIVEN Steps
    // =======================================================

    @Given("a game board is initialized with size {int} rows and {int} columns")
    public void a_game_board_is_initialized_with_size_rows_and_columns(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    @Given("the board's initial state is defined by: {string}")
    public void the_board_s_initial_state_is_defined_by(String jsonCoordinates) throws Exception {
        int[][] coords = mapper.readValue(jsonCoordinates.replaceAll("'", ""), int[][].class);
        this.grid = new GameGridStateMachine(this.rows, this.cols, coords);
    }

    // =======================================================
    // WHEN Step
    // =======================================================

    @When("the board advances {int} generations")
    public void the_board_advances_generations(int generations) {
        for (int i = 1; i <= generations; i++) {
            this.grid.nextGeneration();
        }
    }

    // =======================================================
    // THEN Step
    // =======================================================

    @Then("the board's live state should be: {string}")
    public void the_board_s_live_state_should_be(String expectedJsonCoordinates) throws Exception {

        // 1. Get Actual State (sorted via CellSortingStrategy)
        List<Cell> actualState = sortingStrategy.sort(Collections.emptyList(), this.grid.getCurrentLiveState());

        // 2. Parse Expected State (ensure it's sorted to ignore order differences)
        int[][] coords = mapper.readValue(expectedJsonCoordinates, int[][].class);

        HashSet<Cell> expectedState = new HashSet<>();
        for (int[] coord : coords) {
            expectedState.add(new Cell(coord[0], coord[1]));
        }

        List<Cell> expectedSortedState = sortingStrategy.sort(Collections.emptyList(), expectedState);

        // 3. Final Assertion
        assertEquals(expectedSortedState, actualState,
            "The final board state did not match the expected coordinates.");
    }
}