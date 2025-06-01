package com.dailybraingame.engine;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WordGameEngine {
    private final Set<String> validWords;
    private final char[] letters;
    private final Random random;

    public WordGameEngine(Context context) {
        validWords = new HashSet<>();
        letters = new char[16];
        random = new Random();
        initializeValidWords(context);
        generateRandomLetters();
    }

    private void initializeValidWords(Context context) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("words.txt")))) {
            String word;
            while ((word = reader.readLine()) != null) {
                validWords.add(word.trim().toUpperCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateRandomLetters() {
        String vowels = "AEIOU";
        String consonants = "BCDFGHJKLMNPQRSTVWXYZ";
        Set<Character> usedLetters = new HashSet<>();
        List<Character> letterPool = new ArrayList<>();

        // Add unique vowels
        while (letterPool.size() < 4) {
            char vowel = vowels.charAt(random.nextInt(vowels.length()));
            if (usedLetters.add(vowel)) {
                letterPool.add(vowel);
            }
        }

        // Add unique consonants
        while (letterPool.size() < 16) {
            char consonant = consonants.charAt(random.nextInt(consonants.length()));
            if (usedLetters.add(consonant)) {
                letterPool.add(consonant);
            }
        }

        // Shuffle the letters
        Collections.shuffle(letterPool);

        for (int i = 0; i < 16; i++) {
            letters[i] = letterPool.get(i);
        }
    }

    public boolean isValidWord(String word) {
        return validWords.contains(word);
    }

    public int calculateScore(String word) {
        int length = word.length();
        if (!isValidWord(word)) return 0;
        
        switch (length) {
            case 3: return 10;
            case 4: return 20;
            case 5: return 40;
            case 6: return 70;
            default: return length >= 7 ? 100 : 0;
        }
    }

    public char[] getLetters() {
        return letters;
    }

    public void refreshGrid() {
        generateRandomLetters();
    }
} 