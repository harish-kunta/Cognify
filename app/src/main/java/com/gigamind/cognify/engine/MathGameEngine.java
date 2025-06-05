package com.gigamind.cognify.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MathGameEngine {
    private final Random random;
    private int currentAnswer;
    private String currentQuestion;
    private List<Integer> currentOptions;

    public MathGameEngine() {
        random = new Random();
        currentOptions = new ArrayList<>();
    }

    public void generateQuestion() {
        int a = random.nextInt(20) + 1;
        int b = random.nextInt(20) + 1;
        currentAnswer = a + b;
        currentQuestion = a + " + " + b + " = ?";
        generateOptions();
    }

    private void generateOptions() {
        currentOptions.clear();
        currentOptions.add(currentAnswer);
        
        // Generate 3 fake options that are close to the real answer
        while (currentOptions.size() < 4) {
            int fake = currentAnswer + (random.nextInt(7) - 3); // -3 to +3 from real answer
            if (fake > 0 && !currentOptions.contains(fake)) {
                currentOptions.add(fake);
            }
        }
        
        Collections.shuffle(currentOptions);
    }

    public String getCurrentQuestion() {
        return currentQuestion;
    }

    public List<Integer> getOptions() {
        return currentOptions;
    }

    public boolean checkAnswer(int answer) {
        return answer == currentAnswer;
    }

    public int getScore(boolean correct) {
        return correct ? 10 : 0;
    }

    public int getCurrentAnswer() {
        return currentAnswer;
    }
} 
