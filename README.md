# Android-Screen-Clicker

Rapid fire tap app – a simple Android auto clicker that keeps tapping one
point of the screen at a chosen rate.

Written in plain Java with only the Android SDK (no third‑party runtime
libraries). Taps are performed by an **Accessibility Service** using
`dispatchGesture`, so **no root is required**.

## Features

- Select the tap point:
  - by typing X / Y coordinates, or
  - by dragging a floating target (blue circle) over any app.
- Screen profiles – enter coordinates for a generic Android or iOS screen and
  they are scaled proportionally to the real screen of your device:
  - This device (native pixels, no scaling)
  - Generic Android HD 720×1600, FHD+ 1080×2400, QHD+ 1440×3200
  - iPhone SE 750×1334, iPhone 15/16 1179×2556, iPhone 15/16 Pro Max 1290×2796
  - iPad 10th gen 1640×2360
  - Percent of screen (100×100 – e.g. `50, 50` is the center)
- Tap rate in taps per second (0.1 – 20).
- Floating panel with **Start / Stop**, **Close** and a tap counter.

## Requirements

- Android 7.0 (API 24) or newer.
- To build: JDK 17 and the Android SDK (compileSdk 35). Android Studio works
  out of the box.

## Build & install

```bash
./gradlew assembleDebug          # builds app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # installs on a connected device / emulator
./gradlew test                   # runs unit tests
```

Or open the project in Android Studio and press **Run**.

## Usage

1. Open **Screen Clicker** and tap **Open Accessibility settings**.
   Enable the **Screen Clicker** service (on Android 13+ you may first need to
   allow "restricted settings" in the app info screen when side‑loading).
2. Back in the app choose the **screen profile** your coordinates come from.
   Changing the profile converts the current point, so it stays on the same spot.
3. Enter **X / Y** and the **tap rate** (taps per second).
4. Press **Save & show floating controls**. The app goes to the background and
   a blue target plus a small control panel appear.
5. Open the app you want to tap. If needed, drag the blue target onto the
   exact spot (its center is the tap point) – the new point is saved
   automatically. The panel can be moved by dragging its title.
6. Press **▶ Start**. The target turns red and taps continuously.
   Press **■ Stop** to stop, **Close** to remove the floating controls.

## How coordinates are scaled

For a profile of size `Pw × Ph` and a device screen of `Dw × Dh` pixels:

```
deviceX = x * Dw / Pw
deviceY = y * Dh / Ph
```

If the device is in landscape the profile width/height are swapped. Points
outside the screen are clamped to the screen edge.

## Project structure

```
app/src/main/java/io/github/wojtek0126/screenclicker/
├── MainActivity.java     settings screen (profile, X/Y, rate)
├── ClickerService.java   accessibility service: overlay + tapping loop
├── ClickerSettings.java  SharedPreferences storage, screen size
├── ScreenProfile.java    screen presets + coordinate scaling
├── TapRate.java          taps/second -> delay conversion
└── CrosshairView.java    floating target view
app/src/test/...          JUnit tests for ScreenProfile and TapRate
```

## Notes

- The service does not read screen content; it only injects taps.
- Some apps/games may ignore injected gestures, and system screens (e.g. the
  lock screen or settings permission dialogs) may block them.
