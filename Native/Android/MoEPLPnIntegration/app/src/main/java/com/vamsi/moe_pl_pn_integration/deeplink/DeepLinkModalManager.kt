package com.vamsi.moe_pl_pn_integration.deeplink

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Holds the deeplink data received from a push notification tap and exposes it
 * to the UI so a modal can be shown. MoEngage and Plotline both deliver their
 * deeplink payload here (see MoEApplication and MainActivity).
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