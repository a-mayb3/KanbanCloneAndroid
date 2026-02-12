package com.campusaula.edbole.kanban_clone_android.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    @Volatile
    private var retrofit: Retrofit? = null
    private var cookieJar: AuthCookieJar? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun getRetrofit(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(context.applicationContext).also { retrofit = it }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        cookieJar = AuthCookieJar(context)

        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar!!)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Helper: obtiene el valor de la cookie de autenticación para una URL. */
    fun getAuthCookieForUrl(url: String): String? {
        return cookieJar?.getAuthCookieForUrl(url)
    }

    fun getCookieHeaderForUrl(url: String): String? {
        return cookieJar?.getCookieHeaderForUrl(url)
    }

    fun clearCookiesForHost(host: String) {
        cookieJar?.clearCookiesForHost(host)
    }

    fun cookieJarInstance(): AuthCookieJar? = cookieJar
}
