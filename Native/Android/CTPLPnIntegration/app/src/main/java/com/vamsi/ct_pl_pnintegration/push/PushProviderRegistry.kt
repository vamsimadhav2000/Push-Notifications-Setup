package com.vamsi.ct_pl_pnintegration.push

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage

/**
 * Routes incoming FCM payloads to the first provider that can handle them and
 * fans out FCM token refreshes to every registered provider.
 *
 * To add another push provider: implement [PushProvider], then insert it into
 * the provider list in [default] (before CleverTap if you want it to take
 * precedence for overlapping payloads).
 */
class PushProviderRegistry(private val providers: List<PushProvider>) {

    /** First provider whose canHandle() returns true, or null if none match. */
    private fun findProvider(data: Map<String, String>): PushProvider? {
        for (provider in providers) {
            val handled = runCatching { provider.canHandle(data) }.getOrDefault(false)
            if (handled) return provider
        }
        return null
    }

    fun handle(context: Context, message: RemoteMessage) {
        val data = message.data
        if (data.isEmpty()) return

        val provider = findProvider(data)
        if (provider != null) {
            runCatching { provider.handle(context, message) }
                .onFailure { t -> Log.e(TAG, "${provider.name} failed to handle push", t) }
        } else {
            Log.d(TAG, "Received push not handled by any provider: $data")
        }
    }

    fun onNewToken(context: Context, token: String) {
        for (provider in providers) {
            runCatching { provider.onNewToken(context, token) }
                .onFailure { t -> Log.e(TAG, "${provider.name} failed to handle new token", t) }
        }
    }

    companion object {
        private const val TAG = "PushProviderRegistry"

        /** Shared registry used by the FCM service and the Application class. */
        val default: PushProviderRegistry by lazy {
            PushProviderRegistry(
                listOf(
                    PlotlinePushProvider(),
                    CleverTapPushProvider()
                )
            )
        }
    }
}