package com.vamsi.ct_pl_pnintegration

import android.app.NotificationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.Application as CleverTapApplication
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.messaging.FirebaseMessaging
import com.vamsi.ct_pl_pnintegration.deeplink.DeepLinkModalManager
import com.vamsi.ct_pl_pnintegration.push.PushProviderRegistry
import so.plotline.insights.Activities.PlotlineNotificationListener
import so.plotline.insights.Models.PlotlineNotificationConfig
import so.plotline.insights.Plotline
import so.plotline.insights.PlotlinePush

/**
 * App entry point.
 *  - Extends the CleverTap SDK [CleverTapApplication], which registers the
 *    activity lifecycle callbacks automatically (no manual register() call).
 *  - FCM push routing (CleverTap + Plotline) is handled by the router service
 *    declared in AndroidManifest.xml (see [PushProviderRegistry]).
 *  - Syncs the FCM token to every push provider on launch so the token is
 *    always registered, even if FCM never fires onNewToken (unchanged token).
 */
class CTPLApplication : CleverTapApplication() {

    override fun onCreate() {
        super.onCreate()

        setupCleverTap()
        setupPlotline()
        syncFcmToken()
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
        // If a "deeplink" key is present, surface it to the UI as a modal.
        PlotlinePush.setPlotlineNotificationClickListener(
            object : PlotlineNotificationListener {
                override fun onNotificationClickedPayloadReceived(customData: org.json.JSONObject) {
                    Log.d(TAG, "Plotline push clicked, customData: $customData")
                    customData.optString(DEEP_LINK_KEY).takeIf { it.isNotBlank() }?.let {
                        DeepLinkModalManager.present("Plotline", it)
                    }
                }
            }
        )
    }

    /**
     * Fetches the current FCM token and fans it out to every registered push
     * provider via [PushProviderRegistry]. Runs on every app launch so the
     * token is always registered, independent of whether Firebase delivers a
     * new-token callback.
     *
     * On a fresh install the token is generated asynchronously (Firebase
     * Installations registration) and can be slow or fail once (e.g. no
     * network), so a failed fetch is retried a few times instead of being a
     * one-shot that silently loses the token.
     */
    @Suppress("DEPRECATION") // token/getToken() deprecated in favor of FID; CleverTap/Plotline still require the classic token
    private fun syncFcmToken() {
        fetchFcmToken(attempt = 0)
    }

    private fun fetchFcmToken(attempt: Int) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM token fetched (${token.length} chars): $token")
                PushProviderRegistry.onNewToken(applicationContext, token)
                return@addOnCompleteListener
            }

            Log.w(
                TAG,
                "Fetching FCM token failed (attempt ${attempt + 1}/$MAX_TOKEN_FETCH_ATTEMPTS)",
                task.exception
            )
            if (attempt + 1 < MAX_TOKEN_FETCH_ATTEMPTS) {
                mainHandler.postDelayed({ fetchFcmToken(attempt + 1) }, TOKEN_RETRY_DELAY_MS)
            }
        }
    }

    companion object {
        private const val TAG = "CTPLApplication"

        /** Custom key-value pair set on push campaigns to carry the deeplink. */
        private const val DEEP_LINK_KEY = "deeplink"

        /** How many times the initial FCM token fetch is attempted. */
        private const val MAX_TOKEN_FETCH_ATTEMPTS = 3

        /** Delay between token fetch retries, in milliseconds. */
        private const val TOKEN_RETRY_DELAY_MS = 2000L

        private val mainHandler = Handler(Looper.getMainLooper())
    }
}