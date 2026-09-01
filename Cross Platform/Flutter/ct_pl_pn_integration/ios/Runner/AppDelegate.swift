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
    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  override func application(
    _ application: UIApplication,
    didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
  ) {
    // Pass APNS device token to Plotline.
    PlotlinePlugin.setDeviceToken(deviceToken: deviceToken)
    super.application(application, didRegisterForRemoteNotificationsWithDeviceToken: deviceToken)
  }

  override func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    willPresent notification: UNNotification,
    withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
  ) {
    // Allow Plotline to handle foreground notifications.
    PlotlinePlugin.onNotificationReceived(notification: notification, completionHandler: completionHandler)
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