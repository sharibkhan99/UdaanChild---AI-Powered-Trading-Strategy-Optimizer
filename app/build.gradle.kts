// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // any other module‑level plugins
}

android {
    namespace = "com.udaan.child"
    compileSdk = 34               // 34 works with AGP 8.7.3
    defaultConfig {
        applicationId = "com.udaan.child"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // ---- Kotlin compile options go HERE ----
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
    // -----------------------------------------

    // composeOptions, buildTypes, etc.
}

dependencies {
    // your dependencies …
}
