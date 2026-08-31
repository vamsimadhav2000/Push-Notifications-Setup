package com.vamsi.ct_pl_pnintegration.push

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage

/**
 * Central registration point for all push providers.
 *
 * First-match routing: providers are asked (in order) whether they own an
 * incoming payload; the first match renders it. Adding a new provider is just
 * adding a class and one entry to [default].
 *
 * CleverTap is checked first so that if a payload is ever dual-tagged with both
 * providers' marker keys, CleverTap (the primary platform) wins.
 */
object PushProviderRegistry {

    val default: List<PushProvider> = listOf(
        CleverTapPushProvider(),
        PlotlinePushProvider()
    )

    /**
     * Routes an incoming FCM message to the first provider that owns it.
     *
     * @return true if a provider claimed the message.
     */
    fun route(context: Context, message: RemoteMessage): Boolean {
        val data = message.data
        for (provider in default) {
            try {
                if (provider.canHandle(data)) {
                    Log.d(TAG, "${provider.name} claimed message")
                    provider.handle(context, message)
                    return true
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Provider ${provider.name} failed for payload", t)
            }
        }
        Log.w(TAG, "No provider claimed message, payload keys: ${data.keys}")
        return false
    }

    /**
     * Fans out a token to every provider. Called from the FCM service on token
     * refresh and from CTPLApplication on app launch so that the token is always
     * registered even when FCM never fires onNewToken (unchanged token).
     */
    fun onNewToken(context: Context, token: String) {
        for (provider in default) {
            try {
                provider.onNewToken(context, token)
                Log.d(TAG, "${provider.name} token sync complete")
            } catch (t: Throwable) {
                Log.w(TAG, "Provider ${provider.name} token sync failed", t)
            }
        }
    }

    private const val TAG = "PushProviderRegistry"
}