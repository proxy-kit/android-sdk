package com.proxykit.sdk.core.network.interceptors

import com.proxykit.sdk.core.attestation.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds authentication headers to requests
 */
internal class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for attestation endpoints
        if (originalRequest.url.encodedPath.contains("/attestation/")) {
            return chain.proceed(originalRequest)
        }
        
        val sessionToken = sessionManager.getSessionToken()
        val deviceId = sessionManager.getDeviceId()
        
        val requestBuilder = originalRequest.newBuilder()
        
        if (sessionToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer $sessionToken")
        }
        
        if (deviceId != null) {
            requestBuilder.addHeader("X-Device-ID", deviceId)
        }
        
        return chain.proceed(requestBuilder.build())
    }
}