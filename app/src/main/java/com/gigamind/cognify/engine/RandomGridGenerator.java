package com.gigamind.cognify.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.gigamind.cognify.util.GameConfig;

/**
 * Default {@link GridGenerator} implementation that uses random letters.
 */
public class RandomGridGenerator implements GridGenerator {
    private static final String VOWELS = "AEIOU";
    private static final String CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ";

    private final Random random;

    public RandomGridGenerator() {
        this(new Random());
    }

    public RandomGridGenerator(Random random) {
        this.random = random;
    }

    @Override
    public char[] generate() {
        int total = GameConfig.TOTAL_LETTERS;
        int maxVowels = total / 2;
        int availableVowels = VOWELS.length();

        int vowelCount = 2 + random.nextInt(maxVowels - 1);
        if (vowelCount > availableVowels) {
            vowelCount = availableVowels;
        }

        List<Character> vowelPool = new ArrayList<>();
        for (char c : VOWELS.toCharArray()) {
            vowelPool.add(c);
        }
        Collections.shuffle(vowelPool, random);

        int consonantNeeded = total - vowelCount;
        if (consonantNeeded > CONSONANTS.length()) {
            consonantNeeded = CONSONANTS.length();
        }

        List<Character> consonantPool = new ArrayList<>();
        for (char c : CONSONANTS.toCharArray()) {
            consonantPool.add(c);
        }
        Collections.shuffle(consonantPool, random);

        List<Character> combined = new ArrayList<>(total);
        for (int i = 0; i < vowelCount; i++) {
            combined.add(vowelPool.get(i));
        }
        for (int i = 0; i < consonantNeeded; i++) {
            combined.add(consonantPool.get(i));
        }

        Collections.shuffle(combined, random);

        char[] grid = new char[total];
        for (int i = 0; i < total; i++) {
            grid[i] = combined.get(i);
        }
        return grid;
    }
}
