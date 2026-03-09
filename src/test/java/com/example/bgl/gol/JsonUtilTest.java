package com.example.bgl.gol;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.bgl.gol.model.Cell;
import com.example.bgl.gol.util.CustomSingleLineSpacePrettyPrinter;
import com.example.bgl.gol.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link JsonUtil} and {@link CustomSingleLineSpacePrettyPrinter}.
 */
class JsonUtilTest {

    private JsonUtil jsonUtil;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        CustomSingleLineSpacePrettyPrinter printer = new CustomSingleLineSpacePrettyPrinter();
        jsonUtil = new JsonUtil(mapper, printer);
    }

    // -----------------------------------------------------------------------
    // parse2DIntArray
    // -----------------------------------------------------------------------

    @Test
    void parse_validJson_returnsCorrectArray() throws Exception {
        int[][] result = jsonUtil.parse2DIntArray("[[1, 2], [3, 4]]");
        assertArrayEquals(new int[][]{{1, 2}, {3, 4}}, result);
    }

    @Test
    void parse_emptyArray_returnsEmpty2DArray() throws Exception {
        int[][] result = jsonUtil.parse2DIntArray("[]");
        assertEquals(0, result.length);
    }

    @Test
    void parse_singleCell_returnsOneEntry() throws Exception {
        int[][] result = jsonUtil.parse2DIntArray("[[5, 9]]");
        assertEquals(1, result.length);
        assertArrayEquals(new int[]{5, 9}, result[0]);
    }

    @Test
    void parse_nullInput_returnsEmptyArray() throws Exception {
        int[][] result = jsonUtil.parse2DIntArray(null);
        assertEquals(0, result.length);
    }

    @Test
    void parse_blankInput_returnsEmptyArray() throws Exception {
        int[][] result = jsonUtil.parse2DIntArray("   ");
        assertEquals(0, result.length);
    }

    @Test
    void parse_invalidJson_throwsJsonProcessingException() {
        JsonProcessingException ex = assertThrows(JsonProcessingException.class,
            () -> jsonUtil.parse2DIntArray("not-valid-json"));
        assertTrue(ex.getMessage().contains("Unrecognized token"),
            "Exception message should mention 'Unrecognized token'");
    }

    // -----------------------------------------------------------------------
    // toJsonString
    // -----------------------------------------------------------------------

    @Test
    void toJsonString_nullList_returnsEmptyArrayLiteral() throws Exception {
        String result = jsonUtil.toJsonString(null);
        assertEquals("[]", result);
    }

    @Test
    void toJsonString_emptyList_returnsEmptyArrayLiteral() throws Exception {
        String result = jsonUtil.toJsonString(List.of());
        assertEquals("[]", result);
    }

    @Test
    void toJsonString_singleCell_returnsWrappedArray() throws Exception {
        String result = jsonUtil.toJsonString(List.of(new Cell(3, 7)));
        assertEquals("[[3, 7]]", result);
    }

    @Test
    void toJsonString_multipleCells_hasSeparatorSpaces() throws Exception {
        String result = jsonUtil.toJsonString(List.of(new Cell(1, 1), new Cell(2, 2)));
        // CustomSingleLineSpacePrettyPrinter inserts ", " between items
        assertTrue(result.contains(", "), "Expected comma-space separator between array elements");
        assertEquals("[[1, 1], [2, 2]]", result);
    }
}
