# ProxyKit Android SDK

Secure AI proxy for Android apps. Access OpenAI and Anthropic models without exposing API keys.

## Installation

Add to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.proxykit:proxykit-android:1.0.0")
}
```

## Setup

### 1. Enable Play Integrity

In Google Play Console:
1. Select your app
2. Go to **Release > App integrity**
3. Enable **Play Integrity API**

### 2. Initialize

```kotlin
import com.proxykit.sdk.AIProxy

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        AIProxy.configure()
            .withAppId("app_xxxxxxxxxxxxx") // Get from dashboard
            .build(this)
    }
}
```

## Usage

### Basic Chat

```kotlin
val response = AIProxy.openai.chat.completions.create(
    model = "gpt-4o",
    messages = listOf(
        ChatMessage.user("Hello!")
    )
)

println(response.choices.first().message.content)
```

### Streaming

```kotlin
val stream = AIProxy.openai.chat.completions.stream(
    model = "gpt-4o",
    messages = messages
)

stream.collect { chunk ->
    chunk.choices.firstOrNull()?.delta?.content?.let {
        print(it) // Print each chunk
    }
}
```

### With Context (SecureProxy)

Keep conversation history automatically:

```kotlin
import com.proxykit.sdk.SecureProxy

val assistant = SecureProxy(model = ChatModel.gpt4o)

// Remembers previous messages
val response1 = assistant.send("What's Kotlin?")
val response2 = assistant.send("Tell me more") // Knows context
```

## Jetpack Compose Example

```kotlin
@Composable
fun ChatScreen() {
    var messages by remember { mutableStateOf(listOf<String>()) }
    var input by remember { mutableStateOf("") }
    val assistant = remember { SecureProxy(model = ChatModel.gpt4o) }
    val scope = rememberCoroutineScope()
    
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        
        Row(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f)
            )
            
            Button(
                onClick = {
                    val userMessage = input
                    messages = messages + "You: $userMessage"
                    input = ""
                    
                    scope.launch {
                        try {
                            val response = assistant.send(userMessage)
                            messages = messages + "AI: $response"
                        } catch (e: Exception) {
                            messages = messages + "Error: ${e.message}"
                        }
                    }
                },
                enabled = input.isNotEmpty()
            ) {
                Text("Send")
            }
        }
    }
}
```

## Error Handling

```kotlin
try {
    val response = AIProxy.openai.chat.completions.create(...)
} catch (e: ProxyKitError) {
    when (e) {
        is ProxyKitError.AttestationFailed -> {
            // Device verification failed
        }
        is ProxyKitError.RateLimited -> {
            // Rate limited, retry after e.retryAfter seconds
        }
        is ProxyKitError.NetworkError -> {
            // Network issues
        }
        else -> {
            // Other errors
        }
    }
}
```

## Requirements

- Android 5.0+ (API level 21+)
- Real device (emulator won't work for attestation)

## Troubleshooting

**Attestation fails?**
- Check Play Integrity is enabled
- Verify app package name matches dashboard
- Use real device, not emulator
- Ensure app is signed with release key

**Need help?**
- [Documentation](https://docs.proxykit.dev)
- [Dashboard](https://app.proxykit.dev)

## License

MIT