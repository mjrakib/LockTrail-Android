# LockTrail Android MVP

LockTrail is an Android anti-theft app prototype focused on the first two requested screens:
- Local sign-in using email/phone and a password (demo only; no server account).
- Secure Your Phone: save a local app PIN/passphrase setting.
- Track Location On: request Android location permission and display the newest cached location available on the device.

## Important limitations

This is not yet a production anti-theft service. Sign-in and PIN storage are local demo implementations and must be replaced with secure authentication and credential storage before production. The location toggle is a local preference; this version does not continuously collect or upload location and cannot track the phone from another phone or PC. Remote tracking requires a secure backend, authenticated device registration, a web dashboard, and a properly permissioned background location service.

A regular Android application cannot universally block the phone from powering off, factory resetting, or entering recovery mode. Some managed-device policies require Android Enterprise Device Owner enrollment and device-specific support; they do not guarantee every shutdown/reset path can be blocked.

## Build APK

The GitHub Actions workflow at `.github/workflows/build-apk.yml` builds a debug APK on pushes to `main` or manual workflow dispatch. Open **Actions → Build Android APK** and download the `locktrail-debug-apk` artifact after a successful run.

Debug APKs are for testing, not production distribution. A production release requires proper signing, security review, backend setup, privacy disclosures, and end-to-end testing.

## Build requirements

- JDK 17
- Android SDK platform 35 and build-tools 35.0.0
- Gradle 8.9
