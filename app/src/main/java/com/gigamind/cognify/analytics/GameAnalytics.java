package com.gigamind.cognify.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.gigamind.cognify.analytics.ResponseTimeBucket;

import java.util.HashMap;
import java.util.Map;
import java.util.EnumMap;

import com.gigamind.cognify.util.GameType;

public class GameAnalytics {
    private static GameAnalytics instance;
    private final FirebaseAnalytics firebaseAnalytics;
    private final FirebaseFirestore firestore;
    private final SharedPreferences prefs;
    private final String userId;
    private long sessionStartTime;
    private int sessionWordCount;
    private int sessionMathCount;
    private Map<Integer, Integer> wordLengthDistribution;
    private EnumMap<ResponseTimeBucket, Long> responseTimeDistribution;

    private GameAnalytics(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firestore = FirebaseService.getInstance().getFirestore();
        prefs = context.getSharedPreferences("GameAnalytics", Context.MODE_PRIVATE);
        
        FirebaseUser user = FirebaseService.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "guest_" + prefs.getString("guest_id", "unknown");
        
        wordLengthDistribution = new HashMap<>();
        responseTimeDistribution = new EnumMap<>(ResponseTimeBucket.class);
        sessionStartTime = System.currentTimeMillis();
    }

    public static synchronized GameAnalytics getInstance(Context context) {
        if (instance == null) {
            instance = new GameAnalytics(context.getApplicationContext());
        }
        return instance;
    }

    // Session Analytics
    public void startSession() {
        sessionStartTime = System.currentTimeMillis();
        sessionWordCount = 0;
        sessionMathCount = 0;
        
        Bundle bundle = new Bundle();
        bundle.putString("user_id", userId);
        firebaseAnalytics.logEvent("main_activity_start", bundle);
    }

    public void endSession() {
        long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        
        Bundle bundle = new Bundle();
        bundle.putString("user_id", userId);
        bundle.putLong("duration", sessionDuration);
        bundle.putInt("words_played", sessionWordCount);
        bundle.putInt("math_played", sessionMathCount);
        firebaseAnalytics.logEvent("session_end", bundle);

        // Store session data in Firestore
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("duration", sessionDuration);
        sessionData.put("words_played", sessionWordCount);
        sessionData.put("math_played", sessionMathCount);
        sessionData.put("timestamp", System.currentTimeMillis());
        
        firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .add(sessionData);
    }

    // Game Performance Analytics
    public void logGameStart(GameType gameType) {
        Bundle bundle = new Bundle();
        bundle.putString("game_type", gameType.id());
        firebaseAnalytics.logEvent("game_start", bundle);
    }

    public void logGameEnd(GameType gameType, int score, int duration, boolean completed) {
        Bundle bundle = new Bundle();
        bundle.putString("game_type", gameType.id());
        bundle.putInt("score", score);
        bundle.putInt("duration", duration);
        bundle.putBoolean("completed", completed);
        firebaseAnalytics.logEvent("game_end", bundle);

        if (gameType == GameType.WORD) {
            sessionWordCount++;
        } else if (gameType == GameType.MATH) {
            sessionMathCount++;
        }

        // Store game data in Firestore
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("game_type", gameType.id());
        gameData.put("score", score);
        gameData.put("duration", duration);
        gameData.put("completed", completed);
        gameData.put("timestamp", System.currentTimeMillis());

        firestore.collection("users")
                .document(userId)
                .collection("games")
                .add(gameData);
    }

    // Word Game Analytics
    public void logWordFound(String word, long timeSpent) {
        Bundle bundle = new Bundle();
        bundle.putString("word", word);
        bundle.putInt("length", word.length());
        bundle.putLong("time_spent", timeSpent);
        firebaseAnalytics.logEvent("word_found", bundle);

        // Track word length distribution
        int lengthKey = word.length();
        wordLengthDistribution.put(
            lengthKey,
            wordLengthDistribution.getOrDefault(lengthKey, 0) + 1
        );

        // Track response time distribution
        ResponseTimeBucket bucket = ResponseTimeBucket.fromTime(timeSpent);
        responseTimeDistribution.put(
            bucket,
            responseTimeDistribution.getOrDefault(bucket, 0L) + 1
        );
    }

    public void logInvalidWord(String word) {
        Bundle bundle = new Bundle();
        bundle.putString("word", word);
        firebaseAnalytics.logEvent("invalid_word", bundle);
    }

    // Math Game Analytics
    public void logMathAnswer(boolean correct, long timeSpent) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("correct", correct);
        bundle.putLong("time_spent", timeSpent);
        firebaseAnalytics.logEvent("math_answer", bundle);
    }

    // User Engagement Analytics
    public void logStreakUpdate(int newStreak) {
        Bundle bundle = new Bundle();
        bundle.putInt("streak", newStreak);
        firebaseAnalytics.logEvent("streak_update", bundle);

        // Update user profile in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("current_streak", newStreak);
        updates.put("last_played", System.currentTimeMillis());

        firestore.collection("users")
                .document(userId)
                .update(updates);
    }

    public void logDailyChallengeCompleted(String gameType, int score) {
        Bundle bundle = new Bundle();
        bundle.putString("game_type", gameType);
        bundle.putInt("score", score);
        firebaseAnalytics.logEvent("daily_challenge_completed", bundle);
    }

    // Navigation Analytics
    public void logScreenView(String screenName) {
        Bundle bundle = new Bundle();
        bundle.putString("screen_name", screenName);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    public void logButtonClick(String buttonName) {
        Bundle bundle = new Bundle();
        bundle.putString("button_name", buttonName);
        firebaseAnalytics.logEvent("button_click", bundle);
    }

    // Error Analytics
    public void logError(String errorType, String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString("error_type", errorType);
        bundle.putString("error_message", errorMessage);
        firebaseAnalytics.logEvent("app_error", bundle);
    }

    // Get Analytics Data
    public Map<Integer, Integer> getWordLengthDistribution() {
        return new HashMap<>(wordLengthDistribution);
    }

    public Map<ResponseTimeBucket, Long> getResponseTimeDistribution() {
        EnumMap<ResponseTimeBucket, Long> copy =
                new EnumMap<>(ResponseTimeBucket.class);
        copy.putAll(responseTimeDistribution);
        return copy;
    }
} 