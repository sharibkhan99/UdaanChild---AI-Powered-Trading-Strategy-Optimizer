// ---------------------------------------------------------------
// UdaanChild/settings.gradle.kts
// ---------------------------------------------------------------
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Pin the exact plugin versions – the module will just `apply` them
    plugins {
        id("com.android.application") version "8.5.0"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    }
}

// All **artifact** repositories for every sub‑project
repositories {
    google()
    mavenCentral()
}

// Include the app module
include(":app")
