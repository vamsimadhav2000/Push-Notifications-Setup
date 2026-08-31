package com.vamsi.ct_pl_pnintegration

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.messaging.FirebaseMessaging
import com.vamsi.ct_pl_pnintegration.push.PushProviderRegistry
import so.plotline.insights.Activities.PlotlineNotificationListener
import so.plotline.insights.Models.PlotlineNotificationConfig
import so.plotline.insights.Plotline
import so.plotline.insights.PlotlinePush

class CTPLApplication : Application() {

    override fun onCreate() {
        // CleverTap: register activity lifecycle callbacks BEFORE super.onCreate()
        // so that app launches, in-app notifications etc. are tracked correctly.
        ActivityLifecycleCallback.register(this)
        super.onCreate()

        setupCleverTap()
        setupPlotline()
        syncFcmToken()
    }

    private fun setupCleverTap() {
        // Rich media push: route notification rendering through the Push Templates SDK
        // (carousel, timer, text-over-image, five icons, rating, product catalog, etc.)
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

    /**
     * Passes the current FCM token to every registered push provider.
     * Token refreshes are delivered to [push.MyFcmMessagingService.onNewToken],
     * which fans them out through the same registry.
     */
    @Suppress("DEPRECATION") // token/getToken() deprecated in favor of FID; CleverTap/Plotline still require the classic token
    private fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            PushProviderRegistry.default.onNewToken(applicationContext, task.result)
        }
    }

    companion object {
        private const val TAG = "CTPLApplication"
    }
}
