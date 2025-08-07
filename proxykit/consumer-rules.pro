# ProxyKit ProGuard Rules

# Keep all public API classes
-keep class com.proxykit.sdk.ProxyKit { *; }
-keep class com.proxykit.sdk.SecureProxy { *; }
-keep class com.proxykit.sdk.core.models.** { *; }
-keep class com.proxykit.sdk.core.Configuration { *; }
-keep class com.proxykit.sdk.core.Configuration$* { *; }
-keep class com.proxykit.sdk.core.attestation.AttestationStatus { *; }
-keep class com.proxykit.sdk.core.attestation.AttestationObserver { *; }

# Keep serializable classes for Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.proxykit.sdk.**$$serializer { *; }
-keepclassmembers class com.proxykit.sdk.** {
    *** Companion;
}
-keepclasseswithmembers class com.proxykit.sdk.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Play Integrity API
-keep class com.google.android.play.core.integrity.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Coroutines
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}