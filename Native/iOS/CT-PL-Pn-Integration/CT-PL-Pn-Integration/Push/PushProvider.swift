import Foundation
import UserNotifications

/// A push provider that can identify and handle APNs payloads addressed to it.
///
/// Providers are checked in registration order by [PushProviderRegistry] using
/// first-match routing: [canHandle] delegates to each SDK's own detection API
/// (no hardcoded marker keys) and the matching provider renders the
/// notification.
///
/// Mirrors the Android `PushProvider` interface.
protocol PushProvider {

    /// Human-readable provider name, used in logs.
    var name: String { get }

    /// True if this provider owns the given payload. Implementations should
    /// delegate to the SDK's own detection API.
    func canHandle(request: UNNotificationRequest) -> Bool

    /// Renders (or otherwise processes) a notification received while the app
    /// is in the foreground. Only invoked when [canHandle] returned true.
    func handleWillPresent(_ notification: UNNotification,
                           completionHandler: @escaping (UNNotificationPresentationOptions) -> Void)

    /// Processes a notification tapped by the user. Only invoked when
    /// [canHandle] returned true.
    func handleNotificationTap(_ response: UNNotificationResponse,
                               completionHandler: @escaping () -> Void)

    /// Receives the current APNs device token, typically used to register it
    /// with the provider's backend. Called on token refresh and on app launch.
    func onNewToken(_ deviceToken: Data)
}