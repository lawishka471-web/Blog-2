package com.example.data.remote

import com.example.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            

        if (!token.isNull_orEmpty()) {
            // requestBuilder.header("Authorization", "Bearer $token")
            requestBuilder.header("Host", "blog_verse_apps.test")
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()
}
