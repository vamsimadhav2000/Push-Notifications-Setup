package com.vamsi.moe_pl_pn_integration

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.moengage.core.Properties
import com.moengage.core.analytics.MoEAnalyticsHelper
import com.moengage.pushbase.MoEPushHelper
import com.vamsi.moe_pl_pn_integration.deeplink.DeepLinkModalManager
import com.vamsi.moe_pl_pn_integration.ui.theme.MoEPLPnIntegrationTheme
import org.json.JSONObject
import so.plotline.insights.Listeners.PlotlineEventsListener
import so.plotline.insights.Listeners.PlotlineRedirectListener
import so.plotline.insights.Plotline
import so.plotline.insights.PlotlinePush
import so.plotline.insights.PlotlineWidget

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupMoEngageUser()
        setupPlotline()
        requestPushPermission()
        requestExactAlarmPermission()
        handleDeepLink(intent)

        setContent {
            MoEPLPnIntegrationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onTrackMoEngageEvent = {
                            MoEAnalyticsHelper.trackEvent(
                                this,
                                "MoE Test Event",
                                Properties().addAttribute("category", "testing")
                            )
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
    }

    /**
     * Handles a deep link launched from a MoEngage (or Plotline) push
     * notification. Reads the URI from the incoming ACTION_VIEW intent.
     */
    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        Log.d(TAG, "Deep link received: $uri")
        // TODO: navigate based on uri.host / uri.path here.
    }

    private fun setupMoEngageUser() {
        // MoEngage: uniquely identify the user and attach attributes so that
        // campaigns, cohorts and filters can target this user.
        MoEAnalyticsHelper.identifyUser(
            this,
            mapOf(
                "email" to BuildConfig.MOE_USER_EMAIL,
                "id" to BuildConfig.MOE_USER_ID
            )
        )
        MoEAnalyticsHelper.setUserAttribute(
            this,
            mapOf(
                "Name" to BuildConfig.MOE_USER_NAME,
                "Identity" to BuildConfig.MOE_USER_ID,
                "Email" to BuildConfig.MOE_USER_EMAIL
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

        // Plotline: forward campaign interaction events to MoEngage.
        Plotline.setPlotlineEventsListener(object : PlotlineEventsListener {
            override fun onEvent(eventName: String, properties: JSONObject) {
                Log.d(TAG, "Plotline event: $eventName, properties: $properties")
                MoEAnalyticsHelper.trackEvent(this@MainActivity, eventName, properties.toMoEProperties())
            }
        })
    }

    private fun requestPushPermission() {
        // Runtime push permission is required on Android 13+. We request it via
        // the OS dialog, then inform both SDKs so they can optimize re-asks and
        // track push opt-in state.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pushPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val pushPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            MoEPushHelper.getInstance().pushPermissionResponse(this, granted)
            PlotlinePush.setPushPermissionGranted(this, granted)
            PlotlinePush.setPushPermissionCount(this, 1)
        }

    private fun requestExactAlarmPermission() {
        // Plotline "Timer with Progress Bar" push template updates its progress
        // using exact alarms. On Android 12+ the permission is declared in the
        // manifest; on Android 14+ it is off by default, so request it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }

    private fun JSONObject.toMoEProperties(): Properties {
        val properties = Properties()
        keys().forEach { key ->
            val value = get(key)
            if (value != null && value != JSONObject.NULL) {
                properties.addAttribute(key, value)
            }
        }
        return properties
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onTrackMoEngageEvent: () -> Unit = {},
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
        Text(text = "MoEngage + Plotline Integration")

        Button(onClick = onTrackMoEngageEvent) {
            Text("Track MoEngage Event")
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
    MoEPLPnIntegrationTheme {
        HomeScreen()
    }
}