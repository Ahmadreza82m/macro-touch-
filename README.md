# MacroTouch

MacroTouch is an Android 8+ prototype for testing multiple draggable floating control buttons.

## Current prototype
- Adds multiple independent floating buttons over other apps.
- Each button can be dragged to a different screen position.
- A tap only gives a visual confirmation; it does not inject touches into other apps or automate aiming/gameplay.
- Built with GitHub Actions; the debug APK is uploaded as a workflow artifact.

## Build
Open the **Actions** tab and run **Build MacroTouch APK**, or push a commit to the default branch. Download the artifact named `MacroTouch-debug-apk`.

Package: `com.ahmadreza.macrotouch`
Version: `0.1.0`
