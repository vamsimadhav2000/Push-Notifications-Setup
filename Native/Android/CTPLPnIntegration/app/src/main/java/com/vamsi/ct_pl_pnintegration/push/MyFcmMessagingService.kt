package com.vamsi.ct_pl_pnintegration.push

import android.os.Bundle
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import so.plotline.insights.PlotlinePush

/**
 * Single FCM entry point that routes payloads to CleverTap or Plotline.
 *
 * - Plotline payloads -> rendered by the Plotline SDK.
 * - CleverTap payloads -> rendered by the CleverTap SDK, including rich media
 *   push templates (PushTemplateNotificationHandler is set in [com.vamsi.ct_pl_pnintegration.CTPLApplication]).
 * - Anything else -> handle yourself.
 */
class MyFcmMessagingService : FirebaseMessagingService() {

    // onNewToken is deprecated in firebase-messaging 25.x in favor of the
    // FID-based onRegistered(). We intentionally keep onNewToken because
    // CleverTap (pushFcmRegistrationId) and Plotline (setFcmToken) still
    // require the classic FCM registration token, not the Firebase Installation ID.
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Share the refreshed FCM token with both providers.
        CleverTapAPI.getDefaultInstance(applicationContext)
            ?.pushFcmRegistrationId(token, true)
        PlotlinePush.setFcmToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        if (data.isEmpty()) return

        try {
            if (PlotlinePush.isPushPlotline(data)) {
                PlotlinePush.showNotification(applicationContext, data)
                return
            }

            val extras = Bundle()
            for ((key, value) in data) {
                extras.putString(key, value)
            }

            val info = CleverTapAPI.getNotificationInfo(extras)
            if (info.fromCleverTap) {
                CTFcmMessageHandler().createNotification(applicationContext, message)
            } else {
                // Not from CleverTap or Plotline - handle your own push here.
                Log.d(TAG, "Received non-CleverTap/Plotline push: $data")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error parsing FCM message", t)
        }
    }

    companion object {
        private const val TAG = "MyFcmMessagingService"
    }
}
