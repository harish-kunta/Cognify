package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.FoundWordsAdapter;
import com.gigamind.cognify.engine.WordGameEngine;
import com.gigamind.cognify.util.Constants;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordDashActivity extends AppCompatActivity {
    private WordGameEngine gameEngine;
    private TextView scoreText, timerText, currentWordText;
    private GridLayout letterGrid;
    private RecyclerView foundWordsRecycler;
    private FoundWordsAdapter foundWordsAdapter;

    private StringBuilder currentWord;
    private int currentScore;
    private CountDownTimer countDownTimer;
    private Set<String> usedWords;
    private List<String> foundWordsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_dash);

        // Initialize views
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        currentWordText = findViewById(R.id.currentWordText);
        letterGrid = findViewById(R.id.letterGrid);
        foundWordsRecycler = findViewById(R.id.foundWordsRecycler);

        MaterialButton submitButton = findViewById(R.id.submitButton);
        MaterialButton clearButton = findViewById(R.id.clearButton);
        MaterialButton backspaceButton = findViewById(R.id.backspaceButton);

        // Initialize game
        gameEngine = new WordGameEngine(getApplicationContext());
        currentWord = new StringBuilder();
        currentScore = 0;
        usedWords = new HashSet<>();
        foundWordsList = new ArrayList<>();

        setupFoundWordsRecycler();
        setupLetterGrid();

        // Set up buttons
        submitButton.setOnClickListener(v -> submitWord());
        clearButton.setOnClickListener(v -> clearWord());
        backspaceButton.setOnClickListener(v -> {
            if (currentWord.length() > 0) {
                currentWord.deleteCharAt(currentWord.length() - 1);
                currentWordText.setText(currentWord.toString());
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        });

        // Start timer
        startGameTimer();
    }

    private void setupFoundWordsRecycler() {
        foundWordsAdapter = new FoundWordsAdapter();
        foundWordsRecycler.setAdapter(foundWordsAdapter);

        // FlexboxLayoutManager: items will flow left→right, then wrap onto next line
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(com.google.android.flexbox.AlignItems.STRETCH);

        foundWordsRecycler.setLayoutManager(layoutManager);

        // Initialize with an empty list
        foundWordsAdapter.submitList(new ArrayList<>(foundWordsList));
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
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(80).withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(80)
                ).start();
                onLetterClick(letters[index]);
            });
            letterGrid.addView(button);
        }
    }

    private void onLetterClick(char letter) {
        currentWord.append(letter);
        currentWordText.setText(currentWord.toString());
    }

    private void submitWord() {
        String word = currentWord.toString();
        if (word.length() < 3) {
            Toast.makeText(this, "Word must be at least 3 letters", Toast.LENGTH_SHORT).show();
            return;
        }

        int points = gameEngine.calculateScore(word);
        if (points > 0 && !usedWords.contains(word)) {
            usedWords.add(word);
            currentScore += points;
            scoreText.setText("Score: " + currentScore);
            scoreText.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            scoreText.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction(() ->
                    scoreText.animate().scaleX(1f).scaleY(1f).setDuration(100)
            ).start();

            foundWordsList.add(word);
            foundWordsAdapter.submitList(new ArrayList<>(foundWordsList));
        } else {
            scoreText.performHapticFeedback(HapticFeedbackConstants.REJECT);
            letterGrid.animate().translationX(10).setDuration(50).withEndAction(() ->
                    letterGrid.animate().translationX(-10).setDuration(50).withEndAction(() ->
                            letterGrid.animate().translationX(0).setDuration(50).start()
                    ).start()
            ).start();
        }
        clearWord();
    }

    private void clearWord() {
        currentWord.setLength(0);
        currentWordText.setText("");
    }

    private void startGameTimer() {
        countDownTimer = new CountDownTimer(Constants.WORD_DASH_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText((millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private void endGame() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(Constants.INTENT_SCORE, currentScore);
        intent.putExtra(Constants.INTENT_TIME, Constants.WORD_DASH_DURATION_MS / 1000);
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