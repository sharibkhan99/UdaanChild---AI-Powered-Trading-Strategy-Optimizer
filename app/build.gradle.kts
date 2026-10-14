// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Compose Compiler Gradle plugin (mandatory with Kotlin 2.0)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.udaan.child"
    compileSdk = 34                     // works with AGP 8.7.3
    defaultConfig {
        applicationId = "com.udaan.child"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        // Android 12+ requires explicit exported flag for launcher activity
        manifestPlaceholders["exported"] = "true"
    }

    // ---- Kotlin / Java compatibility ----
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // ---- Compose ----
    buildFeatures {
        compose = true
        viewBinding = true               // keep if you use view‑binding
    }
    composeOptions {
        // The Compose Compiler plugin now manages the version,
        // but keeping the explicit version does not hurt.
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE,NOTICE}"
        }
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
}

dependencies {
    // ---- AndroidX / Core ----
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.0")

    // ---- Compose (BOM manages versions) ----
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")

    // ---- Navigation ----
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ---- Networking ----
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ---- Coroutines ----
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ---- WorkManager ----
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ---- Test ----
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
