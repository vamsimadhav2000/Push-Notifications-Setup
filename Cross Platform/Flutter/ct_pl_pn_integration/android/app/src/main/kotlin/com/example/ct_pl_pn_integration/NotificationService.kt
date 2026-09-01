package com.example.ct_pl_pn_integration

import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import so.plotline.plotline.PlotlinePlugin

/**
 * Single FCM entry point so CleverTap and Plotline can share one pipeline.
 *  - Plotline:  PlotlinePlugin.showNotification (only handles Plotline payloads)
 *  - CleverTap: CTFcmMessageHandler - the same renderer CleverTap's own
 *               FcmMessageListenerService uses internally.
 * New tokens are fanned out to both SDKs.
 */
class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        if (PlotlinePlugin.isPushPlotline(data)) {
            PlotlinePlugin.showNotification(applicationContext, data)
        } else {
            CTFcmMessageHandler().createNotification(applicationContext, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PlotlinePlugin.setFcmToken(applicationContext, token)
        CleverTapAPI.getDefaultInstance(applicationContext)?.pushFcmRegistrationId(token, true)
    }
}