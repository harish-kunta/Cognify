package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.R;
import com.gigamind.cognify.engine.MathGameEngine;
import com.gigamind.cognify.util.Constants;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class QuickMathActivity extends AppCompatActivity {
    private MathGameEngine gameEngine;
    private TextView scoreText;
    private TextView questionCountText;
    private TextView equationText;
    private MaterialButton[] answerButtons;
    private int currentScore;
    private int questionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_math);

        // Initialize views
        scoreText = findViewById(R.id.scoreText);
        questionCountText = findViewById(R.id.questionCountText);
        equationText = findViewById(R.id.equationText);
        
        answerButtons = new MaterialButton[4];
        answerButtons[0] = findViewById(R.id.answer1Button);
        answerButtons[1] = findViewById(R.id.answer2Button);
        answerButtons[2] = findViewById(R.id.answer3Button);
        answerButtons[3] = findViewById(R.id.answer4Button);

        // Initialize game
        gameEngine = new MathGameEngine();
        currentScore = 0;
        questionCount = 0;

        // Set up click listeners for answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            final int index = i;
            answerButtons[i].setOnClickListener(v -> checkAnswer(index));
        }

        // Start first question
        nextQuestion();
    }

    private void nextQuestion() {
        if (questionCount >= Constants.TOTAL_QUESTIONS) {
            endGame();
            return;
        }

        questionCount++;
        questionCountText.setText(String.format("Question %d/%d", questionCount, Constants.TOTAL_QUESTIONS));

        gameEngine.generateQuestion();
        equationText.setText(gameEngine.getCurrentQuestion());

        List<Integer> options = gameEngine.getOptions();
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(String.valueOf(options.get(i)));
        }
    }

    private void checkAnswer(int buttonIndex) {
        int selectedAnswer = Integer.parseInt(answerButtons[buttonIndex].getText().toString());
        boolean isCorrect = gameEngine.checkAnswer(selectedAnswer);
        
        int points = gameEngine.getScore(isCorrect);
        currentScore += points;
        scoreText.setText(currentScore);

        // Visual feedback (could be enhanced with animations)
        answerButtons[buttonIndex].setBackgroundTintList(
            getColorStateList(isCorrect ? R.color.success : R.color.text_secondary)
        );

        // Delay before next question to show feedback
        answerButtons[buttonIndex].postDelayed(() -> {
            answerButtons[buttonIndex].setBackgroundTintList(
                getColorStateList(R.color.button_background)
            );
            nextQuestion();
        }, 500);
    }

    private void endGame() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(Constants.INTENT_SCORE, currentScore);
        intent.putExtra(Constants.INTENT_TIME, questionCount);
        intent.putExtra(Constants.INTENT_TYPE, Constants.TYPE_QUICK_MATH);
        startActivity(intent);
        finish();
    }
} 