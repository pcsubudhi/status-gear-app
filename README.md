# Status Gear - Mobile App (Android)

A mobile app for Status Gear construction management system.

## How to Build APK

### Option 1: GitHub Actions (Easiest - No Android Studio needed)

1. Push this repo to GitHub
2. Go to **Actions** tab → **Build Android APK** → **Run workflow**
3. Wait ~3 minutes for build
4. Download the APK from **Artifacts** section

### Option 2: Build Locally

**Requirements:** Node.js 18+, Android Studio, JDK 17

```bash
npm install
npx cap sync android
cd android
./gradlew assembleDebug
```

APK will be at: `android/app/build/outputs/apk/debug/app-debug.apk`

## How it Works

The app loads from `https://sgapp.steplit.com/steplit-mobile-app.html` — so any updates you make to the HTML file on the server are instantly available in the app without rebuilding the APK.

## App Details

- **Package:** com.steplit.statusgear
- **App Name:** Status Gear
- **API:** https://soft.steplit.com/api/mobile
- **Auth:** JWT tokens stored in localStorage
