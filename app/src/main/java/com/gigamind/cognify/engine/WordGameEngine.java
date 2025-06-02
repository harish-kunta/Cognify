package com.gigamind.cognify.engine;

import android.content.Context;
import android.content.res.AssetManager;

import com.gigamind.cognify.util.GameConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WordGameEngine {
    private static final String DICTIONARY_FILE = "words.txt";
    private static final String VOWELS = "AEIOU";
    private static final String CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ";
    private static final double VOWEL_PROBABILITY = 0.4;
    private final Set<String> dictionary;
    private final Random random;
    private char[] currentGrid;

    public WordGameEngine(Context context) {
        dictionary = loadDictionary(context);
        random = new Random();
        currentGrid = generateGrid();
    }

    public char[] generateGrid() {
        int total = GameConfig.TOTAL_LETTERS;            // e.g. 16
        int maxVowels = total / 2;                       // e.g. 8
        int availableVowels = VOWELS.length();           // 5

        // 1) Decide how many vowels to include (at least 2, at most maxVowels, capped by 5).
        int vowelCount = 2 + random.nextInt(maxVowels - 1); // random between [2, maxVowels]
        if (vowelCount > availableVowels) {
            vowelCount = availableVowels;  // cannot pick more unique vowels than exist
        }

        // 2) Build a list of all vowels, shuffle, then take the first vowelCount.
        List<Character> vowelPool = new ArrayList<>();
        for (char c : VOWELS.toCharArray()) {
            vowelPool.add(c);
        }
        Collections.shuffle(vowelPool, random);

        // 3) Build a list of all consonants, shuffle, then pick (total - vowelCount) unique ones.
        int consonantNeeded = total - vowelCount;
        if (consonantNeeded > CONSONANTS.length()) {
            consonantNeeded = CONSONANTS.length(); // just in case, though normally TOTAL_LETTERS <= 26
        }

        List<Character> consonantPool = new ArrayList<>();
        for (char c : CONSONANTS.toCharArray()) {
            consonantPool.add(c);
        }
        Collections.shuffle(consonantPool, random);

        // 4) Combine the chosen vowels + consonants into a single list
        List<Character> combined = new ArrayList<>(total);
        for (int i = 0; i < vowelCount; i++) {
            combined.add(vowelPool.get(i));
        }
        for (int i = 0; i < consonantNeeded; i++) {
            combined.add(consonantPool.get(i));
        }

        // 5) Finally shuffle that combined list so letters appear in random positions
        Collections.shuffle(combined, random);

        // 6) Copy into currentGrid[]
        currentGrid = new char[total];
        for (int i = 0; i < total; i++) {
            currentGrid[i] = combined.get(i);
        }

        return currentGrid.clone();
    }

    private Set<String> loadDictionary(Context context) {
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
            throw new RuntimeException("Failed to load dictionary: " + e.getMessage());
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

        int score = GameConfig.BASE_SCORE;

        // Length bonus
        score += (word.length() - GameConfig.MIN_WORD_LENGTH) * GameConfig.LENGTH_BONUS;

        // Complexity bonus for less common letters
        for (char c : word.toCharArray()) {
            if ("JQXZ".indexOf(c) >= 0) {
                score += GameConfig.COMPLEXITY_BONUS;
            } else if ("KWVY".indexOf(c) >= 0) {
                score += GameConfig.COMPLEXITY_BONUS / 2;
            }
        }

        return score;
    }

    public char[] getLetters() {
        return currentGrid.clone();
    }
} 