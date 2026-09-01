package com.vamsi.moe_pl_pn_integration

import android.app.Activity
import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import com.moengage.core.DataCenter
import com.moengage.core.MoEngage
import com.moengage.core.config.NotificationConfig
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper
import com.moengage.pushbase.push.PushMessageListener
import com.vamsi.moe_pl_pn_integration.deeplink.DeepLinkModalManager
import org.json.JSONObject
import so.plotline.insights.Activities.PlotlineNotificationListener
import so.plotline.insights.Models.PlotlineNotificationConfig
import so.plotline.insights.Plotline
import so.plotline.insights.PlotlinePush

/**
 * App entry point.
 *  - Initializes the MoEngage SDK (default instance) with push metadata. MoEngage
 *    handles its own FCM token registration and push rendering via its auto
 *    integration service (com.moengage.firebase.MoEFireBaseMessagingService,
 *    declared in AndroidManifest.xml). See [setupMoEngage].
 *  - Registers Plotline and its push metadata (see [setupPlotline]).
 *  - MoEngage's auto service forwards every non-MoEngage push payload to the
 *    NonMoEngagePushListener registered below, which routes Plotline pushes to
 *    the Plotline SDK. The FCM token is synced to Plotline whenever MoEngage
 *    registers/updates it (TokenAvailableListener) and once on app launch
 *    (MoEFireBaseHelper.getPushToken) to cover installs with an unchanged token.
 */
class MoEApplication : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        setupMoEngage()
        setupPlotline()
        syncPlotlineToken()
    }

    private fun setupMoEngage() {
        // MoEngage core initialization. The workspace id and data center come
        // from BuildConfig (see app/build.gradle.kts / secrets.properties).
        // Token registration is left enabled (default) - MoEngage registers and
        // manages the FCM token itself through its auto-integration service.
        val moEngage = MoEngage.Builder(
            application = this,
            appId = BuildConfig.MOENGAGE_APP_ID,
            dataCenter = dataCenter(BuildConfig.MOENGAGE_DATA_CENTER)
        )
            // Rich media push: small icon (mandatory) + large icon + accent color.
            .configureNotificationMetaData(
                NotificationConfig(
                    smallIcon = R.drawable.ic_notification_small,
                    largeIcon = R.mipmap.ic_launcher,
                    notificationColor = R.color.notification_accent,
                    isMultipleNotificationInDrawerEnabled = true,
                    isBuildingBackStackEnabled = true,
                    isLargeIconDisplayEnabled = true
                )
            )
            .build()
        MoEngage.initialiseDefaultInstance(moEngage)

        // Default notification channels for MoEngage campaigns (Android O+).
        MoEPushHelper.getInstance().setUpNotificationChannels(this)

        // MoEngage's auto-integration service delivers every non-MoEngage push
        // payload here. Route Plotline payloads to the Plotline SDK; log anything
        // else so no provider's push is silently dropped.
        MoEFireBaseHelper.getInstance().addNonMoEngagePushListener { message ->
            if (PlotlinePush.isPushPlotline(message.data)) {
                Log.d(TAG, "Routing non-MoEngage push to Plotline: ${message.data}")
                PlotlinePush.showNotification(applicationContext, message.data)
            } else {
                Log.d(TAG, "Non-MoEngage push (not Plotline), keys: ${message.data.keys}")
            }
        }

        // Whenever MoEngage registers/refreshes the FCM token, mirror it to
        // Plotline so backend-triggered Plotline campaigns reach this device.
        MoEFireBaseHelper.getInstance().addTokenListener { token ->
            Log.d(TAG, "MoEngage FCM token available")
            PlotlinePush.setFcmToken(applicationContext, token.pushToken)
        }

        // MoEngage push click callback: custom key/value pairs configured on a
        // push campaign are delivered here. If a "deeplink" key is present,
        // surface it to the UI as a modal.
        MoEPushHelper.getInstance().registerMessageListener(object : PushMessageListener() {
            override fun onNotificationClick(activity: Activity, payload: Bundle): Boolean {
                Log.d(TAG, "MoEngage push clicked, payload: $payload")
                payload.getString(DEEP_LINK_KEY)?.takeIf { it.isNotBlank() }?.let {
                    DeepLinkModalManager.present("MoEngage", it)
                }
                return false
            }
        })
    }

    private fun setupPlotline() {
        // Registers activity lifecycle callbacks used to detect activity changes
        // and render Plotline campaigns.
        Plotline.registerApplication(this)

        // Plotline push: notification small icon metadata.
        PlotlinePush.setPlotlineNotificationMetaData(
            this,
            PlotlineNotificationConfig(R.drawable.ic_notification_small)
        )

        // Plotline push: custom key/value pairs set on the dashboard are delivered here.
        PlotlinePush.setPlotlineNotificationClickListener(
            object : PlotlineNotificationListener {
                override fun onNotificationClickedPayloadReceived(customData: JSONObject) {
                    Log.d(TAG, "Plotline push clicked, customData: $customData")
                    customData.optString(DEEP_LINK_KEY).takeIf { it.isNotBlank() }?.let {
                        DeepLinkModalManager.present("Plotline", it)
                    }
                }
            }
        )
    }

    private fun dataCenter(code: String): DataCenter = when (code) {
        "2" -> DataCenter.DATA_CENTER_2
        "3" -> DataCenter.DATA_CENTER_3
        "4" -> DataCenter.DATA_CENTER_4
        "5" -> DataCenter.DATA_CENTER_5
        "6" -> DataCenter.DATA_CENTER_6
        else -> DataCenter.DATA_CENTER_1
    }

    /**
     * MoEngage manages the FCM token itself. On app launch the token may already
     * exist and unchanged, in which case TokenAvailableListener (registered in
     * [setupMoEngage]) does not fire, so read MoEngage's saved token and hand it
     * to Plotline to guarantee the token is always registered.
     */
    private fun syncPlotlineToken() {
        val token = MoEFireBaseHelper.getInstance().getPushToken(this)
        if (!token.isNullOrBlank()) {
            Log.d(TAG, "Syncing existing FCM token to Plotline")
            PlotlinePush.setFcmToken(this, token)
        }
    }

    companion object {
        private const val TAG = "MoEApplication"

        /** Custom key-value pair set on push campaigns to carry the deeplink. */
        private const val DEEP_LINK_KEY = "deeplink"
    }
}