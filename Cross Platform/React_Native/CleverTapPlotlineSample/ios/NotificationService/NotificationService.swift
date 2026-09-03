import UserNotifications
import CTNotificationService

class NotificationService: CTNotificationServiceExtension {
  override func didReceive(
    _ request: UNNotificationRequest,
    withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void
  ) {
    super.didReceive(request, withContentHandler: contentHandler)
  }
}