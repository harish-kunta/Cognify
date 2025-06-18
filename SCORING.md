# Scoring & XP Guide

This document details how Cognify awards points and experience for each game mode.

## Word Dash

Word Dash challenges you to form words from a random 4×4 grid. Words are valid when they contain at least **3 letters**. Points are calculated using the active score strategy:

- **Base score:** `10` points for every valid word.
- **Length bonus:** `+5` for each letter beyond the minimum length.
- **Complexity bonus:** uncommon letters earn more:
  - `+8` for each J, Q, X or Z
  - `+4` for each K, W, V or Y
- **Exponential strategy:** by default the game uses an exponential strategy where
  the base score grows with the square of the word length. Complexity bonuses are
  multiplied by this length factor as well.

```
lengthFactor = wordLength - MIN_WORD_LENGTH + 1
score = BASE_SCORE * lengthFactor^2 + complexityBonus * lengthFactor
```

## Quick Math

Quick Math presents rapid arithmetic questions with weighted difficulty. The
formula mirrors the exponential rules from **Word Dash** so that both games
grant comparable points. Each answer yields points as follows:

```
if wrong:
    score = -5
else:
    diffFactor = difficulty
    base = BASE_SCORE * diffFactor^2
    bonus = LENGTH_BONUS * diffFactor
            * (MAX_RESPONSE_TIME_MS - responseTime) / MAX_RESPONSE_TIME_MS
    score = round(base + bonus)
```

The final game score is the sum of all question scores (no normalisation).

## Experience & Bonuses

After each game your XP increases by the final score. Additional bonuses apply:

- `+20 XP` when you beat your personal best for that game type.
- `+10 XP` if you also played yesterday (streak bonus).

### Badge Levels

Total accumulated XP unlocks badge tiers used on the leaderboard:

| XP Range | Badge |
|---------:|-------|
| 0 – 9,999 | Rookie |
| 10,000 – 19,999 | Learner |
| 20,000 – 29,999 | Thinker |
| 30,000 – 39,999 | Solver |
| 40,000 – 49,999 | Challenger |
| 50,000 – 59,999 | Strategist |
| 60,000 – 69,999 | Brainiac |
| 70,000 – 79,999 | Genius |
| 80,000 – 89,999 | Mastermind |
| 90,000+ | Legend |

These values correspond to `levels.txt` and `BadgeUtils` in the codebase.
