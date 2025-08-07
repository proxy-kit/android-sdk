package com.proxykit.sdk.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from a chat completion request
 */
@Serializable
data class ChatResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null,
    @SerialName("created") val created: Long? = null
)

/**
 * A single choice in the response
 */
@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)

/**
 * Token usage information
 */
@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

/**
 * Stream chunk for real-time responses
 */
@Serializable
data class ChatStreamChunk(
    val id: String,
    val model: String,
    val choices: List<StreamChoice>
)

/**
 * A single choice in a stream chunk
 */
@Serializable
data class StreamChoice(
    val index: Int,
    val delta: DeltaContent,
    @SerialName("finish_reason") val finishReason: String? = null
)

/**
 * Delta content in a stream chunk
 */
@Serializable
data class DeltaContent(
    val role: String? = null,
    val content: String? = null
)