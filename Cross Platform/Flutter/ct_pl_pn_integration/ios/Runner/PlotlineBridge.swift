import Plotline
import UIKit
import UserNotifications

// Bridge used by BOTH the Runner app and the NotificationService extension.
// Compiled into both targets (target membership), so the extension only links
// against this file and Plotline.
public class PlotlineBridge {
    public static func isPushPlotline(request: UNNotificationRequest) -> Bool {
        return PlotlinePush.isPushPlotline(request: request)
    }

    public static func onNotificationReceived(request: UNNotificationRequest, contentHandler: @escaping (UNNotificationContent) -> Void) {
        PlotlinePush.onNotificationReceived(request: request, contentHandler: contentHandler)
    }

    public static func setAppGroupId(groupId: String) {
        PlotlinePush.setAppGroupId(appGroupId: groupId)
    }

    public static func handleNotificationContent(notification: UNNotification, viewController: UIViewController) {
        PlotlinePush.handleNotificationContent(notification, inViewController: viewController)
    }
}