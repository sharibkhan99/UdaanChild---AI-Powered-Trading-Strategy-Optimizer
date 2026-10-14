// build.gradle.kts   (project root)

plugins {
    // Apply plugins in sub‑projects only
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // add any other global plugins here with `apply false`
}

allprojects {
    repositories {
        google()
        mavenCentral()
        // jitpack, etc. if you need them
    }
}
