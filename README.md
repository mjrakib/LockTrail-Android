# LockTrail Android Prototype

This is a starter Android project for the Anti-Theft Mobile Security concept.

## Current functionality
- Native Android screen for security status
- Requests location permission and reads the device's last-known cached location locally
- Local alarm-tone test
- Explains unsupported protections in the UI

## Not implemented yet
- User login, 2FA, backend, or web dashboard
- Live/background location upload
- Remote tracking, remote alarm, or remote lock
- Device Owner / Android Enterprise enrollment
- Power-off blocking, Factory Reset blocking, Recovery Mode protection, or remote erase

A regular Android application cannot universally block shutdown or recovery reset. Stronger device policies require compatible, properly enrolled managed devices, and even those do not guarantee all shutdown paths can be blocked.

## Build an APK

The repository includes `.github/workflows/build-apk.yml`. Push this project to a GitHub repository, then open **Actions → Build Android APK → Run workflow**. When the workflow completes, download the `locktrail-debug-apk` artifact. This produces a debug APK, not a production-signed release.

## Local build requirements
- JDK 17
- Android SDK platform 35 and build-tools 35.0.0
- Gradle 8.9

Then run:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.
