package com.example.ct_pl_pn_integration

import com.clevertap.android.sdk.Application as CleverTapApplication

/**
 * CleverTap auto-integration (minimal code):
 *  - Extends the CleverTap SDK [CleverTapApplication], which registers the
 *    activity lifecycle callbacks automatically (no manual register() call).
 *  - FCM push rendering and token sync are handled automatically by CleverTap's
 *    own FcmMessageListenerService declared in AndroidManifest.xml.
 */
class MainApplication : CleverTapApplication()