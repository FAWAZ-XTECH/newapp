# M9999 Demo Play (Android)

**M9999 Demo Play** is a free-to-play Android gaming application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Firebase Auth & Cloud Firestore**, and **Room + DataStore**.

> **Free-to-Play Disclaimer**: This application is strictly a free-to-play entertainment demo. All coins, virtual points, and mini-game outcomes have **no monetary value**. No real-money gambling, deposits, withdrawals, cash prizes, or payment gateways are supported.

---

## Core Features

1. **Dual-Mode Authentication & Persistence**
   - **Firebase Cloud Mode**: Google Sign-In via Jetpack `CredentialManager` (`GetSignInWithGoogleOption`) backed by Cloud Firestore with strict user-scoped and admin-verified security rules (`firestore.rules`).
   - **Local Demo Mode**: Isolated on-device Room (`m9999_demo_local.db`) + DataStore storage with PBKDF2-HMAC-SHA256 salted password hashing supporting Sign Up, Login, Forgot/Reset Password, Instant Guest Demo, and Session Persistence (clearly labeled as on-device demo storage).
2. **6 Playable Animated Mini-Games** (powered by `java.security.SecureRandom`)
   - **Lucky Wheel**: Animated prize wheel with daily free spin limits and result history.
   - **Golden Slots**: Triple-reel spinning symbol animation with virtual coin payouts.
   - **Royal Dice**: Twin animated dice roll with Under 7, Lucky 7, Over 7, and Doubles predictions.
   - **Card Reveal**: Flip-animated 3-card draw revealing multipliers and hidden rewards.
   - **Lucky Number**: Number selector (1–10) with animated draw reveal and match payouts.
   - **Memory Match**: Interactive card-matching puzzle with 3 difficulty tiers, move tracking, and timer.
3. **Virtual Wallet & Daily Rewards**
   - Live virtual coin balance, transaction ledger, daily login streak ladder, 24-hour UTC anti-duplicate claim protection, and live UTC countdown timer.
4. **Social & Engagement**
   - **Leaderboard**: Weekly, Monthly, and All-Time rankings (sample demo accounts are explicitly labeled).
   - **Notification Center**: Categorized alerts with unread counters, individual read toggles, and mark-all-as-read.
   - **Profile & Info Center**: Avatar selection, profile editing, game statistics, settings toggles, Help & Support, About Us, Privacy Policy, and Terms & Conditions.
5. **Administrator Dashboard**
   - **Cloud Mode**: Enforced server-side by `isAdmin()` in `firestore.rules` (owner email verification or `admins/{uid}` document).
   - **Local Demo Mode**: Protected by a first-run claimed salted PBKDF2 admin passcode.
   - Manage user statuses (enable/disable), grant bonus virtual coins, toggle game availability, configure daily bonus/wheel limits, publish/delete announcements, and audit activity logs.

---

## Project Structure

```text
app/src/main/java/com/example/
├── MainActivity.kt
├── data/
│   ├── firebase/        # AuthHelper (CredentialManager), FirestoreRepository, ErrorHandler
│   ├── local/           # Room entities/DAOs/database, PasswordHasher, DataStore, LocalDemoRepository
│   ├── model/           # Domain models & Firestore DTOs
│   └── repository/      # SecureRng & M9999Repository coordinator
└── ui/
    ├── components/      # Luxury obsidian & gold Compose components
    ├── navigation/      # Adaptive NavigationBar / NavigationRail & NavHost routes
    ├── screens/
    │   ├── auth/        # Welcome, Google Sign-In, Local Demo Sign Up/Login/Reset
    │   ├── main/        # Home Dashboard, Games Hub, Daily Rewards, Leaderboard, Profile
    │   ├── games/       # 6 playable mini-games
    │   └── secondary/   # Wallet, Notifications, History, Settings, Info pages, Admin Dashboard
    ├── theme/           # Color, Typography (Cinzel, Plus Jakarta Sans, JetBrains Mono), Theme
    └── viewmodel/       # M9999ViewModel & DataState management
```

---

## Build, Firebase Setup & APK Generation

### 1. Firebase Configuration
- `app/google-services.json` and `app/src/main/res/values/firebase_applet_config.xml` configure the Firebase project and named Firestore database (`firestore_database_id`).
- Firestore schema blueprint and security rules live in `/firebase-blueprint.json` and `/firestore.rules`.
- Emulator security rule verification tests are in `/firestore.test.js` (`FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js`).

### 2. Local Build & Testing
```bash
# Compile debug build
gradle :app:compileDebugKotlin

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest
```

### 3. Generate APK / Android App Bundle (AAB)
```bash
# Debug APK (outputs to app/build/outputs/apk/debug/app-debug.apk)
gradle :app:assembleDebug

# Release AAB (requires KEYSTORE_PATH, STORE_PASSWORD, KEY_PASSWORD env vars)
gradle :app:bundleRelease
```
