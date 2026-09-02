import UIKit
import UserNotifications
import UserNotificationsUI
import Plotline

class NotificationViewController: UIViewController, UNNotificationContentExtension {

    override func viewDidLoad() {
        super.viewDidLoad()
    }

    func didReceive(_ notification: UNNotification) {
        // App Group must match the one set in the main app (Runner.entitlements)
        // and must be configured BEFORE handling the notification content.
        PlotlinePush.setAppGroupId(appGroupId: "group.com.example.ctPlPnIntegration")
        PlotlinePush.handleNotificationContent(notification, inViewController: self)
    }
}