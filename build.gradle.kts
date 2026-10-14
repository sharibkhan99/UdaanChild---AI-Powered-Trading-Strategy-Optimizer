// build.gradle.kts  (project root)
plugins {
    // Android Gradle Plugin – keep the version you already use
    id("com.android.application") version "8.5.0" apply false

    // Kotlin Android plugin – version must match the serialization plugin
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // Kotlin Serialization plugin – same version as the Kotlin plugin
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}
