import SwiftUI
import UIKit
import ContentSdk

/*
 =================================================================================================
  Content SDK - API catalog (iOS)
 =================================================================================================

  Every public call of `IosContentSdk.shared`, what it is for, and where to find an example.
  The main flow (Steps 1-5) is already wired in this project; everything marked "optional" is
  shown in this file only. Pick what you need and copy it into your app.
  The functions here compile but are never called by the demo.

  Rules that apply to every call:
  - Callbacks and listeners are always delivered on the MAIN thread.
  - `initialize` once at launch; `start` must succeed before any SDK page, `login` or `getDramaList`.
  - Errors are `SdkException` with `code` and `msg`.

  | API                                        | Required | When to call                        | Section |
  |--------------------------------------------|----------|-------------------------------------|---------|
  | initialize(appId:appSecret:userId:language:)| yes     | app launch, once                    | Step 3  |
  | showAdConsentIfRequired(viewController:)   | optional | before start; skip if you run UMP   | Step 4  |
  | start(onComplete:)                         | yes      | after consent, once per launch      | Step 4  |
  | dramaHome(title:insetsMode:onDramaClick:)  | yes *    | after start succeeded               | Step 5  |
  | setLoggingEnabled(enabled:)                | optional | any time, usually before initialize | 1       |
  | setStrategy(strategy:)                     | optional | before start                        | 2       |
  | setColorPalette(colors:) / resetColorPalette() | optional | any time; also applies to open pages | 3 |
  | setSubtitleStyle(style:) / resetSubtitleStyle() | optional | any time; overrides subtitle files | 3 |
  | dramaHome(onDramaClick:) variants          | optional | other title / inset combinations    | 4.1     |
  | dramaPlayer(params:onCloseRequest:)        | optional | open one drama directly             | 4.2     |
  | UIKit navigation                           | optional | your app uses UINavigationController| 4.3     |
  | Embedded in your layout (tab)              | optional | SDK page as one tab                 | 4.4     |
  | login(userId:onComplete:)                  | optional | user logs in / switches / logs out  | 5.1     |
  | setLanguage(language:onComplete:)          | optional | your app language changed           | 5.2     |
  | setPlayerEventListener(listener:)          | optional | playback events                     | 6.1     |
  | setAdEventListener(listener:)              | optional | ad events                           | 6.2     |
  | getDramaList(page:pageSize:...)            | optional | build your own drama entry UI       | 7       |

  * Step 5 or any of 4.1 - 4.4 is enough to show SDK content.
 =================================================================================================
 */
enum SdkApiCatalog {

    // MARK: - 1. Logging

    /// Prints SDK status lines to the Xcode console, e.g. `I/[ContentSdk][Lifecycle] start_succeeded`.
    /// Secrets and URLs are never printed. Default: off.
    ///
    /// Usually inside `#if DEBUG`, before `initialize` (see ContentSdkDemoApp.swift).
    /// Attach these lines when you report an issue.
    static func enableLogs(_ enabled: Bool) {
        IosContentSdk.shared.setLoggingEnabled(enabled: enabled)
    }

    // MARK: - 2. Resource strategy

    /// Chooses how much memory, disk and network the SDK may use. Call BEFORE `start`.
    ///
    /// - `.adaptive` (default, recommended): adjusts to the device automatically. No call needed.
    /// - `.standard`: fixed, balanced for mainstream devices.
    /// - `.lowPerformance`: fixed, lowest memory / disk / data usage.
    /// - `.highPerformance`: fixed, fastest episode switching on high-end devices.
    static func chooseStrategy() {
        IosContentSdk.shared.setStrategy(strategy: ContentSdkStrategy.adaptive)
    }

    // MARK: - 3. Theme colors

    /// Switches SDK pages to the built-in dark palette; also applies to SDK pages already open.
    /// Built-in palettes: `Colors.shared.light` (default) and `Colors.shared.dark`.
    static func useDarkTheme() {
        IosContentSdk.shared.setColorPalette(colors: Colors.shared.dark)
    }

    /// Back to the default light palette.
    static func useDefaultTheme() {
        IosContentSdk.shared.resetColorPalette()
    }

    /// Subtitle appearance for all SDK players. Each field you set wins over the subtitle file;
    /// unset fields keep the file's styling or the SDK default. Applies to subtitles already on screen.
    /// Fields: fontScale (0.5...2.0), textColor, backgroundColor, outlineColor, bold, bottomMargin (0...0.4 of the player height).
    static func useLargeSubtitles() {
        let style = SubtitleStyle(
            fontScale: 1.2,
            textColor: Colors.shared.argb(argb: 0xFFFFFF00),
            // Colors.shared.unspecified = not set: keep the subtitle file / SDK default.
            backgroundColor: Colors.shared.unspecified,
            outlineColor: Colors.shared.unspecified,
            bold: nil,
            bottomMargin: nil
        )
        IosContentSdk.shared.setSubtitleStyle(style: style)
        // Back to file styles and SDK defaults: IosContentSdk.shared.resetSubtitleStyle()
    }

    // MARK: - 4. Other ways to show SDK pages (pick one; all need `start` to have succeeded)

    /// 4.1 Drama home variants. All return a `UIViewController` you place with your own navigation.
    ///
    /// - title: text in the SDK title bar; `nil` hides the bar (use it under your own navigation bar).
    ///   The variants without `title` keep the SDK default title.
    /// - insetsMode: `.systemBarsPadding` (default) | `.statusBarsPadding` | `.navigationBarsPadding` | `.none`.
    /// - onDramaClick: called when the user taps a drama; open the player there (4.2).
    ///   Without it nothing happens on tap.
    static func makeHomeVariants(onDramaClick: @escaping (Drama) -> Void) -> [UIViewController] {
        [
            IosContentSdk.shared.dramaHome(onDramaClick: onDramaClick),
            IosContentSdk.shared.dramaHome(title: "Dramas", onDramaClick: onDramaClick),
            IosContentSdk.shared.dramaHome(insetsMode: .navigationBarsPadding, onDramaClick: onDramaClick),
            IosContentSdk.shared.dramaHome(title: nil, insetsMode: .navigationBarsPadding, onDramaClick: onDramaClick),
        ]
    }

    /// 4.2 Creates the player for one drama, e.g. from your own list (section 7), a push
    /// notification or a deep link. Push or present the returned controller yourself.
    ///
    /// `Drama.PlayerParams`:
    /// - id: drama id.
    /// - episodeOrder: episode to start from, 1-based. 0 (default) = first episode; above the count = last episode.
    /// - drama: (optional) a Drama you already have, so title and cover show without waiting.
    ///   With a Drama from `getDramaList`, `drama.asPlayerParams()` fills all three.
    ///
    /// - Parameter onClose: the player's back button was tapped; pop / dismiss here.
    static func makePlayer(dramaId: String, onClose: @escaping () -> Void) -> UIViewController {
        IosContentSdk.shared.dramaPlayer(
            params: Drama.PlayerParams(id: dramaId, episodeOrder: 1, drama: nil),
            onCloseRequest: onClose
        )
    }

    /// 4.3 UIKit navigation: push home, then push the player when a drama is tapped.
    static func pushHome(onto navigationController: UINavigationController) {
        // `title: nil` because UINavigationController already shows a bar.
        let home = IosContentSdk.shared.dramaHome(title: nil, insetsMode: .navigationBarsPadding) { [weak navigationController] drama in
            let player = IosContentSdk.shared.dramaPlayer(params: drama.asPlayerParams()) {
                navigationController?.popViewController(animated: true)
            }
            navigationController?.pushViewController(player, animated: true)
        }
        navigationController.pushViewController(home, animated: true)
    }
}

/// 4.4 SDK home as one tab of a SwiftUI `TabView`. Make sure `start` succeeded before it appears.
/// `SdkControllerView` (SdkIntegration.swift) wraps the SDK view controller for SwiftUI.
struct SdkHomeTabExample: View {
    var body: some View {
        TabView {
            SdkControllerView {
                IosContentSdk.shared.dramaHome { drama in
                    // Open the player with your own navigation, see 4.2.
                }
            }
            .ignoresSafeArea()
            .tabItem { Label("Dramas", systemImage: "play.rectangle") }
        }
    }
}

extension SdkApiCatalog {

    // MARK: - 5. User and language

    /// 5.1 Tells the SDK which of YOUR users is active, so watch history and unlocks follow them.
    /// Call on login, account switch or logout. Not needed at launch if `initialize` got the userId.
    ///
    /// - Parameter userId: any stable id from your account system; "" = anonymous (use on logout).
    /// - Completion: `error == nil` = new identity active; otherwise the previous identity stays.
    static func onUserChanged(userId: String) {
        IosContentSdk.shared.login(userId: userId) { error in
            if let error { print("login failed: \(error.code) \(error.msg)") }
        }
    }

    /// 5.2 Your app language changed; later content requests use it.
    ///
    /// - Parameter language: BCP-47 code such as "en-US" or "zh-CN". Unsupported codes fall back to "en-US".
    /// - Completion: `effectiveLanguage` is the language the SDK really uses.
    static func onAppLanguageChanged(language: String) {
        IosContentSdk.shared.setLanguage(language: language) { effectiveLanguage, error in
            print("SDK language = \(effectiveLanguage ?? "-"), error = \(error?.code ?? "none")")
        }
    }

    // MARK: - 6. Event listeners (one per process; setting again replaces; nil stops)

    /// 6.1 Playback events. The SDK keeps a strong reference; call `stopListening()` when done.
    ///
    /// Every event has `resource` (nil before an episode is bound): dramaId, dramaName, episodeId,
    /// episodeNumber (1-based), episodeName, episodeCount. Most also have `progress`.
    static func listenToPlayerEvents() {
        IosContentSdk.shared.setPlayerEventListener(listener: PlayerEventHandler())
    }

    /// 6.2 Ad events. Every event has `ad`: format (rewarded / interstitial), scene, adUnitId.
    static func listenToAdEvents() {
        IosContentSdk.shared.setAdEventListener(listener: AdEventHandler())
    }

    /// Stops both listeners, e.g. on logout.
    static func stopListening() {
        IosContentSdk.shared.setPlayerEventListener(listener: nil)
        IosContentSdk.shared.setAdEventListener(listener: nil)
    }

    // MARK: - 7. Drama list (your own entry UI, then open the player with 4.2)

    /// Loads one page of dramas.
    ///
    /// - page: 1-based (>= 1). pageSize: >= 1.
    /// - dramaIds: (optional) only these ids; nil or empty = no filter.
    /// - recentDays: (optional) only dramas published in the last N days (>= 0); nil = no filter.
    ///   Kotlin `Int?` is `KotlinInt?` in Swift: pass `KotlinInt(int: 7)`.
    ///
    /// Each Drama: id, name, coverImageUrl, episodeSize, freeCount, description, tags.
    /// Empty array = no more data. Invalid arguments return code `INVALID_ARGUMENT`.
    static func loadDramas(completion: @escaping ([Drama]) -> Void) {
        IosContentSdk.shared.getDramaList(page: 1, pageSize: 20) { dramas, error in
            if let error { print("getDramaList failed: \(error.code) \(error.msg)") }
            completion(dramas ?? [])
        }
        // Filtered:
        // IosContentSdk.shared.getDramaList(page: 1, pageSize: 20, dramaIds: ["<ID>"], recentDays: KotlinInt(int: 7)) { dramas, error in ... }
    }
}

/// Player listener. Kotlin sealed classes become Swift subclasses: check them with `as?`.
private final class PlayerEventHandler: NSObject, PlayerEventListener {
    func onPlayerEvent(event: PlayerEvent) {
        let episode = event.resource.map { "\($0.dramaName) #\($0.episodeNumber)" } ?? "-"

        if let e = event as? PlayerEvent.FirstFrameRendered {
            // First frame on screen; startupDurationMs = prepare to first frame.
            print("[\(episode)] first frame in \(e.startupDurationMs) ms")
        } else if let e = event as? PlayerEvent.PlaybackStateChanged {
            // Playing, paused, buffering, ended, ...
            if e.state == .playing { print("[\(episode)] playing") }
        } else if let e = event as? PlayerEvent.PlaybackProgressUpdated {
            // About every 300 ms while playing (best-effort, not a precise timer).
            _ = (e.progress.positionMs, e.progress.durationMs)
        } else if event is PlayerEvent.PlaybackEnded {
            print("[\(episode)] finished")
        } else if let e = event as? PlayerEvent.PlaybackError {
            // recoverable = the SDK may retry by itself.
            print("[\(episode)] error \(e.error.code) recoverable=\(e.recoverable)")
        }
        // Also: PositionDiscontinuity (seek), QualityChanged, QualityResolved,
        // PlaybackSpeedChanged, ControlDispatched.
    }
}

/// Ad listener. Kotlin sealed classes become Swift subclasses: check them with `as?`.
private final class AdEventHandler: NSObject, AdEventListener {
    func onAdEvent(event: AdEvent) {
        if event is AdEvent.Impression {
            print("ad shown: \(event.ad.format)")
        } else if let e = event as? AdEvent.RewardEarned {
            print("reward: \(e.rewardAmount) \(e.rewardType)")
        } else if let e = event as? AdEvent.Paid {
            // Impression value, separate from Impression and not guaranteed for every impression.
            // valueMicros / 1_000_000 = value in currencyCode; final earnings come from the ad platform.
            print("paid: \(e.valueMicros) micros \(e.currencyCode) (\(e.precision))")
        } else if event is AdEvent.Dismissed {
            print("ad closed")
        } else if let e = event as? AdEvent.Error {
            // stage = loading or presentation.
            print("ad error at \(e.stage): \(e.error.code)")
        }
        // Also: LoadStarted, Loaded, Clicked.
    }
}
