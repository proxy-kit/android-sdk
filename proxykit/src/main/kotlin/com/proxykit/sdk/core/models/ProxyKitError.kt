package com.proxykit.sdk.core.models

/**
 * Errors that can occur when using ProxyKit SDK
 */
sealed class ProxyKitError : Exception() {
    
    /**
     * SDK is not configured
     */
    object NotConfigured : ProxyKitError() {
        override val message = "ProxyKit SDK is not configured. Call ProxyKit.configure() first."
    }
    
    /**
     * Configuration error
     */
    data class ConfigurationError(override val message: String) : ProxyKitError()
    
    /**
     * Device attestation failed
     */
    data class AttestationFailed(val reason: String) : ProxyKitError() {
        override val message = "Device attestation failed: $reason"
    }
    
    /**
     * Session expired and needs renewal
     */
    object SessionExpired : ProxyKitError() {
        override val message = "Session expired. Please try again."
    }
    
    /**
     * Rate limit exceeded
     */
    data class RateLimited(val retryAfter: Int) : ProxyKitError() {
        override val message = "Rate limit exceeded. Retry after $retryAfter seconds."
    }
    
    /**
     * Network error
     */
    data class NetworkError(val error: Throwable) : ProxyKitError() {
        override val message = "Network error: ${error.message}"
    }
    
    /**
     * Provider-specific error (OpenAI, Anthropic, etc.)
     */
    data class ProviderError(val code: String, val providerMessage: String) : ProxyKitError() {
        override val message = "Provider error ($code): $providerMessage"
    }
    
    /**
     * Stream was interrupted
     */
    object StreamInterrupted : ProxyKitError() {
        override val message = "Stream was interrupted"
    }
    
    /**
     * Invalid response from server
     */
    data class InvalidResponse(val details: String) : ProxyKitError() {
        override val message = "Invalid response: $details"
    }
}