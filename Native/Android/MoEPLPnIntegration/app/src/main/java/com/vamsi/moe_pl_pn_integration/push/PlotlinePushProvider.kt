package com.vamsi.moe_pl_pn_integration.push

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import so.plotline.insights.PlotlinePush

/**
 * Plotline push provider.
 *
 * Detection and rendering delegate to Plotline's own APIs. The Plotline SDK
 * merges no receiver/service, so it depends entirely on the host app feeding
 * payloads and the FCM token to it.
 */
class PlotlinePushProvider : PushProvider {

    override val name: String = "Plotline"

    override fun canHandle(data: Map<String, String>): Boolean =
        PlotlinePush.isPushPlotline(data)

    override fun handle(context: Context, message: RemoteMessage) {
        PlotlinePush.showNotification(context, message.data)
    }

    override fun onNewToken(context: Context, token: String) {
        Log.d("Token Called" + "PL", token)
        PlotlinePush.setFcmToken(context, token)
    }
}