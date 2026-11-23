# Screen Clicker

A simple Android auto-tap (auto-clicker) app. Pick a point on the screen with a draggable
crosshair and the app taps it automatically at a repeat rate you choose.

## Features

- 🎯 **Draggable crosshair overlay** – place it exactly where you want the taps to land
- ⏱️ **Configurable repeat rate** – set the interval between taps in milliseconds (minimum 50 ms)
- ▶️ **Floating control panel** – start/stop tapping and close the overlay from anywhere, over any app
- 🔒 **Privacy friendly** – no network access, no screen-content reading, zero third-party dependencies

## How it works

The app uses an Android **AccessibilityService** with `dispatchGesture()` to inject taps at the
chosen screen coordinates. The crosshair and control panel are `TYPE_ACCESSIBILITY_OVERLAY`
windows, so no extra "draw over other apps" permission is needed. While tapping is active the
crosshair becomes untouchable and semi-transparent so the injected taps pass through to the app
underneath.

## Usage

1. Install and open the app.
2. Set the repeat interval (e.g. `1000` = one tap per second) and press **Save interval**.
3. Press **Open Accessibility settings** and enable the **Screen Clicker** service.
4. Go back and press **Show / hide target**.
5. Drag the red crosshair to the point you want tapped.
6. Press **Start** on the floating panel. Press **Stop** to pause, or **✕** to close the overlay.

## Download / Build

- **Download**: every push to `main` builds a debug APK via GitHub Actions – grab the
  `screen-clicker-debug-apk` artifact from the latest [Actions run](../../actions).
- **Build locally**: requires JDK 17 and the Android SDK.

  ```bash
  ./gradlew assembleDebug
  # APK: app/build/outputs/apk/debug/app-debug.apk
  ```

Minimum Android version: **7.0 (API 24)** – `dispatchGesture()` requires API 24.

## Why no iOS version?

iOS does not allow third-party apps to simulate touches in other apps or system-wide – the
sandbox has no equivalent of Android's AccessibilityService gesture dispatch, and such apps are
not permitted on the App Store. On iOS the built-in **Switch Control** feature
(Settings → Accessibility → Switch Control → Recipes) can be used to create a tap-repeating
recipe without any extra app.

## Disclaimer

Automating taps may violate the terms of service of some apps and games. Use responsibly.
