package com.vamsi.ct_pl_pnintegration.deeplink

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Holds the deeplink data received from a push notification tap and exposes it
 * to the UI so a modal can be shown. CleverTap and Plotline both deliver their
 * deeplink payload here (see CTPLApplication and MainActivity).
 */
object DeepLinkModalManager {

    data class DeepLinkData(
        val source: String,
        val deepLink: String
    )

    val deepLink = MutableStateFlow<DeepLinkData?>(null)

    fun present(source: String, deepLink: String) {
        this.deepLink.value = DeepLinkData(source, deepLink)
    }

    fun dismiss() {
        deepLink.value = null
    }
}