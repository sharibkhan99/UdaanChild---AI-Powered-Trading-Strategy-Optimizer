android {
    namespace = "com.udaan.child"
    compileSdk = 34

    defaultConfig { ... }

    // --- Compose Yahan Enable Karein ---
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // ya aapka jo version ho
    }
    // -----------------------------------
}
