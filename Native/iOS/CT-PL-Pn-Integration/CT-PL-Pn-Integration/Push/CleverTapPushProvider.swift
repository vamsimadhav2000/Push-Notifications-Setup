import Foundation
import UserNotifications
import CleverTapSDK

/// CleverTap push provider.
///
/// Detection delegates to CleverTap's own `isCleverTapNotification`; foreground
/// presentation and token sync delegate to the SDK's public handlers. Because
/// the app uses `CleverTap.autoIntegrate()`, CleverTap itself already swizzles
/// the notification-tap and background callbacks, so this provider is a thin
/// router for those flows - behaviour (rich media via the NSE, click tracking,
/// token sync) is identical to a pure CleverTap auto-integration, matching the
/// Android setup where CleverTapPushProvider calls `CTFcmMessageHandler`.
final class CleverTapPushProvider: PushProvider {

    var name: String { "CleverTap" }

    func canHandle(request: UNNotificationRequest) -> Bool {
        CleverTap.sharedInstance()?.isCleverTapNotification(request.content.userInfo) == true
    }

    func handleWillPresent(_ notification: UNNotification,
                           completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        CleverTap.handleWillPresent(notification,
                                    withDefaultOptions: [.badge, .sound, .alert],
                                    completionHandler: completionHandler)
    }

    func handleNotificationTap(_ response: UNNotificationResponse,
                               completionHandler: @escaping () -> Void) {
        // CleverTap.autoIntegrate() already swizzled
        // userNotificationCenter:didReceiveNotificationResponse: and called
        // handlePushNotification. Routing here is intentionally a no-op to avoid
        // double-counting the "Notification Clicked" event.
        completionHandler()
    }

    func onNewToken(_ deviceToken: Data) {
        CleverTap.sharedInstance()?.setPushToken(deviceToken)
    }
}