package com.vamsi.ct_pl_pnintegration

import android.app.NotificationManager
import android.util.Log
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.Application as CleverTapApplication
import com.clevertap.android.sdk.CleverTapAPI
import so.plotline.insights.Activities.PlotlineNotificationListener
import so.plotline.insights.Models.PlotlineNotificationConfig
import so.plotline.insights.Plotline
import so.plotline.insights.PlotlinePush

/**
 * CleverTap auto-integration (minimal code):
 *  - Extends the CleverTap SDK [CleverTapApplication], which registers the
 *    activity lifecycle callbacks automatically (no manual register() call).
 *  - FCM push rendering and token sync are handled automatically by CleverTap's
 *    own FcmMessageListenerService declared in AndroidManifest.xml.
 */
class CTPLApplication : CleverTapApplication() {

    override fun onCreate() {
        super.onCreate()

        setupCleverTap()
        setupPlotline()
    }

    private fun setupCleverTap() {
        // Rich media push: route notification rendering through the Push Templates SDK
        // (carousel, timer, text-over-image, five icons, rating, product catalog, etc.).
        // PushTemplateNotificationHandler implements NotificationHandler transitively
        // (via ActionButtonClickHandler), so no cast is needed.
        CleverTapAPI.setNotificationHandler(PushTemplateNotificationHandler())
        Log.d(TAG, "CleverTap Push Templates notification handler set")

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)

        // Default notification channel for CleverTap campaigns (Android O+).
        // Use this channel id ("CTGeneral") while creating campaigns on the dashboard.
        CleverTapAPI.createNotificationChannel(
            applicationContext,
            "CTGeneral",
            "General",
            "General notifications",
            NotificationManager.IMPORTANCE_MAX,
            true
        )
    }

    private fun setupPlotline() {
        // Registers activity lifecycle callbacks used to detect activity changes
        // and render Plotline campaigns.
        Plotline.registerApplication(this)

        // Plotline push: notification small icon metadata.
        PlotlinePush.setPlotlineNotificationMetaData(
            applicationContext,
            PlotlineNotificationConfig(R.drawable.ic_notification_small)
        )

        // Plotline push: custom key/value pairs set on the dashboard are delivered here.
        PlotlinePush.setPlotlineNotificationClickListener(
            object : PlotlineNotificationListener {
                override fun onNotificationClickedPayloadReceived(customData: org.json.JSONObject) {
                    Log.d(TAG, "Plotline push clicked, customData: $customData")
                    // Handle navigation based on the key/value pairs here.
                }
            }
        )
    }

    companion object {
        private const val TAG = "CTPLApplication"
    }
}