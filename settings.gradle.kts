// ---------------------------------------------------------------
// UdaanChild/settings.gradle.kts
// ---------------------------------------------------------------
pluginManagement {
    // Where Gradle looks for the *plugins* themselves
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // Pin the exact plugin versions – the modules will just `apply` them
    plugins {
        id("com.android.application") version "8.5.0"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    }
}

// ---------------------------------------------------------------
// Artifact repositories for *all* projects (the only legal place
// for `repositories { … }` in a settings script since Gradle 7)
// ---------------------------------------------------------------
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // enforce the rule
    repositories {
        google()
        mavenCentral()          // no arguments – the deprecated var‑arg overload is avoided
    }
}

// ---------------------------------------------------------------
// Include the app module
// ---------------------------------------------------------------
include(":app")
