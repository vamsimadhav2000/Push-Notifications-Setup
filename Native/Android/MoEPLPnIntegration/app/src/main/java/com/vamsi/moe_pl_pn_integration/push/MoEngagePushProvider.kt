package com.vamsi.moe_pl_pn_integration.push

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper

/**
 * MoEngage push provider.
 *
 * Detection delegates to MoEngage's own [MoEPushHelper.isFromMoEngagePlatform];
 * rendering and token sync delegate to [MoEFireBaseHelper] - the exact same
 * handlers MoEngage's auto-integrated MoEFireBaseMessagingService runs - so
 * behavior (rendering incl. rich media / push templates, token registration,
 * click tracking) is identical to a pure MoEngage auto-integration.
 */
class MoEngagePushProvider : PushProvider {

    override val name: String = "MoEngage"

    override fun canHandle(data: Map<String, String>): Boolean =
        MoEPushHelper.getInstance().isFromMoEngagePlatform(data)

    override fun handle(context: Context, message: RemoteMessage) {
        MoEFireBaseHelper.getInstance().passPushPayload(context, message.data)
    }

    override fun onNewToken(context: Context, token: String) {
        Log.d("Token Called" + "MoE", token)
        MoEFireBaseHelper.getInstance().passPushToken(context, token)
    }
}