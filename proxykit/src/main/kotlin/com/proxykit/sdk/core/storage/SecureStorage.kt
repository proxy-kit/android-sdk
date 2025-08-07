package com.proxykit.sdk.core.storage

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for sensitive data using Android Keystore
 */
internal class SecureStorage(context: Context) {
    
    private val sharedPreferences = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Use MasterKey.Builder for API 23+ (modern approach)
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            "proxykit_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } else {
        // For API < 23, use regular SharedPreferences (less secure but compatible)
        context.getSharedPreferences("proxykit_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * Store session token
     */
    fun saveSessionToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_SESSION_TOKEN, token)
            .putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Retrieve session token
     */
    fun getSessionToken(): String? {
        return sharedPreferences.getString(KEY_SESSION_TOKEN, null)
    }
    
    /**
     * Get session timestamp
     */
    fun getSessionTimestamp(): Long {
        return sharedPreferences.getLong(KEY_SESSION_TIMESTAMP, 0)
    }
    
    /**
     * Store device ID
     */
    fun saveDeviceId(deviceId: String) {
        sharedPreferences.edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .apply()
    }
    
    /**
     * Retrieve device ID
     */
    fun getDeviceId(): String? {
        return sharedPreferences.getString(KEY_DEVICE_ID, null)
    }
    
    /**
     * Store public key
     */
    fun savePublicKey(publicKey: String) {
        sharedPreferences.edit()
            .putString(KEY_PUBLIC_KEY, publicKey)
            .apply()
    }
    
    /**
     * Retrieve public key
     */
    fun getPublicKey(): String? {
        return sharedPreferences.getString(KEY_PUBLIC_KEY, null)
    }
    
    /**
     * Clear all stored data
     */
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Clear session data only
     */
    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_SESSION_TIMESTAMP)
            .apply()
    }
    
    companion object {
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_SESSION_TIMESTAMP = "session_timestamp"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_PUBLIC_KEY = "public_key"
    }
}