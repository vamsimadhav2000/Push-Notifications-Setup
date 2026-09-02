import UserNotifications
import CTNotificationService

class NotificationService: CTNotificationServiceExtension {

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        // App Group must match the one set in the main app (Runner.entitlements).
        PlotlineBridge.setAppGroupId(groupId: "group.com.example.ctPlPnIntegration")

        // Pass payload data to Plotline if originating from Plotline; otherwise let
        // CTNotificationServiceExtension handle CleverTap rich media attachments.
        if PlotlineBridge.isPushPlotline(request: request) {
            PlotlineBridge.onNotificationReceived(request: request, contentHandler: contentHandler)
        } else {
            super.didReceive(request, withContentHandler: contentHandler)
        }
    }

    override func serviceExtensionTimeWillExpire() {
        super.serviceExtensionTimeWillExpire()
    }
}
