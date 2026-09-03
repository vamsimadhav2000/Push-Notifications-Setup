package com.clevertapplotlinesample

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.clevertap.react.CleverTapRnAPI
import com.clevertap.android.sdk.CleverTapAPI
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "CleverTapPlotlineSample"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // CleverTap: notify of a launch deep link.
    CleverTapRnAPI.setInitialUri(intent?.data)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    // CleverTap: on Android 12+ notification trampoline restrictions block the
    // push-click callback from a service; raise it from the launcher activity instead.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      CleverTapAPI.getDefaultInstance(applicationContext)
        ?.pushNotificationClickedEvent(intent.extras)
    }
  }

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
}