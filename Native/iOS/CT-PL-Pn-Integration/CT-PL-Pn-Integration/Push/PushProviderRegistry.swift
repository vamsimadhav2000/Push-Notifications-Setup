import Foundation
import UserNotifications

/// Central registration point for all push providers.
///
/// First-match routing: providers are asked (in order) whether they own an
/// incoming payload; the first match handles it. Adding a new provider is just
/// adding a class and one entry to [providers].
///
/// CleverTap is checked first so that if a payload is ever dual-tagged with
/// both providers' marker keys, CleverTap (the primary platform) wins.
///
/// Mirrors the Android `PushProviderRegistry`.
final class PushProviderRegistry {

    static let shared = PushProviderRegistry()

    private let providers: [PushProvider] = [
        CleverTapPushProvider(),
        PlotlinePushProvider()
    ]

    /// Routes a foreground notification to the first provider that owns it.
    /// Falls back to a default presentation so unclaimed pushes still show.
    func routeWillPresent(_ notification: UNNotification,
                          completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        for provider in providers {
            guard provider.canHandle(request: notification.request) else { continue }
            print("[PushProviderRegistry] \(provider.name) claimed foreground notification")
            provider.handleWillPresent(notification, completionHandler: completionHandler)
            return
        }
        print("[PushProviderRegistry] No provider claimed foreground notification")
        completionHandler([.badge, .sound, .alert])
    }

    /// Routes a tapped notification to the first provider that owns it.
    func routeNotificationTap(_ response: UNNotificationResponse,
                              completionHandler: @escaping () -> Void) {
        for provider in providers {
            guard provider.canHandle(request: response.notification.request) else { continue }
            print("[PushProviderRegistry] \(provider.name) claimed notification tap")
            provider.handleNotificationTap(response, completionHandler: completionHandler)
            return
        }
        print("[PushProviderRegistry] No provider claimed notification tap")
        completionHandler()
    }

    /// Fans out the APNs token to every provider so the token is always
    /// registered, independent of whether the system fires the callback.
    func onNewToken(_ deviceToken: Data) {
        for provider in providers {
            provider.onNewToken(deviceToken)
            print("[PushProviderRegistry] \(provider.name) token sync complete")
        }
    }
}