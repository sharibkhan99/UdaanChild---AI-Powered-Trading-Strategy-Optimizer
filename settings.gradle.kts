// settings.gradle.kts
pluginManagement {
    // Where Gradle looks for the plugins you apply in the root build file
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Repositories for **all** modules (app, library, etc.)
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // If you ever need a third‑party repo add it here, e.g. maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "UdaanChild---AI-Powered-Trading-Strategy-Optimizer"
include(":app")
