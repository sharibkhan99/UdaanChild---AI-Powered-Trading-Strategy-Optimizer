// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")   // already present
}

android {
    namespace = "com.udaan.child"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.udaan.child"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Modern replacement for the deprecated `kotlinOptions` block
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // ----- AndroidX / Jetpack -------------------------------------------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // *** ConstraintLayout – provides the layout_constraint* attributes ***
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ----- DataStore (Preferences) -------------------------------------------
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ----- kotlinx‑serialization (JSON) ---------------------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ----- WorkManager --------------------------------------------------------
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ----- (any other libraries you already use – Retrofit, Coroutines, etc.) -----
}
