package com.proxykit.sdk.core.providers

import com.proxykit.sdk.core.attestation.AttestationManager
import com.proxykit.sdk.core.models.*
import com.proxykit.sdk.core.network.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Base class for chat providers
 */
abstract class ChatProvider(
    protected val networkClient: NetworkClient,
    protected val attestationManager: AttestationManager
) {
    /**
     * Provider name
     */
    abstract val provider: AIProvider
    
    /**
     * Chat namespace for fluent API
     */
    val chat = Chat()
    
    inner class Chat {
        val completions = Completions()
    }
    
    inner class Completions {
        /**
         * Create a chat completion
         */
        suspend fun create(
            model: String,
            messages: List<ChatMessage>,
            temperature: Double? = null,
            maxTokens: Int? = null,
            topP: Double? = null,
            frequencyPenalty: Double? = null,
            presencePenalty: Double? = null,
            stream: Boolean = false
        ): ChatResponse {
            // Ensure we have a valid session
            attestationManager.attestIfNeeded()
            
            val request = ChatRequest(
                provider = provider.value,
                model = model,
                messages = messages,
                temperature = temperature,
                maxTokens = maxTokens,
                topP = topP,
                frequencyPenalty = frequencyPenalty,
                presencePenalty = presencePenalty,
                stream = stream
            )
            
            return networkClient.post(
                path = "/v1/proxy/chat",
                body = request
            )
        }
        
        /**
         * Create a streaming chat completion
         */
        suspend fun stream(
            model: String,
            messages: List<ChatMessage>,
            temperature: Double? = null,
            maxTokens: Int? = null,
            topP: Double? = null,
            frequencyPenalty: Double? = null,
            presencePenalty: Double? = null
        ): Flow<ChatStreamChunk> {
            // Ensure we have a valid session
            attestationManager.attestIfNeeded()
            
            val request = ChatRequest(
                provider = provider.value,
                model = model,
                messages = messages,
                temperature = temperature,
                maxTokens = maxTokens,
                topP = topP,
                frequencyPenalty = frequencyPenalty,
                presencePenalty = presencePenalty,
                stream = true
            )
            
            return networkClient.stream(
                path = "/v1/proxy/chat",
                body = request
            )
        }
    }
    
    @Serializable
    private data class ChatRequest(
        val provider: String,
        val model: String,
        val messages: List<ChatMessage>,
        val temperature: Double? = null,
        @SerialName("max_tokens") val maxTokens: Int? = null,
        @SerialName("top_p") val topP: Double? = null,
        @SerialName("frequency_penalty") val frequencyPenalty: Double? = null,
        @SerialName("presence_penalty") val presencePenalty: Double? = null,
        val stream: Boolean = false
    )
}