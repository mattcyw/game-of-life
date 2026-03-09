# Game of Life

A command-line implementation of Conway's Game of Life using Java 17 and Spring Boot 3.

---

## Assumptions & Design Decisions

- **Toroidal grid**: The grid wraps at every edge — the top row is adjacent to the bottom row, and the left column is adjacent to the right column. The engine (`GameGridStateMachine`) applies this wrapping unconditionally; any integer coordinate is accepted and normalised using modular arithmetic.
- **Layered input validation**: Different categories of input are validated at the layer best equipped to handle them — see [Input Validation Design](#input-validation-design) below.
- **Duplicate cells**: Duplicate coordinates in the input are silently deduplicated.
- **Output ordering**: Two strategies are available for ordering live cells in the output (see `gol.sorting-strategy`):
  - `slot-preserving` *(default)* — survivors keep their slot position; a dead cell's slot is filled by the nearest new birth in the same row; unused births are appended sorted.
    This is to meet the exact ordering of the output live cells given in the requirement document.
  - `simple` — sorted by (row, col) every generation, for testing and demonstrating how the Strategy Pattern works.

---

## How to Build

```bat
./gradlew build
```

---

## How to Run

Using Spring Boot:

```bat
./gradlew bootRun --args="'[[5, 5], [6, 5], [7, 5], [5, 6], [6, 6], [7, 6]]'"
```

Using the built JAR directly:

```bat
java -jar build/libs/GameOfLife-0.0.1-SNAPSHOT.jar "[[5, 5], [6, 5], [7, 5], [5, 6], [6, 6], [7, 6]]"
```

## Input Validation Design

Validation is split across layers, with each layer responsible only for what it can meaningfully check:

| Layer | What is validated | On failure |
| --- | --- | --- |
| `CommandLineApplication` | Argument count, JSON parsability, non-empty cell list, coordinate pair length (must be `[row, col]`) | Logger error + `System.exit(1)` — these are simple data-type/existence checks, best surfaced immediately as CLI errors |
| `GameOfLifeSimulator` | `rows`, `cols`, `numGenerations` all ≥ 1 | `IllegalArgumentException` thrown — these are **configuration values** (not user input); a violation is a programming/misconfiguration error |
| `GameOfLifeSimulator` | Each initial-live-cell is within `[0, rows) × [0, cols)` | Informative error written to the `OutputStream`, then the method returns — this is **GOL-specific user-input validation**; the error belongs in the simulation output, not the application log |

---

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
gol:
  width:    200          # grid columns
  height:   200          # grid rows
  generations: 100       # number of generations to emit

  # Output ordering strategy: "simple" | "slot-preserving"
  sorting-strategy: "slot-preserving"
```

---

## Architecture

### `CommandLineApplication`

Entry point. Performs simple data-type and existence validation (arg count, JSON parse, coord pair length), then delegates to `GameOfLifeSimulator`. Catches `IllegalArgumentException` from the simulator (invalid configuration) and exits cleanly.

### `GameOfLifeSimulator`

Orchestrates validation and the multi-generation loop.

**Validation performed here** (see [Input Validation Design](#input-validation-design)):

- Throws `IllegalArgumentException` if `rows`, `cols`, or `numGenerations` < 1.
- Writes an informative error to the `OutputStream` and returns if any initial-live-cell is outside the grid bounds.

Each tick it calls `GameGridStateMachine.nextGeneration()` and pipes the result through the configured `CellSortingStrategy` before writing to the output stream.

Termination conditions (whichever comes first):

- Requested number of generations reached.
- All cells die — simulation stops immediately.
- Stable state detected (two consecutive identical states) — the same state is re-emitted for the remaining generations without further engine computation.

(Located in `com.example.bgl.gol.engine` package)

### `GameGridStateMachine`

The core Conway's Game of Life engine. Uses a sparse `Set<Cell>` representation — only live cells and their eight toroidal neighbours are evaluated each generation, so the algorithm is O(L) where L is the number of live cells, independent of grid size.

**No validation** — the constructor applies toroidal wrapping to any integer coordinate:  
`row = ((r % rows) + rows) % rows`, same for columns.  
This makes the engine a pure, always-valid state machine. Bounds checking is the caller's responsibility (`GameOfLifeSimulator`).

**Toroidal boundaries** — both input normalisation and neighbour lookups use the same modular arithmetic formula.

(Located in `com.example.bgl.gol.engine` package)

### `CellSortingStrategy`

Functional interface with two implementations selected via `gol.sorting-strategy`:

| Implementation | Behaviour |
| --- | --- |
| `SimpleSortingStrategy` | Returns current live cells sorted by (row, col). |
| `SlotPreservingSortingStrategy` | Survivors keep their slot; a dead cell's slot is filled by the nearest same-row birth; remaining births appended sorted. |

(Located in `com.example.bgl.gol.engine` package)

### `JsonUtil` / `CustomSingleLineSpacePrettyPrinter`

Format utilities. `JsonUtil` handles parsing (`int[][]` from JSON) and serialisation (`List<Cell>` → `[[r, c], ...]` with comma-space separators).

---

## Performance Notes

- **Big-O Analysis**: The engine considers only live cells L and their neighbours O(L) per generation, so performance is not affected by grid size N×M. The algorithm is efficient for large, sparse populations. The worst case — all cells alive — equals a full grid scan O(N×M).

- **Reality Check**: When L ≈ N×M, the HashSet overhead may make the O(L) path marginally slower than a dense array scan, but extremely dense populations rarely survive more than a few generations.

- **Stable-state short-circuit**: Once two consecutive states are identical, all remaining generations are emitted from cache — no further engine computation occurs.

---

## Testing

### Strategy — JUnit vs. Cucumber

| Concern | Tool | Rationale |
| --- | --- | --- |
| Named business scenarios (Loner Dies, Blinker, Glider, Stabilises) | **Cucumber** `GameGridEngine.feature` | Human-readable acceptance tests traceable to requirements. |
| Constructor contract (OOB toroidal wrapping, deduplication, empty input) | **JUnit** `GameGridStateMachineTest` | White-box: tests exact normalised coordinates and object state. |
| Isolated Conway rules (underpopulation, overpopulation, reproduction, survival) | **JUnit** `GameGridStateMachineTest` | One rule per test; surgical assertions; no business narrative needed. |
| Toroidal boundary exact coordinates | **JUnit** `GameGridStateMachineTest` | Precise `[row,col]` assertions are awkward to express in Gherkin. |
| Simulator loop behaviour (termination, I/O) | **JUnit** `GameOfLifeSimulatorTest` | Tests the orchestration layer independently of the engine. |
| Config validation (rows/cols/numGenerations ≥ 1) | **JUnit** `GameOfLifeSimulatorTest` | Throws `IllegalArgumentException`; CLI catches and exits. |
| OOB cell user-input validation | **JUnit** `GameOfLifeSimulatorTest` | Error written to `OutputStream`; simulation skipped. |
| Sorting strategy contract | **JUnit** `SortingStrategyTest` | Both implementations and edge cases tested in isolation. |
| JSON utility and model | **JUnit** `JsonUtilTest`, `CellTest` | Pure unit tests of utility methods and the `Cell` record. |

### Running Tests

```bat
./gradlew test
```

### Code Coverage

JaCoCo is integrated and runs automatically after every test task:

```bat
./gradlew test jacocoTestReport
```

HTML report: `build/reports/jacoco/test/html/index.html`

Current coverage: **95% instructions / 97% branches** (excluding `CommandLineApplication` and configuration classes).
