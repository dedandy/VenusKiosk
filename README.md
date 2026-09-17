# Venus Kiosk (Fire 7 5th gen / Android 5.1.1)

Minimal WebView kiosk for a local Victron Venus OS GUI.

## v0.2
- fullscreen immersive WebView
- landscape, keep screen on, Back consumed
- boot receiver
- default URL `http://192.168.1.107/gui-v1/`
- optional **fit console** mode enabled by default
  - finds the largest canvas used by the remote console
  - hides the HTML Hotkeys panel when detected
  - centers the console
  - scales it to use almost all available 1024x600 display area
- fit mode can be disabled from the hidden admin dialog if a future Venus update changes the page layout

## Hidden admin control
Hold the invisible top-left 72x72 px corner for ~3 seconds.
You can change the URL, enable/disable fit mode, or reload.

Later, set the capanno URL to:
`http://192.168.1.35/gui-v1/`

## Build
Open in Android Studio and Build APK(s), or run `gradle :app:assembleDebug` with Android SDK configured.

## Install / update

    adb install -r app/build/outputs/apk/debug/app-debug.apk
    adb shell am start -n local.venus.kiosk/.MainActivity

## Remove

    adb uninstall local.venus.kiosk

No Amazon packages are modified by this project.
