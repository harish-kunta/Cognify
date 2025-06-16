package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.gigamind.cognify.util.GameConfig;
import com.gigamind.cognify.util.GameTimer;

import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.R;
import com.gigamind.cognify.engine.MathGameEngine;
import com.gigamind.cognify.util.Constants;
import com.google.android.material.button.MaterialButton;
import com.gigamind.cognify.analytics.GameAnalytics;
import com.gigamind.cognify.util.GameType;

import java.util.List;

public class QuickMathActivity extends AppCompatActivity {
    private MathGameEngine gameEngine;
    private TextView scoreText;
    private TextView timerText;
    private TextView equationText;
    private MaterialButton[] answerButtons;
    private int currentScore;
    private GameAnalytics analytics;
    private long questionStartTime;
    private GameTimer gameTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_math);

        int challengeScore = getIntent().getIntExtra(Constants.EXTRA_CHALLENGE_SCORE, -1);
        if (challengeScore >= 0) {
            Toast.makeText(this, getString(R.string.challenge_toast, challengeScore), Toast.LENGTH_LONG).show();
        }

        analytics = GameAnalytics.getInstance(this);
        analytics.logScreenView(Constants.ANALYTICS_SCREEN_QUICK_MATH);
        analytics.logGameStart(GameType.MATH);

        // Initialize views
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        equationText = findViewById(R.id.equationText);
        
        answerButtons = new MaterialButton[4];
        answerButtons[0] = findViewById(R.id.answer1Button);
        answerButtons[1] = findViewById(R.id.answer2Button);
        answerButtons[2] = findViewById(R.id.answer3Button);
        answerButtons[3] = findViewById(R.id.answer4Button);

        // Initialize game
        gameEngine = new MathGameEngine();
        currentScore = 0;

        // Set up click listeners for answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            final int index = i;
            answerButtons[i].setOnClickListener(v -> checkAnswer(index));
        }

        // Start timer & first question
        startGameTimer();
        nextQuestion();
    }

    private void nextQuestion() {
        questionStartTime = System.currentTimeMillis();

        gameEngine.generateQuestion();
        equationText.setText(gameEngine.getCurrentQuestion());

        List<Integer> options = gameEngine.getOptions();
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(String.valueOf(options.get(i)));
        }
    }

    private void startGameTimer() {
        gameTimer = new GameTimer.Builder()
                .duration(GameConfig.QUICK_MATH_DURATION_MS)
                .tickInterval(1000)
                .listener(new GameTimer.Listener() {
                    @Override
                    public void onTick(long millisRemaining) {
                        timerText.setText(String.valueOf(millisRemaining / 1000));
                    }

                    @Override
                    public void onFinish() {
                        endGame();
                    }
                })
                .build();
        timerText.setText(String.valueOf(GameConfig.QUICK_MATH_DURATION_MS / 1000));
        gameTimer.start();
    }

    private void checkAnswer(int buttonIndex) {
        int selectedAnswer = Integer.parseInt(answerButtons[buttonIndex].getText().toString());
        boolean isCorrect = gameEngine.checkAnswer(selectedAnswer);
        long timeSpent = System.currentTimeMillis() - questionStartTime;
        
        analytics.logMathAnswer(isCorrect, timeSpent);
        analytics.logButtonClick("answer_" + selectedAnswer);

        int points = gameEngine.getScore(isCorrect);
        currentScore += points;
        scoreText.setText(String.valueOf(currentScore));

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
        if (gameTimer != null) gameTimer.stop();
        analytics.logGameEnd(GameType.MATH,
            currentScore,
            (int)(GameConfig.QUICK_MATH_DURATION_MS / 1000),
            true);
        
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(Constants.INTENT_SCORE, currentScore);
        intent.putExtra(Constants.INTENT_TIME, (int)(GameConfig.QUICK_MATH_DURATION_MS / 1000));
        intent.putExtra(Constants.INTENT_TYPE, Constants.TYPE_QUICK_MATH);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameTimer != null) gameTimer.stop();
        analytics.logGameEnd(GameType.MATH,
            currentScore,
            (int)(GameConfig.QUICK_MATH_DURATION_MS / 1000),
            false);
    }
} 
