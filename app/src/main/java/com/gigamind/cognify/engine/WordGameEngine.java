package com.gigamind.cognify.engine;

import android.content.Context;
import android.content.res.AssetManager;

import com.gigamind.cognify.exception.GameException;

import com.gigamind.cognify.util.GameConfig;
import com.gigamind.cognify.util.ExceptionLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import com.gigamind.cognify.engine.scoring.ExponentialWordScoreStrategy;
import com.gigamind.cognify.engine.scoring.ScoreStrategy;
import com.gigamind.cognify.engine.GridGenerator;
import com.gigamind.cognify.engine.RandomGridGenerator;

public class WordGameEngine {
    private static final String DICTIONARY_FILE = "words.txt";

    private final Set<String> dictionary;
    private final ScoreStrategy scoreStrategy;
    private final GridGenerator gridGenerator;
    private char[] currentGrid;

    public WordGameEngine(Set<String> dictionary) {
        this(dictionary, new ExponentialWordScoreStrategy(), new RandomGridGenerator());
    }

    public WordGameEngine(Set<String> dictionary, ScoreStrategy strategy) {
        this(dictionary, strategy, new RandomGridGenerator());
    }

    public WordGameEngine(Set<String> dictionary, ScoreStrategy strategy, GridGenerator generator) {
        this.dictionary = dictionary;
        this.scoreStrategy = strategy != null ? strategy : new ExponentialWordScoreStrategy();
        this.gridGenerator = generator != null ? generator : new RandomGridGenerator();
        this.currentGrid = generateGrid();
    }

    /**
     * Convenience constructor used mainly for unit tests. Loads the
     * dictionary from the given {@link Context}'s assets using the
     * built‑in "words.txt" file.
     */
    public WordGameEngine(Context context) {
        this(context, new ExponentialWordScoreStrategy());
    }

    public WordGameEngine(Context context, ScoreStrategy strategy) {
        this(loadDictionary(context), strategy, new RandomGridGenerator());
    }

    public char[] generateGrid() {
        currentGrid = gridGenerator.generate();
        return currentGrid.clone();
    }

    private static Set<String> loadDictionary(Context context) {
        Set<String> words = new HashSet<>();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    assetManager.open(DICTIONARY_FILE)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() >= GameConfig.MIN_WORD_LENGTH) {
                    words.add(line.toUpperCase());
                }
            }
            reader.close();
        } catch (IOException e) {
            ExceptionLogger.log("WordGameEngine", e);
            throw new GameException(
                    GameException.ErrorCode.DICTIONARY_LOAD_ERROR,
                    "Failed to load dictionary",
                    e
            );
        }
        return words;
    }

    public boolean isValidWord(String word) {
        if (word == null || word.length() < GameConfig.MIN_WORD_LENGTH) {
            return false;
        }
        return dictionary.contains(word.toUpperCase());
    }

    public int calculateScore(String word) {
        if (!isValidWord(word)) {
            return 0;
        }
        return scoreStrategy.calculateScore(word.toUpperCase());
    }

    public char[] getLetters() {
        return currentGrid.clone();
    }
} 
