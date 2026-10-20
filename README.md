# Content SDK Integration Demo

English | [中文](README.zh-CN.md)

Get Content SDK running in a few minutes: put the SDK package into this demo, run it, and you'll see the SDK's drama home page. Then copy the marked steps into your own app.

This repository does not include the SDK itself; get the SDK package for your platform from the SDK provider.

## Demo environment

The demo is ready to run with the setup below.

| Android | |
|---|---|
| Android Gradle Plugin / Gradle | 9.3.0 / 9.5.0 |
| Kotlin | 2.3.10 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 24 |
| Gradle JDK | 25 (downloaded by Gradle if missing) |

| iOS | |
|---|---|
| Xcode | 26.6 |
| CocoaPods | 1.17.0 |
| Deployment target | iOS 16.0, iPhone |
| Run on | Device, or an Apple-silicon Mac simulator |

## Quick start: Android

1. Copy `ContentSdk-<version>.aar` from the Android package into `android/app/libs/`.
2. Copy `content-sdk-dependencies.gradle` from the same package into `android/app/`.
3. Open `android/` in Android Studio and run `app`.

## Quick start: iOS

1. Copy everything in the iOS package into `ios/ContentSdk/`.
2. Run `pod install` in `ios/`.
3. Open `ios/ContentSdkDemo.xcworkspace` and run.

## Integrate into your app

Each step is marked `Content SDK - integration Step N` in the code. Copy them in order:

| Step | Android | iOS |
|---|---|---|
| 1 | `settings.gradle.kts`: repositories | `Podfile`: SDK Pod |
| 2 | `app/build.gradle.kts`: AAR and dependencies | `Info.plist`: AdMob Application ID |
| 3 | `AndroidManifest.xml`: AdMob Application ID | `ContentSdkDemoApp.swift`: initialize |
| 4 | `DemoApplication.kt`: initialize | `SdkIntegration.swift`: ad consent + start |
| 5 | `MainActivity.kt`: ad consent + start | `SdkIntegration.swift`: open the SDK page |
| 6 | `MainActivity.kt`: open the SDK page | |

Then fill in your own values:

- Your App ID / App Secret: get them at [https://open.lucky-chick.com](https://open.lucky-chick.com) and fill them in `DemoApplication.kt` / `ContentSdkDemoApp.swift`. The values in the demo are for demonstration only.
- Your AdMob Application ID (`AndroidManifest.xml` / `Info.plist`); the demo uses Google's test ID.
- iOS: merge `SKAdNetworkItems.plist` from the SDK package into your `Info.plist`.

Everything else is optional and works with SDK defaults. `SdkApiCatalog.kt` / `SdkApiCatalog.swift` list every public API with a ready-to-copy example. `DemoHomeScreen.kt` / `DemoHomeView.swift` are demo UI only.

## Logs

Debug builds turn on SDK diagnostic logging with `setLoggingEnabled` so you can follow initialization and page loading. Keep it off in release builds.

- Android: filter Logcat by `ContentSdk`.
- iOS: filter the Xcode console by `ContentSdk`.
