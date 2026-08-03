# KSTV Launcher

KSTV Launcher is a lightweight Android TV Home application built for Android TV 11 and remote-control navigation. Stage 1 establishes the production foundation: Home intent handling, discovery of launchable applications, explicit launches, and deterministic D-pad focus.

## Requirements

- Android Studio with JDK 17
- Android SDK 35
- Android TV device running API 30 or newer

## Build

Open the project in Android Studio and run the `app` configuration, or use Gradle 8.9:

```bash
gradle testDebugUnitTest assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Stage 1 device test

1. Copy `app-debug.apk` to a USB drive and install it on the TV box.
2. When Android asks for a Home application, choose **KSTV Launcher** and select **Always**.
3. Confirm that the first application tile receives focus.
4. Navigate left, right, up, and down with the D-pad.
5. Open three different applications with the center/OK button.
6. Press Home after each launch and confirm that KSTV Launcher returns.
7. Disconnect power for 15 seconds, reconnect it, and confirm that KSTV Launcher remains Home.

Do not perform a factory reset. A factory reset removes every user-installed launcher.
