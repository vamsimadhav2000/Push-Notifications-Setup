import Foundation
import UserNotifications
import Plotline

/// Plotline push provider.
///
/// Detection and rendering delegate to Plotline's own APIs. The Plotline SDK
/// merges no receiver/service, so it depends entirely on the host app feeding
/// payloads and the APNs token to it.
final class PlotlinePushProvider: PushProvider {

    var name: String { "Plotline" }

    func canHandle(request: UNNotificationRequest) -> Bool {
        PlotlinePush.isPushPlotline(request: request)
    }

    func handleWillPresent(_ notification: UNNotification,
                           completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        PlotlinePush.onNotificationReceived(notification: notification,
                                            completionHandler: completionHandler)
    }

    func handleNotificationTap(_ response: UNNotificationResponse,
                               completionHandler: @escaping () -> Void) {
        PlotlinePush.userNotificationCenter(center: UNUserNotificationCenter.current(),
                                            didReceive: response)
        completionHandler()
    }

    func onNewToken(_ deviceToken: Data) {
        PlotlinePush.setPushToken(deviceToken: deviceToken)
    }
}