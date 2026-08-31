package com.vamsi.ct_pl_pnintegration.push

import android.content.Context
import com.google.firebase.messaging.RemoteMessage

/**
 * A push provider that can identify and handle FCM payloads addressed to it.
 *
 * Providers are checked in registration order by [PushProviderRegistry] using
 * first-match routing: [canHandle] delegates to each SDK's own detection API
 * (no hardcoded marker keys) and [handle] renders the notification for the
 * matched provider.
 */
interface PushProvider {

    /** Human-readable provider name, used in logs. */
    val name: String

    /**
     * True if this provider owns the given payload. Implementations should
     * delegate to the SDK's own detection API, never render here.
     */
    fun canHandle(data: Map<String, String>): Boolean

    /**
     * Renders (or otherwise processes) the incoming message. Only invoked when
     * [canHandle] returned true for the same message.
     */
    fun handle(context: Context, message: RemoteMessage)

    /**
     * Receives the current FCM token, typically used to register it with the
     * provider's backend. Called on token refresh and on app launch (via the
     * registry fan-out in CTPLApplication).
     */
    fun onNewToken(context: Context, token: String)
}