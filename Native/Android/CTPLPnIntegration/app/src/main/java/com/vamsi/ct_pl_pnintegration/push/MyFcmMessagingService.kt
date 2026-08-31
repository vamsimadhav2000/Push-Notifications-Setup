package com.vamsi.ct_pl_pnintegration.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Single FCM entry point that routes payloads to the registered push providers
 * (currently Plotline, then CleverTap) via [PushProviderRegistry.default].
 *
 * Add another provider by implementing [PushProvider] and registering it in
 * [PushProviderRegistry.default] - no changes are needed here.
 */
class MyFcmMessagingService : FirebaseMessagingService() {

    // onNewToken is deprecated in firebase-messaging 25.x in favor of the
    // FID-based onRegistered(). We intentionally keep onNewToken because
    // CleverTap (pushFcmRegistrationId) and Plotline (setFcmToken) still
    // require the classic FCM registration token, not the Firebase Installation ID.
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushProviderRegistry.default.onNewToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        PushProviderRegistry.default.handle(applicationContext, message)
    }
}