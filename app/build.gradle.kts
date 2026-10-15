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

    // -------------------- Kotlinx Serialization --------------------
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // -------------------- DhanHQ SDK (live market data) --------------------
    implementation("com.dhanhq:dhanhq-sdk:1.0.4")

    // -------------------- Apache Commons Math (matrix exponential) --------------------
    implementation("org.apache.commons:commons-math3:3.6.1")

    // -------------------- Jetpack Compose (Material 3) --------------------
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
}
