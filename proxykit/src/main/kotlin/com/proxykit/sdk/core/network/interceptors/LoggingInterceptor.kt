package com.proxykit.sdk.core.network.interceptors

import com.proxykit.sdk.core.utils.Logger
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Logs network requests and responses
 */
internal class LoggingInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        Logger.debug("→ ${request.method} ${request.url}")
        request.headers.forEach { (name, value) ->
            if (name.lowercase() != "authorization") {
                Logger.debug("  $name: $value")
            }
        }
        
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val duration = System.currentTimeMillis() - startTime
        
        Logger.debug("← ${response.code} ${request.url} (${duration}ms)")
        
        return response
    }
}