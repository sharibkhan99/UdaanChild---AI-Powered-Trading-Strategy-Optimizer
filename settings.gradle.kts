// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Pin the Android Gradle Plugin version here so every module resolves the same one
    plugins {
        id("com.android.application") version "8.5.0" apply false
        id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    }
}

rootProject.name = "UdaanChild"
include(":app")
