plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("maven-publish")
}

android {
    namespace = "com.proxykit.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    // Play Integrity API for attestation
    implementation("com.google.android.play:integrity:1.3.0")
    
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.proxykit"
            artifactId = "proxykit-android"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
            
            pom {
                name.set("ProxyKit Android SDK")
                description.set("Secure AI proxy SDK for Android apps")
                url.set("https://github.com/proxy-kit/android-sdk")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("proxykit")
                        name.set("ProxyKit Team")
                        email.set("support@secureapikey.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/proxy-kit/android-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com/proxy-kit/android-sdk.git")
                    url.set("https://github.com/proxy-kit/android-sdk")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/proxy-kit/android-sdk")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}