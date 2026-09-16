# Venus Kiosk (Fire 7 5th gen / Android 5.1.1)

Minimal WebView kiosk for a local Victron Venus OS GUI.

## Defaults
- minSdk: 22 (Android 5.1)
- targetSdk: 22
- orientation: landscape
- default URL: http://192.168.1.107/gui-v1/
- fullscreen immersive
- keep screen on
- consumes Back
- attempts to start on BOOT_COMPLETED

## Hidden admin control
Hold the invisible top-left 72x72 px corner for ~3 seconds.
A small dialog lets you change the URL or reload the page.

Later, set the capanno URL to:
http://192.168.1.35/gui-v1/

## Build
Open the directory in Android Studio and build an APK, or from a machine with Android SDK + Gradle:

    gradle :app:assembleDebug

Result is normally:

    app/build/outputs/apk/debug/app-debug.apk

## Install with ADB

    adb install -r app-debug.apk
    adb shell am start -n local.venus.kiosk/.MainActivity

## Remove

    adb uninstall local.venus.kiosk

No Amazon packages are modified by this project.
