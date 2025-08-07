package com.proxykit.sdk

import android.content.Context
import com.proxykit.sdk.core.Configuration
import com.proxykit.sdk.core.models.*

/**
 * Simple, context-aware chat interface
 * Automatically maintains conversation history
 */
class SecureProxy(
    private val model: ChatModel = ChatModel.gpt4o,
    private val systemPrompt: String = "You are a helpful assistant"
) {
    private val messages = mutableListOf<ChatMessage>()
    
    /**
     * Overrides for individual chat calls
     */
    data class ChatOverrides(
        val model: ChatModel? = null,
        val systemPrompt: String? = null
    )
    
    /**
     * Send a message and get a response
     */
    suspend fun send(
        message: String,
        images: List<ByteArray>? = null,
        overrides: ChatOverrides = ChatOverrides()
    ): String {
        val activeModel = overrides.model ?: model
        
        // Add system prompt if this is the first message
        if (messages.isEmpty()) {
            val prompt = overrides.systemPrompt ?: systemPrompt
            messages.add(ChatMessage.system(prompt))
        }
        
        // Add user message
        if (!images.isNullOrEmpty()) {
            messages.add(ChatMessage.user(message, images))
        } else {
            messages.add(ChatMessage.user(message))
        }
        
        // Make API call
        val response = when (activeModel) {
            is ChatModel.OpenAI -> {
                AIProxy.openai.chat.completions.create(
                    model = activeModel.rawValue,
                    messages = messages
                )
            }
            is ChatModel.Anthropic -> {
                AIProxy.anthropic.chat.completions.create(
                    model = activeModel.rawValue,
                    messages = messages
                )
            }
        }
        
        // Extract response
        val assistantMessage = response.choices.firstOrNull()?.message
            ?: throw ProxyKitError.InvalidResponse("No response from AI")
        
        // Extract text content
        val messageText = when (val content = assistantMessage.content) {
            is MessageContent.Text -> content.text
            is MessageContent.Parts -> {
                content.parts.filterIsInstance<ContentPart.Text>()
                    .joinToString(" ") { it.text }
            }
        }
        
        // Update conversation history
        messages.add(assistantMessage)
        
        return messageText
    }
    
    /**
     * Stream a response for the given message
     */
    suspend fun streamResponse(
        message: String,
        images: List<ByteArray>? = null,
        overrides: ChatOverrides = ChatOverrides()
    ): kotlinx.coroutines.flow.Flow<String> {
        val activeModel = overrides.model ?: model
        
        // Add system prompt if this is the first message
        if (messages.isEmpty()) {
            val prompt = overrides.systemPrompt ?: systemPrompt
            messages.add(ChatMessage.system(prompt))
        }
        
        // Add user message
        if (!images.isNullOrEmpty()) {
            messages.add(ChatMessage.user(message, images))
        } else {
            messages.add(ChatMessage.user(message))
        }
        
        // Stream from API
        val stream = when (activeModel) {
            is ChatModel.OpenAI -> {
                AIProxy.openai.chat.completions.stream(
                    model = activeModel.rawValue,
                    messages = messages
                )
            }
            is ChatModel.Anthropic -> {
                AIProxy.anthropic.chat.completions.stream(
                    model = activeModel.rawValue,
                    messages = messages
                )
            }
        }
        
        // Transform stream to return only text content
        return kotlinx.coroutines.flow.flow {
            val fullMessage = StringBuilder()
            stream.collect { chunk ->
                chunk.choices.firstOrNull()?.delta?.content?.let { content ->
                    when (content) {
                        is MessageContent.Text -> {
                            fullMessage.append(content.text)
                            emit(content.text)
                        }
                        is MessageContent.Parts -> {
                            val text = content.parts.filterIsInstance<ContentPart.Text>()
                                .joinToString("") { it.text }
                            fullMessage.append(text)
                            emit(text)
                        }
                    }
                }
            }
            
            // Add the complete message to history
            messages.add(ChatMessage.assistant(fullMessage.toString()))
        }
    }
    
    /**
     * Reset conversation context
     */
    fun reset() {
        messages.clear()
    }
    
    /**
     * Get conversation history
     */
    fun getHistory(): List<ChatMessage> {
        return messages.toList()
    }
    
    companion object {
        /**
         * Configure ProxyKit SDK
         * @param context Application context
         * @param appId Your app ID from the dashboard
         * @return Configuration error if any, null on success
         */
        @JvmStatic
        fun configure(context: Context, appId: String): Exception? {
            return try {
                AIProxy.configure()
                    .withAppId(appId)
                    .build(context)
                null
            } catch (e: Exception) {
                e
            }
        }
    }
}