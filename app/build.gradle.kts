plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")   // <-- enable kotlinx‑serialization
    id("org.jetbrains.kotlin.plugin.compose")         // if you use Jetpack Compose
}

android {
    // … your existing config …
    // make sure compileSdk = 34 (or the version you target)
}

dependencies {
    // ---- DataStore (Preferences) ----
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ---- kotlinx‑serialization (JSON) ----
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ---- WorkManager ----
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ---- Other libs you already use (Retrofit, Coroutines, etc.) ----
    // implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // …
}
