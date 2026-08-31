import Foundation
import Combine

/// Holds the deeplink data received from a push notification tap and exposes it
/// to the UI so a modal can be shown. CleverTap and Plotline both deliver their
/// deeplink payload here (see AppDelegate). Mirrors the Android
/// `DeepLinkModalManager`.
final class DeepLinkModalManager: ObservableObject {

    static let shared = DeepLinkModalManager()

    struct DeepLinkData {
        let source: String
        let deepLink: String
    }

    @Published var data: DeepLinkData?

    private init() {}

    func present(source: String, deepLink: String) {
        data = DeepLinkData(source: source, deepLink: deepLink)
    }

    func dismiss() {
        data = nil
    }
}