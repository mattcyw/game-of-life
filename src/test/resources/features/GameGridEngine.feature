Feature: Game Of Life Core Logic - Requirements Validation

  As a QA Engineer,
  I want tests to directly validate the core rules and provided examples
  So that the implementation strictly adheres to Conway's Game of Life.

  # -----------------------------------------------------------------------
  # Test strategy — JUnit vs. Cucumber split
  #
  # THESE CUCUMBER SCENARIOS cover named, stakeholder-facing behaviours:
  #   requirements acceptance criteria, known canonical patterns.
  #
  # REFERENCES:
  # - https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
  # - https://playgameoflife.com/
  # -----------------------------------------------------------------------

  Background:
    Given a game board is initialized with size 200 rows and 200 columns

  Scenario Outline: Validate Known Patterns
    Given the board's initial state is defined by: <Initial Cells>
    When the board advances <Generations> generations
    Then the board's live state should be: <Expected Cells>

    Examples: Core Requirements (From requirement document)
      | Description            | Initial Cells                                                | Generations | Expected Cells                                      |
      | Loner Dies             | '[[1, 1]]'                                                   | 1           | '[]'                                                |
      | Stabilizes             | '[[5, 5], [6, 5], [7, 5], [5, 6], [6, 6], [7, 6]]'           | 3           | '[[5, 5], [6, 4], [7, 5], [5, 6], [6, 7], [7, 6]]' |

    Examples: Still Lifes (Stable patterns that do not change)
      | Description            | Initial Cells                                                | Generations | Expected Cells                                      |
      | Block (2x2 square)     | '[[5, 5], [5, 6], [6, 5], [6, 6]]'                           | 5           | '[[5, 5], [5, 6], [6, 5], [6, 6]]'                  |
      | Beehive                | '[[4, 5], [4, 6], [5, 4], [5, 7], [6, 5], [6, 6]]'           | 3           | '[[4, 5], [4, 6], [5, 4], [5, 7], [6, 5], [6, 6]]'  |
      | Boat                   | '[[5, 5], [5, 6], [6, 5], [6, 7], [7, 6]]'                   | 4           | '[[5, 5], [5, 6], [6, 5], [6, 7], [7, 6]]'          |

    Examples: Oscillators (Period-2 patterns: return to initial state after 2 generations)
      | Description            | Initial Cells                                                | Generations | Expected Cells                                      |
      | Blinker (horizontal)   | '[[5, 5], [5, 6], [5, 7]]'                                   | 2           | '[[5, 5], [5, 6], [5, 7]]'                          |
      | Toad                   | '[[5, 6], [5, 7], [5, 8], [6, 7], [6, 8], [6, 9]]'           | 2           | '[[5, 6], [5, 7], [5, 8], [6, 7], [6, 8], [6, 9]]'  |
      | Beacon                 | '[[5, 5], [5, 6], [6, 5], [6, 6], [7, 7], [7, 8], [8, 7], [8, 8]]' | 2 | '[[5, 5], [5, 6], [6, 5], [6, 6], [7, 7], [7, 8], [8, 7], [8, 8]]' |

    Examples: Spaceships (Moving patterns: translate across the grid)
      | Description            | Initial Cells                                                | Generations | Expected Cells                                      |
      | Glider (100 gen)       | '[[10, 11], [11, 12], [12, 10], [12, 11], [12, 12]]'         | 100         | '[[35, 36], [36, 37], [37, 35], [37, 36], [37, 37]]'|

    Examples: Methuselahs (Long-lived: take many generations to stabilize or vanish)
      | Description            | Initial Cells                                                | Generations | Expected Cells                                      |
      | Die Hard (vanishes)    | '[[3, 2], [3, 3], [4, 3], [2, 8], [4, 7], [4, 8], [4, 9]]'   | 130         | '[]'                                                |
