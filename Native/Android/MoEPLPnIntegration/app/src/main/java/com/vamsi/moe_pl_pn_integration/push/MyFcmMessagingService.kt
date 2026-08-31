package com.vamsi.moe_pl_pn_integration.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Single FCM service for the app. Delegates every incoming message and token
 * refresh to [PushProviderRegistry], which auto-identifies and routes the
 * payload to the owning provider (MoEngage, Plotline, or any future provider).
 *
 * This replaces MoEngage's MoEFireBaseMessagingService: MoEngagePushProvider
 * internally calls the same MoEFireBaseHelper handlers, so MoEngage behavior is
 * unchanged from its auto-integration.
 */
class MyFcmMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("Data FCM", message.data.toString())
        if (!PushProviderRegistry.route(applicationContext, message)) {
            Log.w(TAG, "Unhandled push payload: ${message.data}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("Token Called" + "FCM", token)
        PushProviderRegistry.onNewToken(applicationContext, token)
    }

    private companion object {
        const val TAG = "MyFcmMessagingService"
    }
}