//
//  NotificationService.swift
//  NotificationServiceExtension
//
//  Created by Work on 31/08/26.
//

import UserNotifications
import CTNotificationService
import Plotline

class NotificationService: CTNotificationServiceExtension {

    override func didReceive(_ request: UNNotificationRequest,
                             withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {

        // Use the SAME App Group ID configured in the main app (secrets.properties).
        PlotlinePush.setAppGroupId(appGroupId: AppConfig.appGroupID)

        // Plotline: renders both rich (media) and normal Plotline pushes.
        if PlotlinePush.isPushPlotline(request: request) {
            PlotlinePush.onNotificationReceived(request: request, contentHandler: contentHandler)
            return
        }

        // CleverTap rich push (ct_mediaUrl / ct_mediaType) via CTNotificationService.
        // Normal CleverTap pushes carry no media keys and pass through unchanged,
        // so both rich and normal notifications are supported.
        super.didReceive(request, withContentHandler: contentHandler)
    }

    override func serviceExtensionTimeWillExpire() {
        super.serviceExtensionTimeWillExpire()
    }
}