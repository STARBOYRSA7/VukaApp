# Vuka (Android wrapper)

This is the Vuka HTML prototype wrapped in a minimal Android app so it runs
as a real, installable app while the full native Kotlin build is still in
progress. It loads app/src/main/assets/vuka.html inside a WebView.

## How to open it

1. Unzip this folder.
2. Open Android Studio, choose "Open", and select the unzipped VukaAndroid folder.
3. Android Studio will detect this project has no gradle-wrapper.jar (it
   couldn't be generated outside of Android Studio without a live download).
   When it offers to fix or regenerate the Gradle wrapper, accept it, this
   is a one-click, automatic step in Android Studio and only needs to
   happen once.
4. Let Gradle sync finish, then press Run.

## What's in here

- app/src/main/assets/vuka.html, the actual Vuka prototype, unchanged.
- MainActivity.kt, a WebView that loads that file, enables JavaScript, and
  supports the CV upload fields (file chooser) and the Android back button.
- Launcher icons, generated from Vuka's own logomark, wired up as an
  adaptive icon so it looks correct on modern Android launchers.
- Internet permission, needed since the app pulls in Tailwind, Google
  Fonts, and live job data from the Adzuna API, same as it does in a
  browser.

## Known limits

This is a WebView wrapper around the HTML prototype, not the native
Jetpack Compose app. It's meant to get you a real, submittable APK today.
Everything the HTML prototype can't really do yet (no backend, no stored
accounts, no real CV parsing) still can't do it here either, wrapping it
in Android doesn't add functionality, it just makes it installable.
