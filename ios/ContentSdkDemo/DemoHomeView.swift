import SwiftUI

// -----------------------------------------------------------------------------
// Demo UI only. Nothing in this file is required to integrate the SDK.
// -----------------------------------------------------------------------------

/// What the demo home screen shows about the SDK. Not part of the SDK API.
enum DemoSdkStatus: Equatable {
    case notStarted
    case starting
    case ready
    case failed(code: String, message: String)
}

private enum DemoColors {
    static let background = Color(red: 0.055, green: 0.059, blue: 0.075)
    static let surface = Color(red: 0.090, green: 0.098, blue: 0.122)
    static let outline = Color(red: 0.149, green: 0.161, blue: 0.196)
    static let textPrimary = Color(red: 0.957, green: 0.961, blue: 0.969)
    static let textSecondary = Color(red: 0.604, green: 0.627, blue: 0.675)
    static let accent = Color(red: 1.0, green: 0.302, blue: 0.427)
    static let accentEnd = Color(red: 1.0, green: 0.541, blue: 0.298)
    static let success = Color(red: 0.204, green: 0.827, blue: 0.600)
    static let warning = Color(red: 0.984, green: 0.749, blue: 0.141)
    static let error = Color(red: 0.973, green: 0.443, blue: 0.443)
}

/// One row of the "integration steps" checklist and where to find it in this project.
private let integrationSteps: [(title: String, file: String)] = [
    ("Install the SDK Pod", "Podfile"),
    ("Set your AdMob Application ID", "Info.plist"),
    ("Initialize the SDK", "ContentSdkDemoApp.swift"),
    ("Ad consent + start", "SdkIntegration.swift"),
    ("Open the SDK page", "SdkIntegration.swift"),
]

struct DemoHomeView: View {
    @StateObject private var demo = ContentSdkDemo()

    var body: some View {
        NavigationStack(path: $demo.path) {
            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        header.padding(.top, 32)
                        statusCard.padding(.top, 28)
                        stepsCard.padding(.top, 16)
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 24)
                }
                openButton
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
            }
            .background(DemoColors.background.ignoresSafeArea())
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: SdkRoute.self) { route in
                SdkDestination(route: route, demo: demo)
            }
        }
        .tint(DemoColors.accent)
        .preferredColorScheme(.dark)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 0) {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(LinearGradient(colors: [DemoColors.accent, DemoColors.accentEnd],
                                     startPoint: .topLeading, endPoint: .bottomTrailing))
                .frame(width: 52, height: 52)
                .overlay(Image(systemName: "play.fill").font(.system(size: 22)).foregroundStyle(.white))
            Text("CONTENT SDK")
                .font(.system(size: 12, weight: .semibold))
                .kerning(2)
                .foregroundStyle(DemoColors.accent)
                .padding(.top, 20)
            Text("Integration Demo")
                .font(.system(size: 30, weight: .bold))
                .foregroundStyle(DemoColors.textPrimary)
                .padding(.top, 6)
            Text("The minimum code needed to bring short-drama content into your iOS app.")
                .font(.system(size: 15))
                .foregroundStyle(DemoColors.textSecondary)
                .lineSpacing(4)
                .padding(.top, 8)
        }
    }

    private var statusCard: some View {
        let (label, detail, color): (String, String, Color) = {
            switch demo.status {
            case .notStarted: return ("Initialized", "Tap the button below to start the SDK.", DemoColors.textSecondary)
            case .starting: return ("Starting…", "Checking ad consent and booting the SDK.", DemoColors.warning)
            case .ready: return ("Ready", "The SDK is running. SDK pages can be opened.", DemoColors.success)
            case let .failed(code, message): return ("Start failed", "\(code) · \(message)", DemoColors.error)
            }
        }()

        return DemoCard {
            sectionTitle("SDK STATUS")
            HStack(spacing: 10) {
                Circle().fill(color).frame(width: 10, height: 10)
                Text(label)
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(DemoColors.textPrimary)
            }
            .padding(.top, 10)
            .animation(.easeInOut, value: demo.status)
            Text(detail)
                .font(.system(size: 13))
                .foregroundStyle(DemoColors.textSecondary)
                .padding(.top, 6)
        }
    }

    private var stepsCard: some View {
        DemoCard {
            sectionTitle("INTEGRATION STEPS")
            ForEach(Array(integrationSteps.enumerated()), id: \.offset) { index, step in
                HStack(spacing: 14) {
                    Text("\(index + 1)")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(DemoColors.accent)
                        .frame(width: 28, height: 28)
                        .background(DemoColors.accent.opacity(0.14), in: Circle())
                    VStack(alignment: .leading, spacing: 2) {
                        Text(step.title)
                            .font(.system(size: 15, weight: .medium))
                            .foregroundStyle(DemoColors.textPrimary)
                        Text(step.file)
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundStyle(DemoColors.textSecondary)
                    }
                }
                .padding(.vertical, 10)
            }
        }
    }

    private var openButton: some View {
        Button(action: demo.onOpenDramaHomeTapped) {
            ZStack {
                if demo.status == .starting {
                    ProgressView().tint(.white)
                } else {
                    Text(isFailed ? "Retry" : "Open Drama Home")
                        .font(.system(size: 16, weight: .semibold))
                }
            }
            .frame(maxWidth: .infinity, minHeight: 56)
            .foregroundStyle(.white)
            .background(DemoColors.accent.opacity(demo.status == .starting ? 0.5 : 1),
                        in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .disabled(demo.status == .starting)
    }

    private var isFailed: Bool {
        if case .failed = demo.status { return true }
        return false
    }

    private func sectionTitle(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 11))
            .kerning(1.5)
            .foregroundStyle(DemoColors.textSecondary)
    }
}

private struct DemoCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) { content }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(18)
            .background(DemoColors.surface, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 20, style: .continuous).stroke(DemoColors.outline, lineWidth: 1))
    }
}

#Preview {
    DemoHomeView()
}
