plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.app.synapse"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.synapse"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    // Core Android & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Assuming this uses the version you want (e.g., 1.12.0 or 1.13.0-alpha01 from your TOML)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1") // Consider adding to libs.versions.toml

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2")) // Consider adding to libs.versions.toml
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx") // If using Firebase Storage

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.viewbinding)
    kapt(libs.hilt.compiler)

    // --- Testing ---
    // Core Testing (Unit Tests)
    testImplementation(libs.junit) // Assuming this points to "junit:junit:4.13.2"
    testImplementation("org.mockito:mockito-core:5.10.0") // Consider adding to libs.versions.toml
    testImplementation("org.mockito:mockito-inline:5.2.0") // Consider adding to libs.versions.toml
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Consider adding to libs.versions.toml
    testImplementation("androidx.arch.core:core-testing:2.2.0") // Consider adding to libs.versions.toml
    // For MockK (alternative to Mockito, often preferred in Kotlin)
    // testImplementation("io.mockk:mockk:1.13.10")
    // For unit tests with Hilt (if needed, often can mock dependencies directly)
    // testImplementation(libs.hilt.android.testing) // If you add an alias for hilt-android-testing
    // kaptTest(libs.hilt.compiler)

    // Core Testing (Instrumented Tests)
    androidTestImplementation(libs.androidx.junit) // Assuming this points to "androidx.test.ext:junit:1.2.1"
    androidTestImplementation(libs.androidx.espresso.core) // Assuming this points to "androidx.test.espresso:espresso-core:3.6.1"

    // Hilt Testing (Instrumented Tests)
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1") // Or use an alias if defined for hilt-android-testing
    kaptAndroidTest(libs.hilt.compiler) // Or "com.google.dagger:hilt-compiler:2.51.1"

    // Espresso Intents (for testing intents)
    // androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")

    // UI Automator (for more complex UI interactions if Espresso is not enough)
    // androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")   // or latest stable
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.wear.compose:compose-material:1.3.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.wear.compose:compose-material:1.3.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0") // Or the latest version
    implementation(libs.androidx.swiperefreshlayout)

}