import SwiftUI
import ContentSdk

@main
struct ContentSdkDemoApp: App {

    /// Content SDK - integration Step 3 (initialize)
    ///
    /// Call `initialize` exactly ONCE when your app launches. `App.init` (SwiftUI) or
    /// `application(_:didFinishLaunchingWithOptions:)` (UIKit) are good places.
    /// It only stores your configuration - no network, ads, or player start here, so it is cheap.
    /// Do NOT call it from a SwiftUI `body`, which can run many times.
    init() {
        // (Optional) Print SDK status messages to the Xcode console. Keep it off in your release builds.
        #if DEBUG
        IosContentSdk.shared.setLoggingEnabled(enabled: true)
        #endif

        IosContentSdk.shared.initialize(
            appId: SdkConfig.appId,
            appSecret: SdkConfig.appSecret,
            userId: SdkConfig.userId,
            // (Optional) Content language, e.g. "en-US". Pass "" to follow your app's locale.
            language: ""
        )
    }

    var body: some Scene {
        WindowGroup {
            DemoHomeView()
        }
    }
}

/// Your SDK credentials.
///
/// The values below are DEMO credentials so this project runs out of the box.
/// Replace them with YOUR App ID / App Secret from the SDK provider - they are bound to your
/// bundle id, so the demo values will not work in your app.
/// Tip: do not hard-code your production secret in a public repository.
enum SdkConfig {
    /// YOUR Content App ID.
    static let appId = "2edf060b-291d-45da-b2f6-bc61ae797a2d"

    /// YOUR Content App Secret.
    static let appSecret = "3789c8e8969fa583d277ca0f28b229a43d24d28fbfae36244c51d7e7ab7ff50f"

    /// YOUR logged-in user's id, so watch history and unlocks follow the user across devices.
    /// Pass "" if your app has no login - the SDK then creates an anonymous user.
    static let userId = ""
}
