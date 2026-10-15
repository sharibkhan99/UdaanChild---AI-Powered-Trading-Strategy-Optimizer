// ---------------------------------------------------------------
// app/build.gradle.kts
// ---------------------------------------------------------------
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")   // for @Serializable data classes
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

        // ---- expose Dhan credentials to BuildConfig (keep them out of source control) ----
        buildConfigField("String", "DHAN_CLIENT_ID", "\"${project.findProperty("DHAN_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "DHAN_ACCESS_TOKEN", "\"${project.findProperty("DHAN_ACCESS_TOKEN") ?: ""}\"")
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

    // Modern replacement for the deprecated `kotlinOptions { jvmTarget = "17" }`
    kotlin {
        jvmToolchain(17)
    }

    // Enable ViewBinding / DataBinding if you use them
    // viewBinding { enable = true }
}

dependencies {
    // -------------------- AndroidX / Jetpack --------------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // -------------------- Kotlin std‑lib & Coroutines --------------------
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // -------------------- DhanHQ SDK (live market data) --------------------
    // Official Maven artifact – check https://github.com/dhanhq/dhanhq-java-sdk for the latest version
    implementation("com.dhanhq:dhanhq-sdk:1.0.4")

    // -------------------- Math (matrix exponential for the calculus forecast) --------------------
    implementation("org.apache.commons:commons-math3:3.6.1")

    // -------------------- (optional) Retrofit / OkHttp if you call other REST APIs --------------------
    // implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    // implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
