package com.joseluiscruz.capturainventario.util

import java.net.URI

/** Normaliza y valida la liga configurable del portal SAP. */
object SapPortalUrlValidator {

    fun normalize(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return null

        val candidate = if (trimmed.contains("://")) trimmed else "https://$trimmed"
        val uri = runCatching { URI(candidate) }.getOrNull() ?: return null

        return if (
            uri.scheme.equals("https", ignoreCase = true) &&
            !uri.host.isNullOrBlank() &&
            uri.userInfo == null
        ) {
            uri.toASCIIString()
        } else {
            null
        }
    }
}
