package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import com.gigamind.cognify.ui.MainActivity;

import com.gigamind.cognify.util.GameConfig;
import com.gigamind.cognify.util.GameTimer;
import com.gigamind.cognify.util.TutorialHelper;
import com.gigamind.cognify.util.GameType;
import com.gigamind.cognify.ui.TutorialOverlay;

import com.gigamind.cognify.ui.BaseActivity;

import com.gigamind.cognify.R;
import com.gigamind.cognify.engine.MathGameEngine;
import com.gigamind.cognify.util.Constants;
import com.google.android.material.button.MaterialButton;
import com.gigamind.cognify.analytics.GameAnalytics;
import com.gigamind.cognify.util.SoundManager;

import java.util.List;

public class QuickMathActivity extends BaseActivity {
    private MathGameEngine gameEngine;
    private ImageView closeGame;
    private TextView scoreText;
    private TextView timerText;
    private TextView equationText;
    private MaterialButton[] answerButtons;
    private int currentScore;
    private int questionCount;
    private GameAnalytics analytics;
    private long questionStartTime;
    private GameTimer gameTimer;
    private boolean questionAnswered;
    private long timeRemaining;
    private long pauseTimestamp;
    private boolean finalCountdownPlayed = false;
    private TutorialHelper tutorialHelper;
    private boolean tutorialActive = false;
    private TutorialOverlay tutorialOverlay;
    private boolean isDailyChallenge = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_math);

        int challengeScore = getIntent().getIntExtra(Constants.EXTRA_CHALLENGE_SCORE, -1);
        isDailyChallenge = getIntent().getBooleanExtra(Constants.INTENT_IS_DAILY, false);
        if (challengeScore >= 0) {
            String msg = getString(R.string.challenge_toast, challengeScore);
            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show();
            rootView.announceForAccessibility(msg);
        }

        analytics = GameAnalytics.getInstance(this);
        analytics.logScreenView(Constants.ANALYTICS_SCREEN_QUICK_MATH);
        analytics.logGameStart(GameType.MATH);

        // Initialize views
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        equationText = findViewById(R.id.equationText);
        closeGame = findViewById(R.id.close_game);

        closeGame.setOnClickListener(view -> {
            SoundManager.getInstance(this).playPop();
            showExitDialog();
        });
        
        answerButtons = new MaterialButton[6];
        answerButtons[0] = findViewById(R.id.answer1Button);
        answerButtons[1] = findViewById(R.id.answer2Button);
        answerButtons[2] = findViewById(R.id.answer3Button);
        answerButtons[3] = findViewById(R.id.answer4Button);
        answerButtons[4] = findViewById(R.id.answer5Button);
        answerButtons[5] = findViewById(R.id.answer6Button);

        // Initialize game
        gameEngine = new MathGameEngine();
        currentScore = 0;
        questionCount = 0;
        timeRemaining = GameConfig.QUICK_MATH_DURATION_MS;

        // Set up click listeners for answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            final int index = i;
            answerButtons[i].setOnClickListener(v -> {
                SoundManager.getInstance(this).playButton();
                checkAnswer(index);
            });
        }

        tutorialHelper = new TutorialHelper(this, GameType.MATH);

        disableGameInteractions();
        if (!tutorialHelper.isTutorialCompleted()) {
            tutorialActive = true;
            View root = findViewById(android.R.id.content);
            tutorialOverlay = new TutorialOverlay(this);
            tutorialOverlay.addStep(equationText, getString(R.string.math_tutorial_step_equation));
            tutorialOverlay.addStep(answerButtons[0], getString(R.string.math_tutorial_step_options));
            tutorialOverlay.addStep(scoreText, getString(R.string.tutorial_step_score));
            tutorialOverlay.addStep(timerText, getString(R.string.tutorial_step_timer));
            tutorialOverlay.setOnComplete(() -> {
                String msg = getString(R.string.tutorial_complete);
                Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
                root.announceForAccessibility(msg);
                tutorialHelper.markTutorialCompleted();
                tutorialActive = false;
                enableGameInteractions();
                startGame();
            });
            equationText.post(tutorialOverlay::start);
        } else {
            enableGameInteractions();
            startGame();
        }
    }

    private void nextQuestion() {
        questionStartTime = System.currentTimeMillis();
        questionAnswered = false;
        setButtonsEnabled(true);

        gameEngine.generateQuestion();
        equationText.setText(gameEngine.getCurrentQuestion());

        List<Integer> options = gameEngine.getOptions();
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(String.valueOf(options.get(i)));
        }
    }

    private void startGameTimer() {
        gameTimer = new GameTimer.Builder()
                .duration(timeRemaining)
                .tickInterval(1000)
                .listener(new GameTimer.Listener() {
                    @Override
                    public void onTick(long millisRemaining) {
                        timeRemaining = millisRemaining;
                        timerText.setText(String.valueOf(millisRemaining / 1000));
                        if (millisRemaining <= GameConfig.FINAL_COUNTDOWN_MS && !finalCountdownPlayed) {
                            triggerFinalCountdown();
                            finalCountdownPlayed = true;
                        }
                    }

                    @Override
                    public void onFinish() {
                        timeRemaining = 0;
                        endGame();
                    }
                })
                .build();
        timerText.setText(String.valueOf(timeRemaining / 1000));
        gameTimer.start();
    }

    private void checkAnswer(int buttonIndex) {
        if (questionAnswered) {
            return;
        }
        questionAnswered = true;
        setButtonsEnabled(false);

        int selectedAnswer = Integer.parseInt(answerButtons[buttonIndex].getText().toString());
        boolean isCorrect = gameEngine.checkAnswer(selectedAnswer);
        long timeSpent = System.currentTimeMillis() - questionStartTime;
        
        analytics.logMathAnswer(isCorrect, timeSpent);
        analytics.logButtonClick("answer_" + selectedAnswer);

        if (isCorrect) {
            SoundManager.getInstance(this).playCorrect();
        } else {
            SoundManager.getInstance(this).playIncorrect();
        }

        int points = gameEngine.getScore(isCorrect, timeSpent);
        currentScore += points;
        questionCount++;
        scoreText.setText(String.valueOf(currentScore));
        showPointsPopup(points);

        // Visual feedback (could be enhanced with animations)
        answerButtons[buttonIndex].setBackgroundTintList(
            getColorStateList(isCorrect ? R.color.success : R.color.error)
        );

        // Delay before next question to show feedback
        answerButtons[buttonIndex].postDelayed(() -> {
            answerButtons[buttonIndex].setBackgroundTintList(
                getColorStateList(R.color.primary)
            );
            nextQuestion();
        }, 500);
    }

    private void endGame() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
        timeRemaining = 0;
        int finalScore = currentScore;
        analytics.logGameEnd(GameType.MATH,
            finalScore,
            (int)(GameConfig.QUICK_MATH_DURATION_MS / 1000),
            true);
        
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(Constants.INTENT_SCORE, finalScore);
        intent.putExtra(Constants.INTENT_TIME, (int)(GameConfig.QUICK_MATH_DURATION_MS / 1000));
        intent.putExtra(Constants.INTENT_TYPE, Constants.TYPE_QUICK_MATH);
        intent.putExtra(Constants.INTENT_IS_DAILY, isDailyChallenge);
        startActivity(intent);
        finish();
    }

    private void startGame() {
        finalCountdownPlayed = false;
        startGameTimer();
        nextQuestion();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
        pauseTimestamp = System.currentTimeMillis();
        int finalScore = currentScore;
        analytics.logGameEnd(GameType.MATH,
            finalScore,
            (int)(GameConfig.QUICK_MATH_DURATION_MS / 1000),
            false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timeRemaining > 0 && gameTimer == null && !tutorialActive) {
            // Adjust question start time to exclude paused duration
            if (pauseTimestamp > 0) {
                long pausedFor = System.currentTimeMillis() - pauseTimestamp;
                questionStartTime += pausedFor;
                pauseTimestamp = 0;
            }
            startGameTimer();
        }
    }

    private void disableGameInteractions() {
        setButtonsEnabled(false);
    }

    private void enableGameInteractions() {
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        for (MaterialButton btn : answerButtons) {
            btn.setEnabled(enabled);
        }
    }

    private void showPointsPopup(int points) {
        View root = findViewById(android.R.id.content);
        String msg = getString(R.string.points_popup_format, points);
        Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
        root.announceForAccessibility(msg);
    }

    private void triggerFinalCountdown() {
        SoundManager.getInstance(this).playHeartbeat();
        com.gigamind.cognify.animation.AnimationUtils.shake(timerText, 8f);
    }

    private void showExitDialog() {
        pauseGameTimer();
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.exit_game_confirm_title)
                .setMessage(R.string.exit_game_confirm_message)
                .setPositiveButton(R.string.exit_game_yes, (d, w) -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .setNegativeButton(R.string.continue_playing, (d, w) -> resumeGameTimer())
                .setOnCancelListener(d -> resumeGameTimer())
                .show();
    }

    private void pauseGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
        pauseTimestamp = System.currentTimeMillis();
    }

    private void resumeGameTimer() {
        if (timeRemaining > 0 && gameTimer == null && !tutorialActive) {
            if (pauseTimestamp > 0) {
                long pausedFor = System.currentTimeMillis() - pauseTimestamp;
                questionStartTime += pausedFor;
                pauseTimestamp = 0;
            }
            startGameTimer();
        }
    }
}
