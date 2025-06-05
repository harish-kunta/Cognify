package com.gigamind.cognify.engine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashSet;
import java.util.Set;

public class GameStateManager {
    private static GameStateManager instance;
    private final MutableLiveData<Integer> score;
    private final MutableLiveData<Long> timeRemaining;
    private final Set<String> usedWords;
    private boolean isGameActive;

    private GameStateManager() {
        score = new MutableLiveData<>(0);
        timeRemaining = new MutableLiveData<>(0L);
        usedWords = new HashSet<>();
        isGameActive = false;
    }

    public static synchronized GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    public void startGame(long duration) {
        score.setValue(0);
        timeRemaining.setValue(duration);
        usedWords.clear();
        isGameActive = true;
    }

    public void endGame() {
        isGameActive = false;
    }

    public void addScore(int points) {
        Integer currentScore = score.getValue();
        if (currentScore != null && isGameActive) {
            score.setValue(currentScore + points);
        }
    }

    public boolean isWordUsed(String word) {
        return usedWords.contains(word);
    }

    public void addUsedWord(String word) {
        usedWords.add(word);
    }

    public void updateTimeRemaining(long time) {
        timeRemaining.setValue(time);
    }

    public LiveData<Integer> getScore() {
        return score;
    }

    public LiveData<Long> getTimeRemaining() {
        return timeRemaining;
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public void reset() {
        score.setValue(0);
        timeRemaining.setValue(0L);
        usedWords.clear();
        isGameActive = false;
    }
}
