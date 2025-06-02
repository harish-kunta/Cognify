package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.FoundWordsAdapter;
import com.gigamind.cognify.engine.GameStateManager;
import com.gigamind.cognify.engine.WordGameEngine;
import com.gigamind.cognify.util.Constants;
import com.gigamind.cognify.util.GameConfig;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class WordDashActivity extends AppCompatActivity {
    private WordGameEngine gameEngine;
    private GameStateManager gameStateManager;
    private TextView scoreText, timerText, currentWordText;
    private GridLayout letterGrid;
    private RecyclerView foundWordsRecycler;
    private FoundWordsAdapter foundWordsAdapter;

    private StringBuilder currentWord;
    private CountDownTimer countDownTimer;
    private List<String> foundWordsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_dash);

        initializeViews();

        initializeGame();

        setupUI();

        // Initialize game
        gameEngine = new WordGameEngine(getApplicationContext());
        currentWord = new StringBuilder();
        foundWordsList = new ArrayList<>();

        setupFoundWordsRecycler();
        setupLetterGrid();

        startGame();
    }

    private void startGame() {
        gameStateManager.startGame(GameConfig.WORD_DASH_DURATION_MS);
        startGameTimer();
    }

    private void handleBackspace(View v) {
        if (currentWord.length() > 0) {
            currentWord.deleteCharAt(currentWord.length() - 1);
            currentWordText.setText(currentWord.toString());
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        currentWordText = findViewById(R.id.currentWordText);
        letterGrid = findViewById(R.id.letterGrid);
        foundWordsRecycler = findViewById(R.id.foundWordsRecycler);
    }

    private void initializeGame() {
        gameEngine = new WordGameEngine(this);
        gameStateManager = GameStateManager.getInstance();
        currentWord = new StringBuilder();
        setupFoundWordsRecycler();
    }

    private void setupUI() {
        setupLetterGrid();
        setupButtons();
        observeGameState();
    }

    private void observeGameState() {
        gameStateManager.getScore().observe(this, score ->
                scoreText.setText(getString(R.string.score_format, score)));

        gameStateManager.getTimeRemaining().observe(this, timeRemaining ->
                timerText.setText(getString(R.string.time_format, timeRemaining / 1000)));
    }

    private void setupButtons() {
        MaterialButton submitButton = findViewById(R.id.submitButton);
        MaterialButton clearButton = findViewById(R.id.clearButton);
        MaterialButton backspaceButton = findViewById(R.id.backspaceButton);

        submitButton.setOnClickListener(v -> submitWord());
        clearButton.setOnClickListener(v -> clearWord());
        backspaceButton.setOnClickListener(this::handleBackspace);
    }

    private void setupFoundWordsRecycler() {
        foundWordsAdapter = new FoundWordsAdapter();

        // FlexboxLayoutManager: items will flow left→right, then wrap onto next line
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(com.google.android.flexbox.AlignItems.STRETCH);

        foundWordsRecycler.setLayoutManager(layoutManager);
        foundWordsRecycler.setAdapter(foundWordsAdapter);
    }

    private void setupLetterGrid() {
        char[] letters = gameEngine.getLetters();
        letterGrid.removeAllViews();

        for (int i = 0; i < 16; i++) {
            ContextThemeWrapper themedContext =
                    new ContextThemeWrapper(this, R.style.Button_Letter);

            MaterialButton button = new MaterialButton(themedContext, null, 0);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 4, 1f);
            params.rowSpec = GridLayout.spec(i / 4, 1f);
            params.setMargins(4, 4, 4, 4);
            button.setLayoutParams(params);

            button.setText(String.valueOf(letters[i]));
            button.setMinHeight(48);
            button.setMinWidth(48);
            button.setGravity(Gravity.CENTER);

            final int index = i;
            button.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                animateButtonPress(button);
                onLetterClick(letters[index]);
            });
            letterGrid.addView(button);
        }
    }

    private void animateButtonPress(MaterialButton button) {
        button.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(GameConfig.BUTTON_SCALE_DURATION_MS)
                .withEndAction(() ->
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(GameConfig.BUTTON_SCALE_DURATION_MS)
                                .start()
                ).start();
    }

    private void onLetterClick(char letter) {
        currentWord.append(letter);
        currentWordText.setText(currentWord.toString());
    }

    private void submitWord() {
        String word = currentWord.toString();

        if (word.length() < GameConfig.MIN_WORD_LENGTH) {
            showError(getString(R.string.word_too_short));
            return;
        }

        if (!gameEngine.isValidWord(word)) {
            showError(getString(R.string.invalid_word_formation));
            return;
        }

        if (gameStateManager.isWordUsed(word)) {
            showError(getString(R.string.word_already_used));
            return;
        }

        int points = gameEngine.calculateScore(word);
        if (points > 0) {
            gameStateManager.addScore(points);
            gameStateManager.addUsedWord(word);

            animateScoreIncrease();

            foundWordsList.add(word);
            foundWordsAdapter.submitList(new ArrayList<>(foundWordsList));
        } else {
            showError(getString(R.string.invalid_word));
            scoreText.performHapticFeedback(HapticFeedbackConstants.REJECT);
            letterGrid.animate().translationX(10).setDuration(50).withEndAction(() ->
                    letterGrid.animate().translationX(-10).setDuration(50).withEndAction(() ->
                            letterGrid.animate().translationX(0).setDuration(50).start()
                    ).start()
            ).start();
        }
        clearWord();
    }

    private void animateScoreIncrease() {
        scoreText.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        scoreText.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(GameConfig.SCORE_ANIMATION_DURATION_MS)
                .withEndAction(() ->
                        scoreText.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(GameConfig.SCORE_ANIMATION_DURATION_MS)
                                .start()
                ).start();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        letterGrid.performHapticFeedback(HapticFeedbackConstants.REJECT);
    }

    private void clearWord() {
        currentWord.setLength(0);
        currentWordText.setText("");
    }

    private void startGameTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(GameConfig.WORD_DASH_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                gameStateManager.updateTimeRemaining(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private void endGame() {
        gameStateManager.endGame();

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.game_over)
                .setMessage(getString(R.string.final_score, gameStateManager.getScore().getValue()))
                .setCancelable(false)
                .setPositiveButton(R.string.view_results, (dialog, which) -> showResults())
                .show();
    }

    private void showResults() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(Constants.INTENT_SCORE, gameStateManager.getScore().getValue());
        intent.putExtra(Constants.INTENT_TIME, GameConfig.WORD_DASH_DURATION_MS / 1000);
        intent.putExtra(Constants.INTENT_TYPE, Constants.GAME_TYPE_WORD_DASH);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
} 