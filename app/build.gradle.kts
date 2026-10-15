// ---------------------------------------------------------------
// UdaanChild/app/build.gradle.kts
// ---------------------------------------------------------------
plugins {
    id("com.android.application")
    // Only the serialization plugin is needed explicitly – the Android
    // plugin now ships its own Kotlin compiler (android.builtInKotlin=true).
    id("org.jetbrains.kotlin.plugin.serialization")
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

        // -----------------------------------------------------------
        // DhanHQ credentials – injected from gradle.properties.
        // The value that reaches BuildConfig must be a *string literal*
        // (including the surrounding quotes), therefore we escape them.
        // -----------------------------------------------------------
        buildConfigField(
            "String",
            "DHAN_CLIENT_ID",
            "\"${project.findProperty(\"DHAN_CLIENT_ID\") ?: \"\"}\""
        )
        buildConfigField(
            "String",
            "DHAN_ACCESS_TOKEN",
            "\"${project.findProperty(\"DHAN_ACCESS_TOKEN\") ?: \"\"}\""
        )
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

    // Use the toolchain API instead of the deprecated kotlinOptions.jvmTarget
    kotlin {
        jvmToolchain(17)
    }
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
    implementation("com.dhanhq:dhanhq-sdk:1.0.4")

    // -------------------- Math – matrix exponential --------------------
    implementation("org.apache.commons:commons-math3:3.6.1")
}
