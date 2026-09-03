//
//  NotificationViewController.swift
//  PlContent
//
//  Created by Work on 02/09/26.
//

import UIKit
import UserNotifications
import UserNotificationsUI
import Plotline

class NotificationViewController: UIViewController, UNNotificationContentExtension {

    override func viewDidLoad() {
        super.viewDidLoad()
    }

    func didReceive(_ notification: UNNotification) {
        // App Group must match the one set in the main app (secrets.properties,
        // CT-PL-Pn-Integration.entitlements) and must be configured BEFORE
        // Plotline renders the rich content.
        PlotlinePush.setAppGroupId(appGroupId: AppConfig.appGroupID)
        PlotlinePush.handleNotificationContent(notification, inViewController: self)
    }
}