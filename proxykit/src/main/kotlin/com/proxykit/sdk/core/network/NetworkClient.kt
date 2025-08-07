package com.proxykit.sdk.core.network

import com.proxykit.sdk.core.Configuration
import com.proxykit.sdk.core.attestation.SessionManager
import com.proxykit.sdk.core.models.ProxyKitError
import com.proxykit.sdk.core.network.interceptors.AuthInterceptor
import com.proxykit.sdk.core.network.interceptors.LoggingInterceptor
import com.proxykit.sdk.core.network.interceptors.RetryInterceptor
import com.proxykit.sdk.core.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCancellableCoroutine

/**
 * Network client for API communication
 */
internal class NetworkClient(
    private val configuration: Configuration,
    private val sessionManager: SessionManager
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(configuration.timeout, TimeUnit.MILLISECONDS)
        .readTimeout(configuration.timeout, TimeUnit.MILLISECONDS)
        .writeTimeout(configuration.timeout, TimeUnit.MILLISECONDS)
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(AuthInterceptor(sessionManager))
        .apply {
            if (configuration.enableRetry) {
                addInterceptor(RetryInterceptor(configuration.maxRetries))
            }
        }
        .build()
    
    /**
     * Make a GET request
     */
    suspend inline fun <reified T> get(
        path: String,
        headers: Map<String, String> = emptyMap()
    ): T {
        val request = Request.Builder()
            .url("${configuration.baseUrl}$path")
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        
        return executeRequest(request)
    }
    
    /**
     * Make a POST request
     */
    suspend inline fun <reified T> post(
        path: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): T {
        val jsonBody = json.encodeToString(body)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("${configuration.baseUrl}$path")
            .post(requestBody)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        
        return executeRequest(request)
    }
    
    /**
     * Stream response using Server-Sent Events
     */
    suspend inline fun <reified T> stream(
        path: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): Flow<T> = flow {
        val jsonBody = json.encodeToString(body)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("${configuration.baseUrl}$path")
            .post(requestBody)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
                addHeader("Accept", "text/event-stream")
            }
            .build()
        
        suspendCancellableCoroutine<Unit> { continuation ->
            val eventSource = EventSources.createFactory(client)
                .newEventSource(request, object : EventSourceListener() {
                    override fun onEvent(
                        eventSource: EventSource,
                        id: String?,
                        type: String?,
                        data: String
                    ) {
                        try {
                            if (data == "[DONE]") {
                                eventSource.cancel()
                                continuation.resume(Unit)
                                return
                            }
                            
                            val chunk = json.decodeFromString<T>(data)
                            // Emit the chunk
                            // Note: In real implementation, we'd use a channel here
                            Logger.debug("Stream chunk: $data")
                        } catch (e: Exception) {
                            Logger.error("Error parsing stream chunk", e)
                        }
                    }
                    
                    override fun onFailure(
                        eventSource: EventSource,
                        t: Throwable?,
                        response: Response?
                    ) {
                        eventSource.cancel()
                        continuation.resumeWithException(
                            t ?: ProxyKitError.NetworkError(IOException("Stream failed"))
                        )
                    }
                })
            
            continuation.invokeOnCancellation {
                eventSource.cancel()
            }
        }
    }
    
    private suspend inline fun <reified T> executeRequest(request: Request): T {
        return suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)
            
            continuation.invokeOnCancellation {
                call.cancel()
            }
            
            try {
                val response = call.execute()
                
                if (!response.isSuccessful) {
                    val error = handleErrorResponse(response)
                    continuation.resumeWithException(error)
                    return@suspendCancellableCoroutine
                }
                
                val body = response.body?.string() ?: ""
                val result = when (T::class) {
                    Unit::class -> Unit as T
                    String::class -> body as T
                    else -> json.decodeFromString<T>(body)
                }
                
                continuation.resume(result)
                
            } catch (e: IOException) {
                continuation.resumeWithException(ProxyKitError.NetworkError(e))
            } catch (e: Exception) {
                continuation.resumeWithException(
                    ProxyKitError.InvalidResponse(e.message ?: "Unknown error")
                )
            }
        }
    }
    
    private fun handleErrorResponse(response: Response): ProxyKitError {
        return when (response.code) {
            401 -> ProxyKitError.SessionExpired
            429 -> {
                val retryAfter = response.header("Retry-After")?.toIntOrNull() ?: 60
                ProxyKitError.RateLimited(retryAfter)
            }
            in 400..499 -> {
                val errorBody = response.body?.string() ?: ""
                ProxyKitError.ProviderError(
                    code = response.code.toString(),
                    providerMessage = errorBody
                )
            }
            else -> ProxyKitError.NetworkError(
                IOException("HTTP ${response.code}: ${response.message}")
            )
        }
    }
}