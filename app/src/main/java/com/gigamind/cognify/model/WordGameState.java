package com.gigamind.cognify.model;

import java.util.ArrayList;
import java.util.List;

public class WordGameState {
    private final int score;
    private final long timeRemaining;
    private final String currentWord;
    private final List<String> foundWords;
    private final char[] letters;
    private final boolean isGameActive;

    private WordGameState(Builder builder) {
        this.score = builder.score;
        this.timeRemaining = builder.timeRemaining;
        this.currentWord = builder.currentWord;
        this.foundWords = new ArrayList<>(builder.foundWords);
        this.letters = builder.letters.clone();
        this.isGameActive = builder.isGameActive;
    }

    public int getScore() {
        return score;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public List<String> getFoundWords() {
        return new ArrayList<>(foundWords);
    }

    public char[] getLetters() {
        return letters.clone();
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public static class Builder {
        private int score;
        private long timeRemaining;
        private String currentWord;
        private List<String> foundWords;
        private char[] letters;
        private boolean isGameActive;

        public Builder() {
            this.score = 0;
            this.timeRemaining = 0;
            this.currentWord = "";
            this.foundWords = new ArrayList<>();
            this.letters = new char[16];
            this.isGameActive = false;
        }

        public Builder score(int score) {
            this.score = score;
            return this;
        }

        public Builder timeRemaining(long timeRemaining) {
            this.timeRemaining = timeRemaining;
            return this;
        }

        public Builder currentWord(String currentWord) {
            this.currentWord = currentWord;
            return this;
        }

        public Builder foundWords(List<String> foundWords) {
            this.foundWords = new ArrayList<>(foundWords);
            return this;
        }

        public Builder letters(char[] letters) {
            this.letters = letters.clone();
            return this;
        }

        public Builder isGameActive(boolean isGameActive) {
            this.isGameActive = isGameActive;
            return this;
        }

        public WordGameState build() {
            return new WordGameState(this);
        }
    }
}
