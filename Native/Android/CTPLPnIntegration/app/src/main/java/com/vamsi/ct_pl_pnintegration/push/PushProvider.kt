package com.vamsi.ct_pl_pnintegration.push

import android.content.Context
import com.google.firebase.messaging.RemoteMessage

/**
 * A push notification provider integrated behind the single FCM entry point.
 *
 * Implement this interface for each provider (e.g. CleverTap, Plotline, MoEngage)
 * and register it in [PushProviderRegistry.default] so incoming messages and
 * FCM token refreshes are routed to the right SDKs.
 */
interface PushProvider {

    /** Human-readable provider name, used for logging. */
    val name: String

    /**
     * Returns true when [data] belongs to this provider.
     * Called for every incoming payload until the first provider returns true.
     */
    fun canHandle(data: Map<String, String>): Boolean

    /** Renders/processes a push message already identified as belonging to this provider. */
    fun handle(context: Context, message: RemoteMessage)

    /** Called whenever the FCM registration token is (re)generated. */
    fun onNewToken(context: Context, token: String)
}