package com.proxykit.sdk.core.attestation

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.proxykit.sdk.core.models.ProxyKitError
import com.proxykit.sdk.core.network.NetworkClient
import com.proxykit.sdk.core.storage.SecureStorage
import com.proxykit.sdk.core.utils.Logger
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Manages device attestation using Play Integrity API
 */
class AttestationManager internal constructor(
    private val context: Context,
    private val appId: String,
    private val networkClient: NetworkClient,
    internal val storage: SecureStorage,
    internal val sessionManager: SessionManager
) {
    private val integrityManager = IntegrityManagerFactory.create(context)
    
    /**
     * Current attestation status
     */
    var currentStatus: AttestationStatus = AttestationStatus.NotStarted
        private set
    
    private val observers = mutableListOf<AttestationObserver>()
    
    /**
     * Perform attestation if needed
     */
    suspend fun attestIfNeeded() {
        if (sessionManager.hasValidSession()) {
            Logger.debug("Valid session exists, skipping attestation")
            return
        }
        
        performAttestation()
    }
    
    /**
     * Force attestation even if session exists
     */
    suspend fun forceAttestation() {
        performAttestation()
    }
    
    private suspend fun performAttestation() {
        updateStatus(AttestationStatus.InProgress)
        
        try {
            // Step 1: Get challenge from server
            val challenge = getChallenge()
            Logger.debug("Got challenge: ${challenge.take(10)}...")
            
            // Step 2: Request integrity token from Play Integrity
            val integrityToken = requestIntegrityToken(challenge)
            Logger.debug("Got integrity token")
            
            // Step 3: Verify with backend
            val result = verifyAttestation(integrityToken)
            
            // Step 4: Store session
            sessionManager.saveSession(
                token = result.sessionToken,
                deviceId = result.deviceId,
                publicKey = result.publicKey
            )
            
            updateStatus(AttestationStatus.Success)
            Logger.info("Attestation successful")
            
        } catch (e: Exception) {
            Logger.error("Attestation failed", e)
            updateStatus(AttestationStatus.Failed(e.message ?: "Unknown error"))
            throw when (e) {
                is ProxyKitError -> e
                else -> ProxyKitError.AttestationFailed(e.message ?: "Unknown error")
            }
        }
    }
    
    private suspend fun getChallenge(): String {
        val response = networkClient.post<ChallengeResponse>(
            path = "/v1/attestation/challenge",
            body = mapOf("appId" to appId)
        )
        return response.challenge
    }
    
    private suspend fun requestIntegrityToken(nonce: String): String {
        val integrityTokenRequest = IntegrityTokenRequest.builder()
            .setNonce(nonce)
            .build()
        
        val response = integrityManager.requestIntegrityToken(integrityTokenRequest).await()
        return response.token()
    }
    
    private suspend fun verifyAttestation(token: String): AttestationVerifyResponse {
        return networkClient.post(
            path = "/v1/attestation/android/verify",
            body = AttestationVerifyRequest(
                appId = appId,
                attestation = token,
                platform = "ANDROID"
            )
        )
    }
    
    private fun updateStatus(status: AttestationStatus) {
        currentStatus = status
        observers.forEach { it.attestationDidUpdate(status) }
    }
    
    fun addObserver(observer: AttestationObserver) {
        observers.add(observer)
    }
    
    fun removeObserver(observer: AttestationObserver) {
        observers.remove(observer)
    }
    
    @Serializable
    private data class ChallengeResponse(
        val challenge: String
    )
    
    @Serializable
    private data class AttestationVerifyRequest(
        val appId: String,
        val attestation: String,
        val platform: String
    )
    
    @Serializable
    private data class AttestationVerifyResponse(
        val sessionToken: String,
        val deviceId: String,
        val publicKey: String,
        val expiresAt: String
    )
}

/**
 * Attestation status
 */
sealed class AttestationStatus {
    object NotStarted : AttestationStatus()
    object InProgress : AttestationStatus()
    object Success : AttestationStatus()
    data class Failed(val reason: String) : AttestationStatus()
}

/**
 * Observer for attestation status changes
 */
interface AttestationObserver {
    fun attestationDidUpdate(status: AttestationStatus)
}