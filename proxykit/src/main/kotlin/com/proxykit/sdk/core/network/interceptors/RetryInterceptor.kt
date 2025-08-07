package com.proxykit.sdk.core.network.interceptors

import com.proxykit.sdk.core.utils.Logger
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Handles automatic retries for failed requests
 */
internal class RetryInterceptor(
    private val maxRetries: Int = 3
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val response = chain.proceed(request)
                
                // Don't retry on client errors (4xx)
                if (response.code in 400..499) {
                    return response
                }
                
                // Retry on server errors (5xx) or network issues
                if (response.isSuccessful) {
                    return response
                }
                
                Logger.debug("Request failed with ${response.code}, attempt ${attempt + 1}/$maxRetries")
                response.close()
                
            } catch (e: IOException) {
                lastException = e
                Logger.debug("Request failed with IOException, attempt ${attempt + 1}/$maxRetries", e)
            }
            
            // Wait before retry (exponential backoff)
            if (attempt < maxRetries - 1) {
                Thread.sleep((attempt + 1) * 1000L)
            }
        }
        
        throw lastException ?: IOException("Request failed after $maxRetries attempts")
    }
}