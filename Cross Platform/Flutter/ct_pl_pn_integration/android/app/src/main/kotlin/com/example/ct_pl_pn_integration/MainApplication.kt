package com.example.ct_pl_pn_integration

import com.clevertap.android.sdk.Application as CleverTapApplication
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.interfaces.NotificationHandler
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler

/**
 * CleverTap auto-integration (minimal code):
 *  - Extends the CleverTap SDK [CleverTapApplication], which registers the
 *    activity lifecycle callbacks automatically (no manual register() call).
 *  - Registers the Push Templates notification handler so rich media templates
 *    (timer, carousel, rating, ...) render. FCM messages are delivered via the
 *    custom NotificationService router, which calls CTFcmMessageHandler and
 *    therefore flows through this handler.
 */
class MainApplication : CleverTapApplication() {

    override fun onCreate() {
        CleverTapAPI.setNotificationHandler(
            PushTemplateNotificationHandler() as NotificationHandler
        )
        super.onCreate()
    }
}