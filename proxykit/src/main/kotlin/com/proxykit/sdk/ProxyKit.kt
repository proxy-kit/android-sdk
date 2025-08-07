package com.proxykit.sdk

import android.content.Context
import com.proxykit.sdk.core.Configuration
import com.proxykit.sdk.core.attestation.AttestationManager
import com.proxykit.sdk.core.attestation.AttestationObserver
import com.proxykit.sdk.core.attestation.AttestationStatus
import com.proxykit.sdk.core.attestation.SessionManager
import com.proxykit.sdk.core.models.ProxyKitError
import com.proxykit.sdk.core.network.NetworkClient
import com.proxykit.sdk.core.providers.AnthropicClient
import com.proxykit.sdk.core.providers.ChatProvider
import com.proxykit.sdk.core.providers.OpenAIClient
import com.proxykit.sdk.core.storage.SecureStorage
import com.proxykit.sdk.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main entry point for ProxyKit SDK
 * Provides direct API access with full control
 */
object ProxyKit {
    private var instance: ProxyKitInstance? = null
    
    /**
     * Configure the SDK
     */
    @JvmStatic
    fun configure(): Configuration.Builder {
        return Configuration.Builder()
    }
    
    /**
     * Initialize with configuration (called by builder)
     */
    internal fun initialize(context: Context, configuration: Configuration) {
        if (instance != null) {
            throw ProxyKitError.ConfigurationError("ProxyKit is already configured")
        }
        
        instance = ProxyKitInstance(context.applicationContext, configuration)
    }
    
    /**
     * OpenAI API access
     */
    @JvmStatic
    val openai: ChatProvider
        get() = getInstance().openAIClient
    
    /**
     * Anthropic API access
     */
    @JvmStatic
    val anthropic: ChatProvider
        get() = getInstance().anthropicClient
    
    /**
     * Reset the SDK (useful for testing)
     */
    @JvmStatic
    fun reset() {
        instance = null
    }
    
    /**
     * Clear session and reset
     */
    @JvmStatic
    suspend fun clearSessionAndReset() {
        instance?.sessionManager?.clearSession()
        instance = null
    }
    
    /**
     * Get current configuration
     */
    @JvmStatic
    val currentConfiguration: Configuration?
        get() = instance?.configuration
    
    /**
     * Check if SDK is configured
     */
    @JvmStatic
    val isConfigured: Boolean
        get() = instance != null
    
    /**
     * Add attestation observer
     */
    @JvmStatic
    fun addAttestationObserver(observer: AttestationObserver) {
        getInstance().attestationManager.addObserver(observer)
    }
    
    /**
     * Remove attestation observer
     */
    @JvmStatic
    fun removeAttestationObserver(observer: AttestationObserver) {
        getInstance().attestationManager.removeObserver(observer)
    }
    
    /**
     * Get current attestation status
     */
    @JvmStatic
    val attestationStatus: AttestationStatus
        get() = instance?.attestationManager?.currentStatus ?: AttestationStatus.NotStarted
    
    private fun getInstance(): ProxyKitInstance {
        return instance ?: throw ProxyKitError.NotConfigured
    }
}

/**
 * Internal instance holder
 */
private class ProxyKitInstance(
    context: Context,
    val configuration: Configuration
) {
    private val storage = SecureStorage(context)
    val sessionManager = SessionManager(storage)
    private val networkClient = NetworkClient(configuration, sessionManager)
    
    val attestationManager = AttestationManager(
        context = context,
        appId = configuration.appId,
        networkClient = networkClient,
        storage = storage,
        sessionManager = sessionManager
    )
    
    val openAIClient = OpenAIClient(networkClient, attestationManager)
    val anthropicClient = AnthropicClient(networkClient, attestationManager)
    
    init {
        Logger.configure(configuration.logLevel)
        Logger.info("ProxyKit initialized with app ID: ${configuration.appId}")
        
        // Perform initial attestation in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                attestationManager.attestIfNeeded()
                Logger.info("Initial attestation completed")
            } catch (e: Exception) {
                Logger.error("Initial attestation failed", e)
                // Don't throw - attestation will be retried on first API call
            }
        }
    }
}

/**
 * Extension to build configuration with context
 */
fun Configuration.Builder.build(context: Context): Configuration {
    val config = build()
    ProxyKit.initialize(context, config)
    return config
}