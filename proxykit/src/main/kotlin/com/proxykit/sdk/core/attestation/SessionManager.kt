package com.proxykit.sdk.core.attestation

import com.proxykit.sdk.core.storage.SecureStorage
import com.proxykit.sdk.core.utils.Logger
import java.util.concurrent.TimeUnit

/**
 * Manages session lifecycle
 */
internal class SessionManager(
    private val storage: SecureStorage
) {
    
    /**
     * Check if we have a valid session
     */
    fun hasValidSession(): Boolean {
        val token = storage.getSessionToken()
        if (token == null) {
            Logger.debug("No session token found")
            return false
        }
        
        val timestamp = storage.getSessionTimestamp()
        val age = System.currentTimeMillis() - timestamp
        val maxAge = TimeUnit.HOURS.toMillis(24) // 24 hour sessions
        
        if (age > maxAge) {
            Logger.debug("Session expired (age: ${age}ms)")
            clearSession()
            return false
        }
        
        Logger.debug("Valid session found (age: ${age}ms)")
        return true
    }
    
    /**
     * Get current session token
     */
    fun getSessionToken(): String? {
        return if (hasValidSession()) {
            storage.getSessionToken()
        } else {
            null
        }
    }
    
    /**
     * Get device ID
     */
    fun getDeviceId(): String? {
        return storage.getDeviceId()
    }
    
    /**
     * Save session data
     */
    fun saveSession(token: String, deviceId: String, publicKey: String) {
        storage.saveSessionToken(token)
        storage.saveDeviceId(deviceId)
        storage.savePublicKey(publicKey)
        Logger.debug("Session saved")
    }
    
    /**
     * Clear session data
     */
    fun clearSession() {
        storage.clearSession()
        Logger.debug("Session cleared")
    }
    
    /**
     * Clear all data
     */
    fun clearAll() {
        storage.clear()
        Logger.debug("All data cleared")
    }
}