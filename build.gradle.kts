// build.gradle.kts (project root)
plugins {
    // No plugins applied at root level – they are applied in the modules
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Common JVM / Kotlin options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}
