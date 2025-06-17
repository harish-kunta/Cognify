package com.gigamind.cognify.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.gigamind.cognify.util.GameConfig;

public class MathGameEngine {
    private final Random random;
    private int currentAnswer;
    private String currentQuestion;
    private List<Integer> currentOptions;
    private int currentDifficulty;

    public MathGameEngine() {
        random = new Random();
        currentOptions = new ArrayList<>();
    }

    public void generateQuestion() {
        int a = random.nextInt(20) + 1;
        int b = random.nextInt(20) + 1;
        int op = random.nextInt(4); // 0:+ 1:- 2:* 3:/
        switch (op) {
            case 1:
                if (a < b) {
                    int temp = a;
                    a = b;
                    b = temp;
                }
                currentAnswer = a - b;
                currentQuestion = a + " - " + b + " = ?";
                currentDifficulty = 1; // subtraction
                break;
            case 2:
                a = random.nextInt(10) + 1;
                b = random.nextInt(10) + 1;
                currentAnswer = a * b;
                currentQuestion = a + " × " + b + " = ?";
                currentDifficulty = 2; // multiplication
                break;
            case 3:
                currentAnswer = a;
                int prod = a * b;
                currentQuestion = prod + " ÷ " + b + " = ?";
                currentDifficulty = 3; // division
                break;
            default:
                currentAnswer = a + b;
                currentQuestion = a + " + " + b + " = ?";
                currentDifficulty = 1; // addition
        }
        generateOptions();
    }

    private void generateOptions() {
        currentOptions.clear();
        currentOptions.add(currentAnswer);
        
        // Generate 3 fake options that are close to the real answer
        while (currentOptions.size() < 4) {
            int offset = random.nextInt(11) - 5; // -5..5
            int fake = currentAnswer + offset;
            if (fake <= 0 || currentOptions.contains(fake)) continue;
            currentOptions.add(fake);
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
        if (!correct) {
            return -5;
        }
        return GameConfig.BASE_SCORE * currentDifficulty;
    }

    public int getCurrentDifficulty() {
        return currentDifficulty;
    }

    public int getCurrentAnswer() {
        return currentAnswer;
    }
} 
