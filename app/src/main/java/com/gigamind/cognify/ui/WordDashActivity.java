package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
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
import com.gigamind.cognify.analytics.GameAnalytics;
import com.gigamind.cognify.engine.DictionaryProvider;
import com.gigamind.cognify.engine.GameStateManager;
import com.gigamind.cognify.engine.WordGameEngine;
import com.gigamind.cognify.util.Constants;
import com.gigamind.cognify.util.GameConfig;
import com.gigamind.cognify.util.GameType;
import com.gigamind.cognify.util.GameTimer;
import com.gigamind.cognify.util.TutorialHelper;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * WordDashActivity defers any use of `gameEngine` (and thus loading letters)
 * until the dictionary is fully loaded from assets. This prevents NPE and speeds up launch.
 */
public class WordDashActivity extends AppCompatActivity {
    private WordGameEngine gameEngine;
    private GameStateManager gameStateManager;
    private TextView scoreText, timerText, currentWordText;
    private GridLayout letterGrid;
    private RecyclerView foundWordsRecycler;
    private FoundWordsAdapter foundWordsAdapter;

    private StringBuilder currentWord;
    private GameTimer gameTimer;
    private List<String> foundWordsList;
    private boolean isDictionaryLoaded = false;

    private GameAnalytics analytics;
    private long wordStartTime;
    private TutorialHelper tutorialHelper;
    private boolean tutorialActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_dash);

        int challengeScore = getIntent().getIntExtra(Constants.EXTRA_CHALLENGE_SCORE, -1);
        if (challengeScore >= 0) {
            Toast.makeText(this, getString(R.string.challenge_toast, challengeScore), Toast.LENGTH_LONG).show();
        }

        analytics = GameAnalytics.getInstance(this);
        analytics.logScreenView(Constants.ANALYTICS_SCREEN_WORD_DASH);
        analytics.logGameStart(GameType.WORD);

        tutorialHelper = new TutorialHelper(this);
        
        // 1) Bind all views and set up UI scaffolding that does NOT use gameEngine yet
        initializeViews();
        setupButtons();
        observeGameState();
        setupFoundWordsRecycler();

        disableGameInteractions();

        // 2) Kick off dictionary loading. As soon as it's ready, we build gameEngine and letter grid.
        DictionaryProvider.getDictionaryAsync(
                getApplicationContext(),
                dictionary -> {
                    // Called on main thread once dictionary is loaded or on error (dictionary may be empty)
                    if (dictionary != null && !dictionary.isEmpty()) {
                        gameEngine = new WordGameEngine(dictionary);
                        foundWordsList = new ArrayList<>();
                        currentWord = new StringBuilder();
                        gameStateManager = GameStateManager.getInstance();
                        setupLetterGrid(); // Now safe—gameEngine is non-null

                        enableGameInteractions();
                        isDictionaryLoaded = true;
                        if (!tutorialHelper.isTutorialCompleted()) {
                            tutorialActive = true;
                            Toast.makeText(this, R.string.tutorial_start_hint, Toast.LENGTH_LONG).show();
                        }
                        startGame();       // Begin the game timer
                    } else {
                        // Handle dictionary load error
                        Toast.makeText(this, "Error loading game dictionary", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
        );
    }

    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        currentWordText = findViewById(R.id.currentWordText);
        letterGrid = findViewById(R.id.letterGrid);
        foundWordsRecycler = findViewById(R.id.foundWordsRecycler);
    }

    private void setupButtons() {
        MaterialButton submitButton = findViewById(R.id.submitButton);
        MaterialButton clearButton = findViewById(R.id.clearButton);
        MaterialButton backspaceButton = findViewById(R.id.backspaceButton);

        submitButton.setOnClickListener(v -> submitWord());
        clearButton.setOnClickListener(v -> clearWord());
        backspaceButton.setOnClickListener(this::handleBackspace);
    }

    private void observeGameState() {
        gameStateManager = GameStateManager.getInstance();
        gameStateManager.getScore().observe(this, score ->
                scoreText.setText(getString(R.string.score_format, score))
        );
        gameStateManager.getTimeRemaining().observe(this, timeRemaining ->
                timerText.setText(getString(R.string.time_format, timeRemaining / 1000))
        );
    }

    private void setupFoundWordsRecycler() {
        foundWordsAdapter = new FoundWordsAdapter();

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(com.google.android.flexbox.AlignItems.STRETCH);

        foundWordsRecycler.setLayoutManager(layoutManager);
        foundWordsRecycler.setAdapter(foundWordsAdapter);
    }

    /**
     * Populates the 4×4 letter grid. Because `gameEngine` is guaranteed non-null
     * here (called from within the dictionary callback), we can safely call getLetters().
     */
    private void setupLetterGrid() {
        // Clear any existing views (though at first run, letterGrid is empty)
        letterGrid.removeAllViews();

        // Retrieve the 16 letters from the engine
        char[] letters = gameEngine.getLetters();

        for (int i = 0; i < letters.length; i++) {
            ContextThemeWrapper themedContext =
                    new ContextThemeWrapper(this, R.style.Button_Letter);

            MaterialButton button = new MaterialButton(themedContext, null, 0);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;  // Use weight to distribute columns evenly
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
        if (wordStartTime == 0) {
            wordStartTime = System.currentTimeMillis();
        }
        currentWord.append(letter);
        currentWordText.setText(currentWord.toString());
        analytics.logButtonClick("letter_" + letter);
    }

    private void submitWord() {
        String word = currentWord.toString();
        long timeSpent = System.currentTimeMillis() - wordStartTime;

        if (word.length() < GameConfig.MIN_WORD_LENGTH) {
            showError(getString(R.string.word_too_short));
            analytics.logError("word_too_short", word);
            return;
        }

        if (!gameEngine.isValidWord(word)) {
            showError(getString(R.string.invalid_word_formation));
            analytics.logInvalidWord(word);
            return;
        }

        if (gameStateManager.isWordUsed(word)) {
            showError(getString(R.string.word_already_used));
            analytics.logError("word_already_used", word);
            return;
        }

        int points = gameEngine.calculateScore(word);
        if (points > 0) {
            gameStateManager.addScore(points);
            gameStateManager.addUsedWord(word);
            analytics.logWordFound(word, timeSpent);
            animateScoreIncrease();

            foundWordsList.add(word);
            foundWordsAdapter.submitList(new ArrayList<>(foundWordsList));
            if (tutorialActive) {
                Toast.makeText(this, R.string.tutorial_complete, Toast.LENGTH_SHORT).show();
                tutorialHelper.markTutorialCompleted();
                tutorialActive = false;
            }
        } else {
            showError(getString(R.string.invalid_word));
            analytics.logInvalidWord(word);
            scoreText.performHapticFeedback(HapticFeedbackConstants.REJECT);
            letterGrid
                    .animate()
                    .translationX(10)
                    .setDuration(50)
                    .withEndAction(() ->
                            letterGrid
                                    .animate()
                                    .translationX(-10)
                                    .setDuration(50)
                                    .withEndAction(() ->
                                            letterGrid
                                                    .animate()
                                                    .translationX(0)
                                                    .setDuration(50)
                                                    .start()
                                    ).start()
                    ).start();
        }
        clearWord();
        wordStartTime = 0;
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

    private void startGame() {
        gameStateManager.startGame(GameConfig.WORD_DASH_DURATION_MS);
        startGameTimer();
    }

    private void startGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new GameTimer.Builder()
                .duration(GameConfig.WORD_DASH_DURATION_MS)
                .tickInterval(1000)
                .listener(new GameTimer.Listener() {
                    @Override
                    public void onTick(long millisRemaining) {
                        gameStateManager.updateTimeRemaining(millisRemaining);
                    }

                    @Override
                    public void onFinish() {
                        endGame();
                    }
                })
                .build();
        gameTimer.start();
    }

    private void endGame() {
        gameStateManager.endGame();
        analytics.logGameEnd(GameType.WORD,
            gameStateManager.getScore().getValue(),
            (int)(GameConfig.WORD_DASH_DURATION_MS / 1000),
            true);
        showResults();
    }

    private void showResults() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(Constants.INTENT_SCORE,
                gameStateManager.getScore().getValue());
        intent.putExtra(Constants.INTENT_TIME,
                GameConfig.WORD_DASH_DURATION_MS / 1000);
        intent.putExtra(Constants.INTENT_TYPE,
                Constants.GAME_TYPE_WORD_DASH);
        intent.putExtra(Constants.INTENT_FOUND_WORDS,
                foundWordsList.size());
        startActivity(intent);
        finish();
    }

    private void handleBackspace(View v) {
        if (currentWord.length() > 0) {
            currentWord.deleteCharAt(currentWord.length() - 1);
            currentWordText.setText(currentWord.toString());
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    private void disableGameInteractions() {
        if (letterGrid != null) {
            letterGrid.setEnabled(false);
            for (int i = 0; i < letterGrid.getChildCount(); i++) {
                View child = letterGrid.getChildAt(i);
                child.setEnabled(false);
            }
        }
    }

    private void enableGameInteractions() {
        if (letterGrid != null) {
            letterGrid.setEnabled(true);
            for (int i = 0; i < letterGrid.getChildCount(); i++) {
                View child = letterGrid.getChildAt(i);
                child.setEnabled(true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameTimer != null) {
            gameTimer.stop();
            analytics.logGameEnd(GameType.WORD,
                gameStateManager.getScore().getValue(),
                (int)(GameConfig.WORD_DASH_DURATION_MS / 1000),
                false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDictionaryLoaded && gameTimer == null) {
            startGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
}
