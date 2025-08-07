package com.proxykit.sdk.core

/**
 * SDK Configuration
 */
data class Configuration(
    val appId: String,
    val baseUrl: String = "https://api.secureapikey.com",
    val environment: Environment = Environment.PRODUCTION,
    val logLevel: LogLevel = LogLevel.ERROR,
    val timeout: Long = 30_000, // 30 seconds
    val enableRetry: Boolean = true,
    val maxRetries: Int = 3
) {
    /**
     * Environment types
     */
    enum class Environment {
        PRODUCTION,
        DEVELOPMENT
    }
    
    /**
     * Log levels
     */
    enum class LogLevel {
        NONE,
        ERROR,
        INFO,
        DEBUG
    }
    
    /**
     * Builder for configuration
     */
    class Builder {
        private var appId: String? = null
        private var baseUrl: String = "https://api.secureapikey.com"
        private var environment: Environment = Environment.PRODUCTION
        private var logLevel: LogLevel = LogLevel.ERROR
        private var timeout: Long = 30_000
        private var enableRetry: Boolean = true
        private var maxRetries: Int = 3
        
        fun withAppId(appId: String) = apply {
            this.appId = appId
        }
        
        fun withBaseUrl(url: String) = apply {
            this.baseUrl = url
        }
        
        fun withEnvironment(env: Environment) = apply {
            this.environment = env
        }
        
        fun withLogLevel(level: LogLevel) = apply {
            this.logLevel = level
        }
        
        fun withTimeout(timeoutMs: Long) = apply {
            this.timeout = timeoutMs
        }
        
        fun withRetry(enable: Boolean, maxRetries: Int = 3) = apply {
            this.enableRetry = enable
            this.maxRetries = maxRetries
        }
        
        @Throws(IllegalArgumentException::class)
        fun build(): Configuration {
            val appId = this.appId
                ?: throw IllegalArgumentException("App ID is required")
            
            if (!appId.startsWith("app_")) {
                throw IllegalArgumentException("Invalid app ID format")
            }
            
            return Configuration(
                appId = appId,
                baseUrl = baseUrl,
                environment = environment,
                logLevel = logLevel,
                timeout = timeout,
                enableRetry = enableRetry,
                maxRetries = maxRetries
            )
        }
    }
}