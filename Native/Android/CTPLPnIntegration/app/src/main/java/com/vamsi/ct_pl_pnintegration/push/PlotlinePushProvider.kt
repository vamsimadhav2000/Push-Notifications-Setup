package com.vamsi.ct_pl_pnintegration.push

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import so.plotline.insights.PlotlinePush

/**
 * Plotline push provider.
 *
 * Rendering and token sync are delegated to the Plotline SDK helpers.
 */
class PlotlinePushProvider : PushProvider {

    override val name: String = "Plotline"

    override fun canHandle(data: Map<String, String>): Boolean =
        PlotlinePush.isPushPlotline(data)

    override fun handle(context: Context, message: RemoteMessage) {
        PlotlinePush.showNotification(context, message.data)
    }

    override fun onNewToken(context: Context, token: String) {
        PlotlinePush.setFcmToken(context, token)
    }
}