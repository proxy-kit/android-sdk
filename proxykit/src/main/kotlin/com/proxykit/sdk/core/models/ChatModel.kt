package com.proxykit.sdk.core.models

/**
 * Available AI providers
 */
enum class AIProvider(val value: String) {
    OPENAI("openai"),
    ANTHROPIC("anthropic");
    
    override fun toString() = value
}

/**
 * Available chat models
 */
sealed class ChatModel(val rawValue: String) {
    
    /**
     * OpenAI models
     */
    sealed class OpenAI(modelName: String) : ChatModel(modelName) {
        object GPT4o : OpenAI("gpt-4o")
        object GPT4oMini : OpenAI("gpt-4o-mini")
        object GPT35Turbo : OpenAI("gpt-3.5-turbo")
        data class Custom(val name: String) : OpenAI(name)
    }
    
    /**
     * Anthropic models
     */
    sealed class Anthropic(modelName: String) : ChatModel(modelName) {
        object Claude3Opus : Anthropic("claude-3-opus-20240229")
        object Claude3Sonnet : Anthropic("claude-3-sonnet-20240229")
        object Claude3Haiku : Anthropic("claude-3-haiku-20240307")
        object Claude2 : Anthropic("claude-2.1")
        object ClaudeInstant : Anthropic("claude-instant-1.2")
        data class Custom(val name: String) : Anthropic(name)
    }
    
    /**
     * Get the provider for this model
     */
    val provider: AIProvider
        get() = when (this) {
            is OpenAI -> AIProvider.OPENAI
            is Anthropic -> AIProvider.ANTHROPIC
        }
    
    companion object {
        /**
         * Common model shortcuts
         */
        val gpt4o = OpenAI.GPT4o
        val gpt4oMini = OpenAI.GPT4oMini
        val gpt35Turbo = OpenAI.GPT35Turbo
        val claude3Opus = Anthropic.Claude3Opus
        val claude3Sonnet = Anthropic.Claude3Sonnet
        val claude3Haiku = Anthropic.Claude3Haiku
        
        /**
         * Create a model from provider and name
         */
        fun from(provider: AIProvider, modelName: String): ChatModel {
            return when (provider) {
                AIProvider.OPENAI -> when (modelName) {
                    "gpt-4o" -> OpenAI.GPT4o
                    "gpt-4o-mini" -> OpenAI.GPT4oMini
                    "gpt-3.5-turbo" -> OpenAI.GPT35Turbo
                    else -> OpenAI.Custom(modelName)
                }
                AIProvider.ANTHROPIC -> when (modelName) {
                    "claude-3-opus-20240229" -> Anthropic.Claude3Opus
                    "claude-3-sonnet-20240229" -> Anthropic.Claude3Sonnet
                    "claude-3-haiku-20240307" -> Anthropic.Claude3Haiku
                    "claude-2.1" -> Anthropic.Claude2
                    "claude-instant-1.2" -> Anthropic.ClaudeInstant
                    else -> Anthropic.Custom(modelName)
                }
            }
        }
    }
}