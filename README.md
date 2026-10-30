# LockTrail Android prototype

This Android app is an early prototype for phone security and location features.

## Current prototype features
- Local demo sign-in (not a real cloud account).
- Shortcut to Android Security settings so the owner can configure the actual system screen lock PIN/password.
- Optional LockTrail-only PIN (does not replace Android's system lock screen).
- Foreground location logging while enabled, with a persistent notification.
- View the latest location recorded by the service on this same phone.

## Important limitations
- Android's system screen lock is what protects access after startup/reboot. A normal third-party app cannot display its own PIN screen before Android starts, cannot guarantee a PIN prompt on the power-off menu, and cannot universally prevent shutdown or factory reset. The user must configure Android's own screen lock in system settings.
- Location points are currently saved locally on the phone. This build does NOT upload location to a server and cannot show the phone on another phone or PC. Remote tracking requires a secure backend, authenticated device registration, and a separate map/dashboard.
- Location updates depend on Android location being enabled, permissions, network/GPS availability, and manufacturer battery/background restrictions.
- Demo sign-in and app-PIN storage are not production-grade authentication/credential storage. Do not use this prototype as the only protection for a sensitive device.

## Build APK
GitHub Actions builds a debug APK on pushes to main or manual workflow dispatch. Open Actions → Build Android APK and download the locktrail-debug-apk artifact after a successful run.

Debug APKs are for testing only. Production use requires secure authentication, backend setup, privacy disclosures, release signing, and real-device testing.

## Build requirements
- JDK 17
- Android SDK platform 35 and build-tools 35.0.0
- Gradle 8.9