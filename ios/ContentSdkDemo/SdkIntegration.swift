import SwiftUI
import UIKit
import ContentSdk

// =============================================================================
// Everything related to the SDK lives in this file:
//   - start()            -> Step 4: ad consent + start the SDK
//   - openDramaHome()    -> Step 5: show the SDK page
//   - SdkDestination     -> how SDK pages are placed in a SwiftUI NavigationStack
// DemoHomeView.swift is only demo UI.
// =============================================================================

/// Screens the demo can push. Each one shows an SDK view controller.
enum SdkRoute: Hashable {
    case dramaHome
    case dramaPlayer(Drama.PlayerParams)
}

@MainActor
final class ContentSdkDemo: ObservableObject {

    /// Shown on the demo home screen.
    @Published private(set) var status: DemoSdkStatus = .notStarted
    /// SwiftUI navigation stack. SDK pages are pushed onto it.
    @Published var path: [SdkRoute] = []

    func onOpenDramaHomeTapped() {
        switch status {
        // Already started: open the page directly. `start` only needs to succeed once per launch.
        case .ready: openDramaHome()
        // A start request is running; ignore repeated taps.
        case .starting: break
        // Not started yet (or the last attempt failed): start first, then open the page.
        case .notStarted, .failed: start { [weak self] in self?.openDramaHome() }
        }
    }

    /// Content SDK - integration Step 4 (consent + start)
    ///
    /// 1. `showAdConsentIfRequired` shows Google's privacy (UMP / GDPR) dialog when the user's
    ///    region needs it. Otherwise it returns immediately. It needs a view controller to present on.
    ///    Skip it if your app already runs UMP.
    /// 2. `start` boots the SDK (network, player, ads). Call it once; do NOT show SDK pages before
    ///    it succeeds.
    ///
    /// Both callbacks run on the main thread, so it is safe to update UI inside them.
    /// Tip: you can also call this early (e.g. right after launch) so the SDK is ready when needed.
    private func start(onReady: @escaping () -> Void) {
        guard let presenter = UIApplication.shared.topViewController else { return }
        status = .starting

        IosContentSdk.shared.showAdConsentIfRequired(viewController: presenter) { [weak self] consent in
            guard consent.canRequestAds else {
                // The user did not allow ad requests. Decide what your app should do here.
                self?.status = .failed(code: "Consent", message: consent.errorMessage ?? "Ads not allowed")
                return
            }

            IosContentSdk.shared.start { [weak self] error in
                if let error {
                    // Show a failure message or retry entry.
                    print("Content SDK start failed: \(error.code) \(error.msg)")
                    self?.status = .failed(code: error.code, message: error.msg)
                    return
                }
                self?.status = .ready
                onReady()
            }
        }
    }

    /// Content SDK - integration Step 5 (show the SDK page)
    ///
    /// Pushes the SDK drama home onto this demo's SwiftUI NavigationStack; `SdkDestination` below
    /// builds the screen. The SDK handles browsing, episode switching and ad unlock. On iOS your
    /// app owns navigation, so the player is pushed when the user taps a drama.
    ///
    /// Using UIKit navigation, embedding the page in a tab, or opening the player directly?
    /// See section 4 of SdkApiCatalog.swift.
    private func openDramaHome() {
        path.append(.dramaHome)
    }
}

/// Builds the SwiftUI screen for each `SdkRoute` (used with `.navigationDestination`).
struct SdkDestination: View {
    let route: SdkRoute
    @ObservedObject var demo: ContentSdkDemo

    var body: some View {
        switch route {
        case .dramaHome:
            SdkControllerView {
                IosContentSdk.shared.dramaHome(
                    // nil hides the SDK title bar, because this NavigationStack already shows one.
                    title: nil,
                    // The navigation bar covers the top; let the SDK pad only the bottom edge.
                    insetsMode: .navigationBarsPadding
                ) { [weak demo] drama in
                    // The user tapped a drama: open the SDK player for it.
                    demo?.path.append(.dramaPlayer(drama.asPlayerParams()))
                }
            }
            .ignoresSafeArea(edges: .bottom)
            .navigationTitle("Dramas")
            .navigationBarTitleDisplayMode(.inline)

        case .dramaPlayer(let params):
            SdkControllerView {
                IosContentSdk.shared.dramaPlayer(params: params) { [weak demo] in
                    // The player's own back button was tapped: leave the player.
                    demo?.path.removeLast()
                }
            }
            .ignoresSafeArea()
            // The player draws its own controls, including a back button.
            .toolbar(.hidden, for: .navigationBar)
        }
    }
}

/// Wraps an SDK `UIViewController` so it can be used in SwiftUI.
/// The controller is created once; never create SDK controllers inside `updateUIViewController`.
struct SdkControllerView: UIViewControllerRepresentable {
    let factory: () -> UIViewController

    func makeUIViewController(context: Context) -> UIViewController { factory() }

    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}

private extension UIApplication {
    /// The view controller currently on screen; used to present the consent dialog.
    var topViewController: UIViewController? {
        let scene = connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
        var top = scene?.keyWindow?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
