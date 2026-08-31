package com.vamsi.ct_pl_pnintegration.push

import android.content.Context
import android.os.Bundle
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler
import com.google.firebase.messaging.RemoteMessage

/**
 * CleverTap push provider.
 *
 * Rendering (including rich media push templates) and token sync are delegated
 * to the CleverTap SDK handlers, so CleverTap stays "auto-integrated" while
 * remaining routable alongside other FCM providers.
 */
class CleverTapPushProvider : PushProvider {

    override val name: String = "CleverTap"

    override fun canHandle(data: Map<String, String>): Boolean {
        val extras = Bundle()
        for ((key, value) in data) {
            extras.putString(key, value)
        }
        return CleverTapAPI.getNotificationInfo(extras).fromCleverTap
    }

    override fun handle(context: Context, message: RemoteMessage) {
        CTFcmMessageHandler().createNotification(context, message)
    }

    override fun onNewToken(context: Context, token: String) {
        CleverTapAPI.getDefaultInstance(context)?.pushFcmRegistrationId(token, true)
    }
}