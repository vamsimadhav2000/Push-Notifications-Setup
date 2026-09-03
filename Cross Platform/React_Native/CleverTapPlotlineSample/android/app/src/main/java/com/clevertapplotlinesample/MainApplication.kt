package com.clevertapplotlinesample

import android.app.Application
import com.clevertap.react.CleverTapApplication
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CleverTapAPI.LogLevel
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost

class MainApplication : CleverTapApplication(), ReactApplication {

  override val reactHost: ReactHost by lazy {
    getDefaultReactHost(
      context = applicationContext,
      packageList =
        PackageList(this).packages.apply {
          // Packages that cannot be autolinked yet can be added manually here, for example:
          // add(MyReactNativePackage())
        },
    )
  }

  override fun onCreate() {
    // CleverTap: out-of-the-box integration.
    // Calling super first ensures CleverTap is initialized before anything else.
    super.onCreate()

    CleverTapAPI.setDebugLevel(LogLevel.VERBOSE)

    loadReactNative(this)
  }
}