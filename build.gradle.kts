// ---------------------------------------------------------------
// UdaanChild/build.gradle.kts   (project / root)
// ---------------------------------------------------------------
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // No extra classpath dependencies – plugins are resolved via pluginManagement
}

plugins {
    // Android Gradle Plugin – **apply false** because the app module will apply it
    id("com.android.application") version "8.5.0" apply false

    // Kotlin plugin – **apply false**; we will *not* apply it in the app module
    // because android.builtInKotlin=true (see gradle.properties)
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // Serialization plugin – still needed for @Serializable data classes
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}

// Centralised repository declaration (required since Gradle 7+)
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
