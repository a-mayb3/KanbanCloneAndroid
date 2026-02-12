package com.campusaula.edbole.kanban_clone_android.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import com.google.gson.Gson
import androidx.core.content.edit
import java.util.concurrent.ConcurrentHashMap
import android.util.Base64
import com.auth0.android.jwt.JWT


private data class StoredCookie(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean,
    val iat: Long? = null,
    val exp: Long? = null
)

class AuthCookieJar(
    context: Context,
    private val authCookieNames: Set<String> = setOf("access_token", "session", "auth", "auth_token", "JSESSIONID")
) : CookieJar {

    private val prefs = context.applicationContext.getSharedPreferences("auth_cookie_prefs", Context.MODE_PRIVATE)
    private val lock = Any()
    private val gson = Gson()
    private val knownHosts = ConcurrentHashMap.newKeySet<String>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val hostKey = url.host
        synchronized(lock) {
            val existing = prefs.getStringSet(hostKey, emptySet())?.toMutableSet() ?: mutableSetOf()
            val now = System.currentTimeMillis()
            for (cookie in cookies) {
                if (cookie.expiresAt <= now) continue
                existing.removeIf { it.startsWith("${'$'}{cookie.name}|${'$'}{cookie.path}|") }

                // default stored expiresAt comes from cookie.expiresAt (already in ms)
                var storedExpires = cookie.expiresAt
                var iatVal: Long? = null
                var expVal: Long? = null

                // if this is an auth cookie, try to decode JWT payload to obtain exp/iat
                if (cookie.name in authCookieNames) {
                    try {
                        val (expSec, iatSec) = decodeJwtExpIat(cookie.value)
                        if (expSec != null) {
                            expVal = expSec
                            // convert to millis
                            storedExpires = expSec * 1000L
                        }
                        if (iatSec != null) {
                            iatVal = iatSec
                        }
                    } catch (_: Exception) {
                        // ignore, keep cookie.expiresAt
                    }
                }

                val stored = StoredCookie(
                    name = cookie.name,
                    value = cookie.value,
                    expiresAt = storedExpires,
                    domain = cookie.domain,
                    path = cookie.path,
                    secure = cookie.secure,
                    httpOnly = cookie.httpOnly,
                    hostOnly = cookie.hostOnly,
                    iat = iatVal,
                    exp = expVal
                )
                // serialize to json explicitly and use it
                val json = gson.toJson(stored)
                existing.add("${cookie.name}|${cookie.path}|${json}")
            }
            prefs.edit { putStringSet(hostKey, existing) }
            knownHosts.add(hostKey)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val hostKey = url.host
        val result = ArrayList<Cookie>()
        val now = System.currentTimeMillis()
        synchronized(lock) {
            val set = prefs.getStringSet(hostKey, emptySet()) ?: emptySet()
            val newSet = mutableSetOf<String>()
            for (s in set) {
                try {
                    val jsonStart = s.indexOf('{')
                    val json = if (jsonStart >= 0) s.substring(jsonStart) else s
                    val stored = gson.fromJson(json, StoredCookie::class.java)
                    if (stored.expiresAt <= now) continue
                    val builder = Cookie.Builder()
                        .name(stored.name)
                        .value(stored.value)
                        .expiresAt(stored.expiresAt)
                        .path(stored.path)
                    if (stored.hostOnly) builder.hostOnlyDomain(stored.domain) else builder.domain(stored.domain)
                    if (stored.secure) builder.secure()
                    if (stored.httpOnly) builder.httpOnly()
                    val cookie = builder.build()
                    if (cookie.matches(url)) {
                        result.add(cookie)
                    }
                    newSet.add(s)
                } catch (_: Exception) {
                    // skip malformed
                }
            }
            prefs.edit { putStringSet(hostKey, newSet) }
            if (newSet.isNotEmpty()) knownHosts.add(hostKey)
        }
        return result
    }

    fun saveFromSetCookieHeader(setCookieHeader: String, requestUrl: String) {
        val url = requestUrl.toHttpUrlOrNull() ?: return
        val lines = setCookieHeader.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
        val parsed = mutableListOf<Cookie>()
        for (line in lines) {
            Cookie.parse(url, line)?.let { parsed.add(it) }
        }
        if (parsed.isNotEmpty()) saveFromResponse(url, parsed)
    }

    fun getCookieHeaderForUrl(urlString: String): String? {
        val url = urlString.toHttpUrlOrNull() ?: return null
        val cookies = loadForRequest(url)
        if (cookies.isEmpty()) return null
        return cookies.joinToString("; ") { "${'$'}{it.name}=${'$'}{it.value}" }
    }

    fun getAuthCookieForUrl(urlString: String): String? {
        val url = urlString.toHttpUrlOrNull() ?: return null
        val host = url.host
        val set = prefs.getStringSet(host, emptySet()) ?: return null
        val now = System.currentTimeMillis()
        for (s in set) {
            try {
                val jsonStart = s.indexOf('{')
                val json = if (jsonStart >= 0) s.substring(jsonStart) else s
                val stored = gson.fromJson(json, StoredCookie::class.java)
                if (stored.expiresAt <= now) continue
                if (stored.name in authCookieNames) return stored.value
            } catch (_: Exception) {
            }
        }
        return null
    }

    /** Remove all cookies stored for the given host. */
    fun clearCookiesForHost(host: String) {
        synchronized(lock) {
            prefs.edit { remove(host) }
            knownHosts.remove(host)
        }
    }

    private fun decodeJwtExpIat(token: String): Pair<Long?, Long?> {
        return try {
            val jwt = JWT(token)
            // expiresAt / issuedAt -> java.util.Date?
            val expSec = jwt.expiresAt?.time?.div(1000)   // segundos desde epoch
            val iatSec = jwt.issuedAt?.time?.div(1000)
            Pair(expSec, iatSec)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    private fun padBase64(b64: String): String {
        val rem = b64.length % 4
        return if (rem == 0) b64 else b64 + "=".repeat(4 - rem)
    }
}