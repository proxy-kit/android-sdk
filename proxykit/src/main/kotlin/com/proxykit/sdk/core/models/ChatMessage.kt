package com.proxykit.sdk.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a message in a chat conversation
 */
@Serializable
sealed class ChatMessage {
    abstract val role: String
    abstract val content: MessageContent
    
    @Serializable
    @SerialName("system")
    data class System(
        override val content: MessageContent
    ) : ChatMessage() {
        override val role: String = "system"
    }
    
    @Serializable
    @SerialName("user")
    data class User(
        override val content: MessageContent
    ) : ChatMessage() {
        override val role: String = "user"
    }
    
    @Serializable
    @SerialName("assistant")
    data class Assistant(
        override val content: MessageContent
    ) : ChatMessage() {
        override val role: String = "assistant"
    }
    
    companion object {
        /**
         * Create a system message
         */
        fun system(content: String): ChatMessage = System(MessageContent.Text(content))
        
        /**
         * Create a user message
         */
        fun user(content: String): ChatMessage = User(MessageContent.Text(content))
        
        /**
         * Create a user message with images
         */
        fun user(text: String, images: List<ByteArray>): ChatMessage {
            val parts = mutableListOf<ContentPart>(ContentPart.Text(text))
            images.forEach { imageData ->
                parts.add(ContentPart.Image(
                    data = android.util.Base64.encodeToString(imageData, android.util.Base64.NO_WRAP),
                    mimeType = "image/jpeg"
                ))
            }
            return User(MessageContent.Parts(parts))
        }
        
        /**
         * Create an assistant message
         */
        fun assistant(content: String): ChatMessage = Assistant(MessageContent.Text(content))
    }
}

/**
 * Represents the content of a message
 */
@Serializable
sealed class MessageContent {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : MessageContent()
    
    @Serializable
    @SerialName("parts")
    data class Parts(val parts: List<ContentPart>) : MessageContent()
}

/**
 * Represents a part of multi-modal content
 */
@Serializable
sealed class ContentPart {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : ContentPart()
    
    @Serializable
    @SerialName("image")
    data class Image(
        val data: String,
        @SerialName("mime_type") val mimeType: String
    ) : ContentPart()
}