package com.vamsi.moe_pl_pn_integration

import android.app.Activity
import android.app.NotificationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.moengage.core.DataCenter
import com.moengage.core.MoEngage
import com.moengage.core.config.FcmConfig
import com.moengage.core.config.NotificationConfig
import com.moengage.pushbase.MoEPushHelper
import com.moengage.pushbase.push.PushMessageListener
import com.vamsi.moe_pl_pn_integration.deeplink.DeepLinkModalManager
import com.vamsi.moe_pl_pn_integration.push.PushProviderRegistry
import org.json.JSONObject
import so.plotline.insights.Activities.PlotlineNotificationListener
import so.plotline.insights.Models.PlotlineNotificationConfig
import so.plotline.insights.Plotline
import so.plotline.insights.PlotlinePush

/**
 * App entry point.
 *  - Initializes the MoEngage SDK (default instance) with push metadata and
 *    FCM token registration handled by the app (see [setupMoEngage]).
 *  - Registers Plotline and its push metadata (see [setupPlotline]).
 *  - FCM push routing (MoEngage + Plotline) is handled by the router service
 *    declared in AndroidManifest.xml (see [PushProviderRegistry]).
 *  - Syncs the FCM token to every push provider on launch so the token is
 *    always registered, even if FCM never fires onNewToken (unchanged token).
 */
class MoEApplication : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        setupMoEngage()
        setupPlotline()
        syncFcmToken()
    }

    private fun setupMoEngage() {
        // MoEngage core initialization. The workspace id and data center come
        // from BuildConfig (see app/build.gradle.kts / secrets.properties).
        // configureFcm disables MoEngage's own token registration because our
        // router service handles token refresh and feeds the token to MoEngage
        // via MoEFireBaseHelper.passPushToken (see MoEngagePushProvider).
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
            // App handles FCM token registration + delivery; MoEngage auto
            // token registration is disabled.
            .configureFcm(FcmConfig(isRegistrationEnabled = false))
            .build()
        MoEngage.initialiseDefaultInstance(moEngage)

        // Default notification channels for MoEngage campaigns (Android O+).
        MoEPushHelper.getInstance().setUpNotificationChannels(this)

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
    @Suppress("DEPRECATION") // token/getToken() deprecated in favor of FID; MoEngage/Plotline still require the classic token
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
        private const val TAG = "MoEApplication"

        /** Custom key-value pair set on push campaigns to carry the deeplink. */
        private const val DEEP_LINK_KEY = "deeplink"

        /** How many times the initial FCM token fetch is attempted. */
        private const val MAX_TOKEN_FETCH_ATTEMPTS = 3

        /** Delay between token fetch retries, in milliseconds. */
        private const val TOKEN_RETRY_DELAY_MS = 2000L

        private val mainHandler = Handler(Looper.getMainLooper())
    }
}