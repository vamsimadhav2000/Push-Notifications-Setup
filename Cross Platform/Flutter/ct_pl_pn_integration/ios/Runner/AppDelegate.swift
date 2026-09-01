import Flutter
import UIKit
import CleverTapSDK
import clevertap_plugin

@main
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    // CleverTap auto-integration: reads CleverTapAccountID / CleverTapToken from
    // Info.plist (injected from the local Secrets.xcconfig) and initializes the SDK.
    CleverTap.autoIntegrate()
    CleverTapPlugin.sharedInstance()?.applicationDidLaunch(options: launchOptions)
    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}