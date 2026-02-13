# Tarneeb Game - Complete Android Application

## Project Structure

```
tarneeb-game/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tarneeb/game/
│   │   │   │   ├── TarneebApplication.kt
│   │   │   │   │
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Typography.kt
│   │   │   │   │   │
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── MainScreen.kt
│   │   │   │   │   │   ├── GameScreen.kt
│   │   │   │   │   │   ├── BiddingScreen.kt
│   │   │   │   │   │   ├── GamePlayScreen.kt
│   │   │   │   │   │   └── ScoreboardScreen.kt
│   │   │   │   │   │
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── CardComponent.kt
│   │   │   │   │   │   ├── PlayerCard.kt
│   │   │   │   │   │   ├── TrickDisplay.kt
│   │   │   │   │   │   ├── BidButton.kt
│   │   │   │   │   │   └── Scoreboard.kt
│   │   │   │   │   │
│   │   │   │   │   └── dialogs/
│   │   │   │   │       ├── GameRulesDialog.kt
│   │   │   │   │       ├── RoundResultDialog.kt
│   │   │   │   │       └── GameOverDialog.kt
│   │   │   │   │
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── GameViewModel.kt
│   │   │   │   │   └── ScoreViewModel.kt
│   │   │   │   │
│   │   │   │   ├── model/
│   │   │   │   │   ├── Card.kt
│   │   │   │   │   ├── Player.kt
│   │   │   │   │   ├── Game.kt
│   │   │   │   │   ├── Team.kt
│   │   │   │   │   ├── Trick.kt
│   │   │   │   │   └── GameState.kt
│   │   │   │   │
│   │   │   │   ├── engine/
│   │   │   │   │   ├── GameEngine.kt
│   │   │   │   │   ├── CardRulesEngine.kt
│   │   │   │   │   ├── ScoringEngine.kt
│   │   │   │   │   ├── BiddingEngine.kt
│   │   │   │   │   └── AIPlayer.kt
│   │   │   │   │
│   │   │   │   ├── utils/
│   │   │   │   │   ├── CardUtils.kt
│   │   │   │   │   ├── GameConstants.kt
│   │   │   │   │   └── Extensions.kt
│   │   │   │   │
│   │   │   │   └── MainActivity.kt
│   │   │   │
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   └── colors.xml
│   │   │       │
│   │   │       └── drawable/
│   │   │           └── card_backgrounds/
│   │   │
│   │   └── test/
│   │       └── java/com/tarneeb/game/
│   │           ├── GameEngineTest.kt
│   │           ├── ScoringEngineTest.kt
│   │           └── CardRulesEngineTest.kt
│   │
│   ├── build.gradle.kts
│   └── AndroidManifest.xml
│
└── build.gradle.kts
```

## Dependencies (build.gradle.kts)

- Jetpack Compose
- Material 3
- Lifecycle & ViewModel
- Kotlin Coroutines
- JUnit & Testing
