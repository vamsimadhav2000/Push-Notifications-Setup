//
//  AppDelegate.swift
//  CT-PL-Pn-Integration
//
//  Created by Work on 31/08/26.
//

import UIKit
import UserNotifications
import CleverTapSDK
import Plotline

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, CleverTapPushNotificationDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        configureCleverTap()
        configurePlotline()
        registerForPush()
        return true
    }

    // MARK: - CleverTap

    private func configureCleverTap() {
        CleverTap.setDebugLevel(3)
        CleverTap.setCredentialsWithAccountID(AppConfig.clevertapAccountID,
                                              andToken: AppConfig.clevertapToken)

        // Auto-integration (CleverTapApplication on Android): CleverTap swizzles
        // the app delegate push callbacks (token, notification tap, background
        // push, silent-in-foreground) so CleverTap notifications are handled
        // automatically. Push payload routing is still done via
        // PushProviderRegistry so Plotline gets its data too.
        CleverTap.autoIntegrate()
        CleverTap.sharedInstance()?.enableDeviceNetworkInfoReporting(true)
        CleverTap.sharedInstance()?.setPushNotificationDelegate(self)

        identifyCleverTapUser()
    }

    private func identifyCleverTapUser() {
        // CleverTap: onUserLogin creates/merges the profile keyed by Identity,
        // then attributes all subsequent events on this device to that user.
        CleverTap.sharedInstance()?.onUserLogin([
            "Name": AppConfig.clevertapUserName,
            "Identity": AppConfig.clevertapUserIdentity,
            "Email": AppConfig.clevertapUserEmail
        ])
    }

    // MARK: - Plotline

    private func configurePlotline() {
        // Must be called BEFORE Plotline.initialize.
        PlotlinePush.setAppGroupId(appGroupId: AppConfig.appGroupID)

        Plotline.initialize(apiKey: AppConfig.plotlineAPIKey, userId: AppConfig.plotlineUserID)

        // Plotline: user attributes for cohort filtering.
        Plotline.identify(attributes: ["subscription": "paid", "plan": "pro"])

        PlotlinePush.enablePush(self)

        // Plotline: handle key/value pairs configured on dashboard buttons.
        Plotline.setPlotlineRedirectListener { keyValuePairs in
            print("Plotline redirect: \(keyValuePairs)")
        }

        // Plotline: forward campaign interaction events to CleverTap.
        Plotline.setPlotlineEventsListener { eventName, properties in
            print("Plotline event: \(eventName) - \(properties)")
            CleverTap.sharedInstance()?.recordEvent(eventName, withProps: properties)
        }

        // Plotline: custom key/value pairs set on the dashboard are delivered
        // here on push tap. If a "deeplink" key is present, surface it to the
        // UI as a modal.
        PlotlinePush.setPlotlinePushClickListener { customData in
            print("Plotline push click: \(customData)")
            if let deepLink = customData["deeplink"], !deepLink.isEmpty {
                DeepLinkModalManager.shared.present(source: "Plotline", deepLink: deepLink)
            }
        }
    }

    // MARK: - Push Registration

    private func registerForPush() {
        UNUserNotificationCenter.current().delegate = self
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            guard granted else { return }
            DispatchQueue.main.async {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
    }

    // MARK: - Push Token

    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        // Fans the token out to every provider (CleverTap + Plotline), like the
        // Android PushProviderRegistry.onNewToken.
        PushProviderRegistry.shared.onNewToken(deviceToken)
    }

    func application(_ application: UIApplication,
                     didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("Failed to register for remote notifications: \(error.localizedDescription)")
    }

    // MARK: - UNUserNotificationCenterDelegate

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        PushProviderRegistry.shared.routeWillPresent(notification, completionHandler: completionHandler)
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        // CleverTap tap tracking/deeplink fires inside its auto-integrated
        // swizzle; the registry routes the payload to the owning provider
        // (Plotline) and we surface the custom deeplink for the UI.
        PushProviderRegistry.shared.routeNotificationTap(response, completionHandler: completionHandler)
    }

    // MARK: - CleverTapPushNotificationDelegate

    func pushNotificationTapped(withCustomExtras customExtras: [AnyHashable: Any]) {
        handleCleverTapDeepLink(from: customExtras)
    }

    /// Surfaces CleverTap's reserved "Deep link" dashboard field (wzrk_dl),
    /// falling back to a "deeplink" custom key-value pair - same as the Android
    /// MainActivity.handlePushExtras.
    private func handleCleverTapDeepLink(from payload: [AnyHashable: Any]) {
        let deepLink = payload["wzrk_dl"] as? String ?? payload["deeplink"] as? String
        if let deepLink, !deepLink.isEmpty {
            DeepLinkModalManager.shared.present(source: "CleverTap", deepLink: deepLink)
        }
    }
}