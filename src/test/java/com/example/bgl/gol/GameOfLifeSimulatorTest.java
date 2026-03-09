package com.example.bgl.gol;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.bgl.gol.engine.GameOfLifeSimulator;
import com.example.bgl.gol.engine.SimpleSortingStrategy;
import com.example.bgl.gol.util.CustomSingleLineSpacePrettyPrinter;
import com.example.bgl.gol.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link GameOfLifeSimulator}.
 * All tests use {@link SimpleSortingStrategy} to keep output predictable.
 */
class GameOfLifeSimulatorTest {

    private GameOfLifeSimulator simulator;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        CustomSingleLineSpacePrettyPrinter printer = new CustomSingleLineSpacePrettyPrinter();
        JsonUtil jsonUtil = new JsonUtil(mapper, printer);
        simulator = new GameOfLifeSimulator(jsonUtil, new SimpleSortingStrategy());
        out = new ByteArrayOutputStream();
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    // -----------------------------------------------------------------------
    // Generation
    // -----------------------------------------------------------------------
    @Test
    void generationNumbers_areSequential() throws Exception {
        simulator.simulate(20, 20,
            new int[][]{{5, 5}, {5, 6}, {6, 5}, {6, 6}}, 5, out);
        String[] lines = output().strip().split("\n");
        for (int i = 0; i < lines.length; i++) {
            assertTrue(lines[i].startsWith((i + 1) + ": "),
                "Line " + i + " should start with generation number " + (i + 1));
        }
    }

    // -----------------------------------------------------------------------
    // Empty / edge inputs
    // -----------------------------------------------------------------------

    @Test
    void emptyInput_noOutputEmitted() throws Exception {
        simulator.simulate(10, 10, new int[][]{}, 5, out);
        assertTrue(output().isBlank());
    }

    @Test
    void singleGeneration_exactlyOneLineEmitted() throws Exception {
        simulator.simulate(20, 20,
            new int[][]{{5, 5}, {5, 6}, {6, 5}, {6, 6}}, 1, out);
        long lineCount = output().lines().filter(l -> !l.isBlank()).count();
        assertEquals(1, lineCount);
    }

    @Test
    void zeroGenerations_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            simulator.simulate(20, 20, new int[][]{{5, 5}}, 0, out)
        );
        assertTrue(ex.getMessage().contains("numGenerations"),
            "Exception message should mention 'numGenerations'");
    }

    // -----------------------------------------------------------------------
    // Configuration validation (rows / cols / numGenerations must be ≥ 1)
    // -----------------------------------------------------------------------

    @Test
    void zeroRows_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            simulator.simulate(0, 10, new int[][]{{0, 0}}, 1, out));
        assertTrue(ex.getMessage().contains("rows"),
            "Exception message should mention 'rows'");
    }

    @Test
    void negativeRows_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            simulator.simulate(-1, 10, new int[][]{{0, 0}}, 1, out));
        assertTrue(ex.getMessage().contains("rows"),
            "Exception message should mention 'rows'");
    }

    @Test
    void zeroCols_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            simulator.simulate(10, 0, new int[][]{{0, 0}}, 1, out));
        assertTrue(ex.getMessage().contains("cols"),
            "Exception message should mention 'cols'");
    }

    @Test
    void negativeCols_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            simulator.simulate(10, -5, new int[][]{{0, 0}}, 1, out));
        assertTrue(ex.getMessage().contains("cols"),
            "Exception message should mention 'cols'");
    }

    @Test
    void configError_exceptionMessage_mentionsAllThreeParameters() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            simulator.simulate(0, -1, new int[][]{}, -1, out));
        String msg = ex.getMessage();
        assertTrue(msg.contains("rows"),          "Message should mention 'rows'");
        assertTrue(msg.contains("cols"),          "Message should mention 'cols'");
        assertTrue(msg.contains("numGenerations"),"Message should mention 'numGenerations'");
    }

    // -----------------------------------------------------------------------
    // Out-of-bounds input cells — error written to OutputStream (not thrown)
    // -----------------------------------------------------------------------

    @Test
    void outOfBounds_cell_errorWrittenToOutputStream() throws Exception {
        simulator.simulate(10, 10, new int[][]{{10, 0}, {5, 5}}, 5, out);
        assertFalse(output().isBlank(),
            "An error message must be written to the output stream for OOB cells");
    }

    @Test
    void outOfBounds_errorMessage_listsOffendingCells() throws Exception {
        simulator.simulate(200, 200, new int[][]{{200, 5}, {5, 5}, {5, 200}}, 2, out);
        String msg = output();
        assertTrue(msg.contains("[200, 5]"), "Error must name [200, 5]");
        assertTrue(msg.contains("[5, 200]"), "Error must name [5, 200]");
        assertFalse(msg.contains("[5, 5]"),  "In-bounds [5, 5] must not appear in the error");
    }

    @Test
    void outOfBounds_noSimulationOutputAfterError() throws Exception {
        simulator.simulate(10, 10, new int[][]{{10, 0}}, 5, out);
        // Only the error message is in the stream; no "1: [...]" generation lines
        String msg = output();
        assertFalse(msg.matches("(?s).*\\d+: \\[.*"),
            "No generation output should be emitted after an OOB error");
    }

    @Test
    void outOfBounds_negativeCoordinates_errorWrittenToOutputStream() throws Exception {
        simulator.simulate(10, 10, new int[][]{{-1, 5}}, 3, out);
        assertFalse(output().isBlank(),
            "Negative coordinates (OOB) must produce an error message in the output stream");
    }
}
