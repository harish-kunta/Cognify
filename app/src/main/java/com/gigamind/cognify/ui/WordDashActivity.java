package com.gigamind.cognify.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.gigamind.cognify.util.SoundManager;
import com.google.android.material.snackbar.Snackbar;

import com.gigamind.cognify.ui.BaseActivity;
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
import com.gigamind.cognify.ui.TutorialOverlay;
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
public class WordDashActivity extends BaseActivity {
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
    private TutorialOverlay tutorialOverlay;
    private MaterialButton submitButton;
    private MaterialButton clearButton;
    private MaterialButton backspaceButton;
    private boolean hapticsEnabled = true;
    private View loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_dash);

        int challengeScore = getIntent().getIntExtra(Constants.EXTRA_CHALLENGE_SCORE, -1);
        if (challengeScore >= 0) {
            String msg = getString(R.string.challenge_toast, challengeScore);
            View root = findViewById(android.R.id.content);
            Snackbar.make(root, msg, Snackbar.LENGTH_LONG).show();
            root.announceForAccessibility(msg);
        }

        analytics = GameAnalytics.getInstance(this);
        analytics.logScreenView(Constants.ANALYTICS_SCREEN_WORD_DASH);
        analytics.logGameStart(GameType.WORD);

        tutorialHelper = new TutorialHelper(this);

        SharedPreferences prefs = getSharedPreferences(Constants.PREF_APP, MODE_PRIVATE);
        hapticsEnabled = prefs.getBoolean(Constants.PREF_HAPTICS_ENABLED, true);
        
        // 1) Bind all views and set up UI scaffolding that does NOT use gameEngine yet
        initializeViews();
        setupButtons();
        observeGameState();
        setupFoundWordsRecycler();

        disableGameInteractions();

        loadingIndicator.setVisibility(View.VISIBLE);
        View root = findViewById(android.R.id.content);
        root.announceForAccessibility(getString(R.string.loading_dictionary));

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
                        loadingIndicator.setVisibility(View.GONE);
                        findViewById(android.R.id.content)
                                .announceForAccessibility(getString(R.string.dictionary_loaded));
                        isDictionaryLoaded = true;
                        if (!tutorialHelper.isTutorialCompleted()) {
                            tutorialActive = true;
                            tutorialOverlay = new TutorialOverlay(this);
                            tutorialOverlay.addStep(letterGrid, getString(R.string.tutorial_step_letters));
                            tutorialOverlay.addStep(currentWordText, getString(R.string.tutorial_step_current));
                            tutorialOverlay.addStep(backspaceButton, getString(R.string.tutorial_step_back));
                            tutorialOverlay.addStep(clearButton, getString(R.string.tutorial_step_clear));
                            tutorialOverlay.addStep(submitButton, getString(R.string.tutorial_step_submit));
                            tutorialOverlay.addStep(foundWordsRecycler, getString(R.string.tutorial_step_history));
                            tutorialOverlay.addStep(scoreText, getString(R.string.tutorial_step_score));
                            tutorialOverlay.addStep(timerText, getString(R.string.tutorial_step_timer));
                            tutorialOverlay.setOnComplete(() -> {
                                String msg = getString(R.string.tutorial_complete);
                                Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
                                root.announceForAccessibility(msg);
                                tutorialHelper.markTutorialCompleted();
                                tutorialActive = false;
                                startGame();
                            });
                            letterGrid.post(tutorialOverlay::start);
                        } else {
                            startGame();       // Begin the game timer immediately
                        }
                    } else {
                        // Handle dictionary load error
                        String msg = "Error loading game dictionary";
                        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
                        findViewById(android.R.id.content).announceForAccessibility(msg);
                        loadingIndicator.setVisibility(View.GONE);
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
        loadingIndicator = findViewById(R.id.loadingIndicator);
    }

    private void setupButtons() {
        submitButton = findViewById(R.id.submitButton);
        clearButton = findViewById(R.id.clearButton);
        backspaceButton = findViewById(R.id.backspaceButton);

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
                if (hapticsEnabled) {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                }
                animateButtonPress(button);
                onLetterClick(letters[index]);
            });

            letterGrid.addView(button);
        }
    }

    private void animateButtonPress(MaterialButton button) {
        com.gigamind.cognify.animation.AnimationUtils.pulse(
                button,
                1.2f,
                GameConfig.BUTTON_SCALE_DURATION_MS
        );
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
            SoundManager.getInstance(this).playCorrect();
            animateScoreIncrease();

            foundWordsList.add(word);
            foundWordsAdapter.submitList(new ArrayList<>(foundWordsList));
            if (tutorialActive) {
                String msg = getString(R.string.tutorial_complete);
                View root = findViewById(android.R.id.content);
                Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
                root.announceForAccessibility(msg);
                tutorialHelper.markTutorialCompleted();
                tutorialActive = false;
            }
        } else {
            showError(getString(R.string.invalid_word));
            analytics.logInvalidWord(word);
            if (hapticsEnabled) {
                scoreText.performHapticFeedback(HapticFeedbackConstants.REJECT);
            }
            com.gigamind.cognify.animation.AnimationUtils.shake(letterGrid);
        }
        clearWord();
        wordStartTime = 0;
    }

    private void animateScoreIncrease() {
        if (hapticsEnabled) {
            scoreText.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        com.gigamind.cognify.animation.AnimationUtils.pulse(
                scoreText,
                1.3f,
                GameConfig.SCORE_ANIMATION_DURATION_MS
        );
    }

    private void showError(String message) {
        View root = findViewById(android.R.id.content);
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
        root.announceForAccessibility(message);
        letterGrid.performHapticFeedback(HapticFeedbackConstants.REJECT);
        if (hapticsEnabled) {
            letterGrid.performHapticFeedback(HapticFeedbackConstants.REJECT);
        }
        SoundManager.getInstance(this).playIncorrect();
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
                        if (millisRemaining <= GameConfig.FINAL_COUNTDOWN_MS) {
                            triggerFinalCountdown();
                        }
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
            if (hapticsEnabled) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
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
        if (isDictionaryLoaded && gameTimer == null && !tutorialActive) {
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

    private void triggerFinalCountdown() {
        SoundManager.getInstance(this).playHeartbeat();
        com.gigamind.cognify.animation.AnimationUtils.shake(timerText, 8f);
    }
}
