import UserNotifications

class NotificationService: UNNotificationServiceExtension {

    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        self.contentHandler = contentHandler
        bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)

        // App Group must match the one set in the main app (Runner.entitlements).
        PlotlineBridge.setAppGroupId(groupId: "group.com.example.ctPlPnIntegration")

        // Pass payload data to Plotline if originating from Plotline.
        if PlotlineBridge.isPushPlotline(request: request) {
            PlotlineBridge.onNotificationReceived(request: request, contentHandler: contentHandler)
        }

        if let bestAttemptContent = bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }

    override func serviceExtensionTimeWillExpire() {
        if let contentHandler = contentHandler, let bestAttemptContent = bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }
}