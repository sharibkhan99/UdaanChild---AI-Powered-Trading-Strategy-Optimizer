// build.gradle.kts  (project root)

plugins {
    // Android Gradle Plugin
    id("com.android.application") version "8.7.3" apply false
    // Kotlin 2.0.x
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // **Required from Kotlin 2.0 when Compose is used**
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
