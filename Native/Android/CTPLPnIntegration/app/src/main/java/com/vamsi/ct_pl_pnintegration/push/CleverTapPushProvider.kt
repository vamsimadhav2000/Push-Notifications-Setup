package com.vamsi.ct_pl_pnintegration.push

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler
import com.google.firebase.messaging.RemoteMessage

/**
 * CleverTap push provider.
 *
 * Detection delegates to CleverTap's own [CleverTapAPI.getNotificationInfo];
 * rendering and token sync delegate to [CTFcmMessageHandler] - the exact same
 * handlers CleverTap's auto-integrated FcmMessageListenerService runs - so
 * behavior (rendering incl. Push Templates, token sync, click tracking) is
 * identical to a pure CleverTap auto-integration.
 */
class CleverTapPushProvider : PushProvider {

    override val name: String = "CleverTap"

    private val handler = CTFcmMessageHandler()

    override fun canHandle(data: Map<String, String>): Boolean {
        val extras = Bundle()
        for ((key, value) in data) {
            extras.putString(key, value)
        }
        return CleverTapAPI.getNotificationInfo(extras).fromCleverTap
    }

    override fun handle(context: Context, message: RemoteMessage) {
        handler.createNotification(context, message)
    }

    override fun onNewToken(context: Context, token: String) {
        Log.d("Token Called"+"CT", token)
        handler.onNewToken(context, token)
    }
}