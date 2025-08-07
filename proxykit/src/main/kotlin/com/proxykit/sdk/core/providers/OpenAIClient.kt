package com.proxykit.sdk.core.providers

import com.proxykit.sdk.core.attestation.AttestationManager
import com.proxykit.sdk.core.models.AIProvider
import com.proxykit.sdk.core.network.NetworkClient

/**
 * OpenAI API client
 */
internal class OpenAIClient(
    networkClient: NetworkClient,
    attestationManager: AttestationManager
) : ChatProvider(networkClient, attestationManager) {
    
    override val provider = AIProvider.OPENAI
}