// app/build.gradle.kts
plugins {
    id("com.android.application") version "8.7.3" apply false   // AGP 8.7.3
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false // Kotlin 2.0.21 → compatible with Gradle 9.8
    // …any other plugins you use
}

android {
    namespace = "com.udaan.child"
    compileSdk = 34               // ← 34 works with AGP 8.7.3 (36 needs AGP 8.8+)
    defaultConfig {
        applicationId = "com.udaan.child"
        minSdk = 24
        targetSdk = 34            // keep targetSdk in sync
        versionCode = 1
        versionName = "1.0"
    }
    // …rest of your config (buildTypes, composeOptions, etc.)
}
