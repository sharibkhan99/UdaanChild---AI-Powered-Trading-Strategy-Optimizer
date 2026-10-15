// ---------------------------------------------------------------
// UdaanChild/settings.gradle.kts
// ---------------------------------------------------------------
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Pin the plugins you use – this also tells Gradle which
    // versions to resolve without putting them in the root build file.
    plugins {
        id("com.android.application") version "8.5.0"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    }
}

// ---------------------------------------------------------------
// All project repositories – **the only place they may appear**
// ---------------------------------------------------------------
repositories {
    google()
    mavenCentral()
}

// ---------------------------------------------------------------
// Include the app module
// ---------------------------------------------------------------
include(":app")
