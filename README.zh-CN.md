# Content SDK 接入演示

[English](README.md) | 中文

几分钟跑起 Content SDK：把 SDK 包放进这个 demo，运行后就能看到 SDK 的短剧首页；再把代码里标好的步骤抄到你自己的应用即可。

本仓库不包含 SDK 本身，请向 SDK 提供方获取对应平台的 SDK 包。

## Demo 环境

Demo 已按下面的环境配置好，可直接运行。

| Android | |
|---|---|
| Android Gradle Plugin / Gradle | 9.3.0 / 9.5.0 |
| Kotlin | 2.3.10 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 24 |
| Gradle JDK | 25（本机没有时 Gradle 会自动下载） |

| iOS | |
|---|---|
| Xcode | 26.6 |
| CocoaPods | 1.17.0 |
| 部署版本 | iOS 16.0，iPhone |
| 运行设备 | 真机，或 Apple 芯片 Mac 上的模拟器 |

## 快速开始：Android

1. 把 Android SDK 包里的 `ContentSdk-<version>.aar` 复制到 `android/app/libs/`。
2. 把同一个包里的 `content-sdk-dependencies.gradle` 复制到 `android/app/`。
3. 用 Android Studio 打开 `android/`，运行 `app`。

## 快速开始：iOS

1. 把 iOS SDK 包里的全部内容复制到 `ios/ContentSdk/`。
2. 在 `ios/` 目录执行 `pod install`。
3. 打开 `ios/ContentSdkDemo.xcworkspace` 运行。

## 接入你自己的应用

代码中每个步骤都标有 `Content SDK - integration Step N`，按顺序抄过去：

| 步骤 | Android | iOS |
|---|---|---|
| 1 | `settings.gradle.kts`：依赖仓库 | `Podfile`：SDK Pod |
| 2 | `app/build.gradle.kts`：AAR 和依赖 | `Info.plist`：AdMob Application ID |
| 3 | `AndroidManifest.xml`：AdMob Application ID | `ContentSdkDemoApp.swift`：初始化 |
| 4 | `DemoApplication.kt`：初始化 | `SdkIntegration.swift`：广告同意 + 启动 |
| 5 | `MainActivity.kt`：广告同意 + 启动 | `SdkIntegration.swift`：打开 SDK 页面 |
| 6 | `MainActivity.kt`：打开 SDK 页面 | |

然后换成你自己的值：

- 你的 App ID / App Secret：在 [https://open.lucky-chick.com](https://open.lucky-chick.com) 获取，填到 `DemoApplication.kt` / `ContentSdkDemoApp.swift`。demo 里填的是演示用的值。
- 你的 AdMob Application ID（`AndroidManifest.xml` / `Info.plist`）；demo 用的是 Google 测试 ID。
- iOS：把 SDK 包里的 `SKAdNetworkItems.plist` 合并到你的 `Info.plist`。

其他调用都是可选的，用 SDK 默认值即可。`SdkApiCatalog.kt` / `SdkApiCatalog.swift` 列出了全部公开 API，并附可直接复制的示例。`DemoHomeScreen.kt` / `DemoHomeView.swift` 只是演示界面。

## 日志

Debug 构建会通过 `setLoggingEnabled` 打开 SDK 调试日志，用于查看初始化和页面加载状态；正式包请保持关闭。

- Android：在 Logcat 中按 `ContentSdk` 筛选。
- iOS：在 Xcode 控制台中按 `ContentSdk` 筛选。
