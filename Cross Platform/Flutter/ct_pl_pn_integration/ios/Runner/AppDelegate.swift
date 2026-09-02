import Flutter
import UIKit
import UserNotifications
import CleverTapSDK
import clevertap_plugin
import plotline_engage

@main
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    // Plotline push: App Group must be set BEFORE Plotline.init (in Dart).
    PlotlineBridge.setAppGroupId(groupId: "group.com.example.ctPlPnIntegration")
    PlotlinePlugin.enablePush(self)

    // CleverTap auto-integration: reads CleverTapAccountID / CleverTapToken from
    // Info.plist (injected from the local Secrets.xcconfig) and initializes the SDK.
    CleverTap.autoIntegrate()
    CleverTapPlugin.sharedInstance()?.applicationDidLaunch(options: launchOptions)

    // Register the rich push category used by the NotificationContent extension
    // (CTNotificationContent) - must match the campaign category on the dashboard.
    let action1 = UNNotificationAction(identifier: "action_1", title: "Back", options: [])
    let action2 = UNNotificationAction(identifier: "action_2", title: "Next", options: [])
    let action3 = UNNotificationAction(identifier: "action_3", title: "View In App", options: [])
    let category = UNNotificationCategory(identifier: "CTNotification", actions: [action1, action2, action3], intentIdentifiers: [], options: [])
    UNUserNotificationCenter.current().setNotificationCategories([category])

    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  override func application(
    _ application: UIApplication,
    didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
  ) {
    // Pass APNS device token to Plotline and CleverTap. (CleverTap's auto-integration
    // also swizzles this method; calling setPushToken explicitly is the documented,
    // belt-and-suspenders path.)
    PlotlinePlugin.setDeviceToken(deviceToken: deviceToken)
    CleverTap.sharedInstance()?.setPushToken(deviceToken)
    super.application(application, didRegisterForRemoteNotificationsWithDeviceToken: deviceToken)
  }

  override func application(
    _ application: UIApplication,
    didFailToRegisterForRemoteNotificationsWithError error: Error
  ) {
    // Surface WHY APNs registration failed (e.g. missing aps-environment entitlement
    // / Push capability, simulator without push support, permission denied).
    print("APNs registration FAILED: \(error.localizedDescription)")
    super.application(application, didFailToRegisterForRemoteNotificationsWithError: error)
  }

  override func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    willPresent notification: UNNotification,
    withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
  ) {
    let userInfo = notification.request.content.userInfo
    if CleverTap.sharedInstance()?.isCleverTapNotification(userInfo) == true {
      // CleverTap foreground push: record the "Notification Viewed" impression and
      // present it. Without calling completionHandler iOS suppresses the notification.
      CleverTap.handlePushNotification(userInfo, openDeepLinksInForeground: true)
      completionHandler([.alert, .sound, .badge])
    } else {
      // Allow Plotline to handle foreground notifications.
      PlotlinePlugin.onNotificationReceived(notification: notification, completionHandler: completionHandler)
    }
  }

  override func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    didReceive response: UNNotificationResponse,
    withCompletionHandler completionHandler: @escaping () -> Void
  ) {
    // Allow Plotline to listen to actions on push notifications.
    PlotlinePlugin.plotlineUserNotificationCenter(center: center, didReceive: response)
    completionHandler()
  }
}