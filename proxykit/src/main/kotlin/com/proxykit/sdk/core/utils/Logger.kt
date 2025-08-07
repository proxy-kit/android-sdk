package com.proxykit.sdk.core.utils

import android.util.Log
import com.proxykit.sdk.core.Configuration

/**
 * Internal logger for SDK
 */
object Logger {
    private const val TAG = "ProxyKit"
    private var logLevel: Configuration.LogLevel = Configuration.LogLevel.ERROR
    
    fun configure(level: Configuration.LogLevel) {
        logLevel = level
    }
    
    fun debug(message: String, throwable: Throwable? = null) {
        if (logLevel >= Configuration.LogLevel.DEBUG) {
            Log.d(TAG, message, throwable)
        }
    }
    
    fun info(message: String, throwable: Throwable? = null) {
        if (logLevel >= Configuration.LogLevel.INFO) {
            Log.i(TAG, message, throwable)
        }
    }
    
    fun error(message: String, throwable: Throwable? = null) {
        if (logLevel >= Configuration.LogLevel.ERROR) {
            Log.e(TAG, message, throwable)
        }
    }
    
    private operator fun Configuration.LogLevel.compareTo(other: Configuration.LogLevel): Int {
        return this.ordinal.compareTo(other.ordinal)
    }
}