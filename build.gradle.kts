// build.gradle.kts   (project root)
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // No extra classpath dependencies needed – the plugins are resolved via pluginManagement above
}

plugins {
    // Android Gradle Plugin
    id("com.android.application") version "8.5.0" apply false

    // Kotlin Android plugin – keep the same version for the serialization plugin
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // Kotlin Serialization plugin (same version as Kotlin)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}

// If you have any `allprojects { … }` or `subprojects { … }` blocks you can keep them,
// but **do not** declare repositories there any more – the central place is settings.gradle.kts.
