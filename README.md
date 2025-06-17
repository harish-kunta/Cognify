# Cognify 🧠

**Cognify** is an Android brain-training game built in Java. It features an engaging onboarding flow, daily challenges, streak tracking and Firebase-powered leaderboards.

## Features

- 🎉 **Animated Onboarding** – A splash screen and onboarding carousel introduce the game in seconds.
- 🔊 **Welcome Sound** – Hear a quick audio cue when you open the app.
- 🔥 **Daily Challenges** – Play "Word Dash" and "Quick Math" to keep your streak alive.
- 👑 **Leaderboards** – Compete globally when you sign in with Google.
- 🔔 **Notifications** – Never miss a session thanks to scheduled reminders.
- 💖 **Guest Mode** – Try the game instantly; sync progress later via Firebase.

<p align="center">
  <img src="app/src/main/res/drawable/brain_train.png" alt="Brain Train" width="100"/>
  <img src="app/src/main/res/drawable/rewards.png" alt="Rewards" width="100"/>
  <img src="app/src/main/res/drawable/ic_streak.png" alt="Streak" width="100"/>
</p>

## Getting Started

1. Clone this repository.
2. Place your `google-services.json` file in the `app/` directory for Firebase.
3. Build and run in Android Studio or execute:
   ```bash
   ./gradlew assembleDebug
   ```

## Folder Structure

```
app/
  src/main/java/...      # Application source code (Java)
  src/main/res/          # Layouts, drawables, strings
  google-services.json   # Firebase config (not included)
```

Cognify is designed to deliver quick, rewarding brain workouts with playful animations and polished UI elements.
