// build.gradle.kts  (project root)

plugins {
    // Apply the Android Gradle Plugin and Kotlin plugin *only* in the sub‑projects
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // If you use any other global plugins (e.g. kotlin‑serialization, hilt, etc.) add them here with `apply false`
}

// No `tasks.withType<KotlinCompile>()` here – that belongs in the **module** (app) build file.
// The root project only needs repository configuration.

allprojects {
    repositories {
        google()
        mavenCentral()
        // Add any other maven repos you need (jitpack, etc.)
    }
}

// Optional: common Gradle‑JVM settings for the whole build
// (you can also keep these in gradle.properties)
