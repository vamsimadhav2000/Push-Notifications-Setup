package com.vamsi.ct_pl_pnintegration

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.clevertap.android.sdk.CleverTapAPI
import com.vamsi.ct_pl_pnintegration.deeplink.DeepLinkModalManager
import com.vamsi.ct_pl_pnintegration.ui.theme.CTPLPnIntegrationTheme
import org.json.JSONObject
import so.plotline.insights.Listeners.PlotlineEventsListener
import so.plotline.insights.Listeners.PlotlineRedirectListener
import so.plotline.insights.Plotline
import so.plotline.insights.PlotlinePush
import so.plotline.insights.PlotlineWidget

class MainActivity : ComponentActivity() {

    private var cleverTapAPI: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // CleverTap: default instance tied to the credentials in AndroidManifest.
        cleverTapAPI = CleverTapAPI.getDefaultInstance(applicationContext)
        identifyCleverTapUser()

        setupPlotline()
        requestPushPermission()
        handleDeepLink(intent)
        handlePushExtras(intent)

        setContent {
            CTPLPnIntegrationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onTrackCleverTapEvent = {
                            cleverTapAPI?.pushEvent("CT Test Event")
                        },
                        onTrackPlotlineEvent = {
                            Plotline.track(
                                "Plotline Test Event",
                                JSONObject().put("category", "testing")
                            )
                        }
                    )
                }
            }
        }
    }

    // Called when MainActivity is already running (singleTop) and a new
    // deep link arrives while the activity is on top.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
        handlePushExtras(intent)
    }

    /**
     * Handles a deep link launched from a CleverTap (or Plotline) push
     * notification. Reads the URI from the incoming ACTION_VIEW intent.
     */
    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        Log.d(TAG, "Deep link received: $uri")
        // TODO: navigate based on uri.host / uri.path here.
    }

    /**
     * CleverTap delivers the full push payload as intent extras on notification
     * tap (cold start in onCreate, warm start in onNewIntent). If the payload is
     * from CleverTap and carries a "deeplink" custom key-value pair, surface it
     * to the UI as a modal.
     */
    private fun handlePushExtras(intent: Intent?) {
        val extras = intent?.extras ?: return
        if (CleverTapAPI.getNotificationInfo(extras).fromCleverTap) {
            extras.getString(DEEP_LINK_KEY)?.let {
                Log.d(TAG, "CleverTap deeplink received: $it")
                DeepLinkModalManager.present("CleverTap", it)
            }
        }
    }

    private fun identifyCleverTapUser() {
        // CleverTap: onUserLogin creates/merges the profile keyed by Identity,
        // then attributes all subsequent events on this device to that user.
        cleverTapAPI?.onUserLogin(
            mapOf<String, Any>(
                "Name" to BuildConfig.CLEVERTAP_USER_NAME,
                "Identity" to BuildConfig.CLEVERTAP_USER_IDENTITY,
                "Email" to BuildConfig.CLEVERTAP_USER_EMAIL
            )
        )
    }

    private fun setupPlotline() {
        // Plotline: init once per app open. An Activity context is strongly
        // recommended so that campaigns can attach to the current window.
        // API key and user id come from BuildConfig (see app/build.gradle.kts).
        Plotline.init(this, BuildConfig.PLOTLINE_API_KEY, BuildConfig.PLOTLINE_USER_ID)

        // Plotline: user attributes for cohort filtering.
        Plotline.identify(
            JSONObject()
                .put("subscription", "paid")
                .put("plan", "pro")
        )

        // Plotline: handle key/value pairs configured on dashboard buttons.
        Plotline.setPlotlineRedirectListener(object : PlotlineRedirectListener {
            override fun onPlotlineRedirect(keyValuePairs: HashMap<String, String>) {
                Log.d(TAG, "Plotline redirect: $keyValuePairs")
                // Navigate the user based on the key/value pairs here.
            }
        })

        // Plotline: forward campaign interaction events to CleverTap.
        Plotline.setPlotlineEventsListener(object : PlotlineEventsListener {
            override fun onEvent(eventName: String, properties: JSONObject) {
                Log.d(TAG, "Plotline event: $eventName, properties: $properties")
                cleverTapAPI?.pushEvent(eventName, properties.toMap())
            }
        })
    }

    private fun requestPushPermission() {
        // Runtime push permission is required on Android 13+.
        // Plotline's helper shows the system dialog and optimizes re-asks.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PlotlinePush.requestPushPermission(this)
        }
    }

    private fun JSONObject.toMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        keys().forEach { key -> map[key] = get(key) }
        return map
    }

    companion object {
        private const val TAG = "MainActivity"

        /** Custom key-value pair set on push campaigns to carry the deeplink. */
        private const val DEEP_LINK_KEY = "deeplink"
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onTrackCleverTapEvent: () -> Unit = {},
    onTrackPlotlineEvent: () -> Unit = {}
) {
    val deepLink by DeepLinkModalManager.deepLink.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "CleverTap + Plotline Integration")

        Button(onClick = onTrackCleverTapEvent) {
            Text("Track CleverTap Event")
        }

        Button(onClick = onTrackPlotlineEvent) {
            Text("Track Plotline Event")
        }

        // Plotline widget placeholder: embeds dynamic targeted content.
        // Width must be match_parent and height wrap_content.
        AndroidView(
            factory = { context -> PlotlineWidget(context) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }

    // Modal shown when a push notification carries deeplink data.
    deepLink?.let { data ->
        AlertDialog(
            onDismissRequest = { DeepLinkModalManager.dismiss() },
            title = { Text("Deeplink from ${data.source}") },
            text = { Text(data.deepLink) },
            confirmButton = {
                TextButton(onClick = { DeepLinkModalManager.dismiss() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CTPLPnIntegrationTheme {
        HomeScreen()
    }
}
