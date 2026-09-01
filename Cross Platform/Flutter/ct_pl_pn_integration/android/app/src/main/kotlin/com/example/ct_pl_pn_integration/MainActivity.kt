package com.example.ct_pl_pn_integration

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.messaging.FirebaseMessaging
import io.flutter.embedding.android.FlutterFragmentActivity
import so.plotline.plotline.PlotlinePlugin

// FlutterFragmentActivity supports CleverTap Header & Footer InApp Notification Templates.
class MainActivity : FlutterFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Plotline push: notification small icon + initial FCM token sync.
        PlotlinePlugin.setNotificationMetadata(this, R.drawable.small_icon)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
                PlotlinePlugin.setFcmToken(this, task.result)
            }
    }

    // Android 12+ notification trampoline restrictions mean CleverTap only learns about
    // a push click through ActivityLifecycleCallback.onActivityCreated (i.e. when the
    // activity is recreated). The tap PendingIntent uses FLAG_ACTIVITY_SINGLE_TOP and
    // this activity is launchMode="singleTop" (required by Plotline), so an alive-state
    // (foreground/background) tap lands here via onNewIntent without recreating the
    // activity. Inform the SDK of the click so the Dart pushClickedPayloadReceived
    // handler fires. See: developer.clevertap.com/docs/flutter-push-notification
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent.extras?.let { extras ->
                CleverTapAPI.getDefaultInstance(this)?.pushNotificationClickedEvent(extras)
            }
        }
    }
}