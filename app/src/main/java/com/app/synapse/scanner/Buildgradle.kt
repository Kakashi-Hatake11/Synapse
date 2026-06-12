plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Compose compiler plugin (required for Kotlin 2.0+). The "Empty Activity"
    // Compose template already adds this — keep whichever the template generated.
    id("org.jetbrains.kotlin.plugin.compose")
    // KSP is used by Room's annotation processor. Match the KSP version to your
    // Kotlin version (e.g. Kotlin 2.0.21 -> KSP 2.0.21-1.0.25).
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.docscanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.docscanner"
        minSdk = 21          // ML Kit Document Scanner requires API 21+ and >=1.7GB RAM
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ----- AndroidX core + Compose (first-party, free) -----
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ----- Room (AndroidX, free) — folders + pages metadata -----
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ----- ML Kit Document Scanner (Google, free) — capture + edge detection -----
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")

    // ----- ML Kit Text Recognition (Google, free, on-device) — OCR for "Create TXT" -----
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // NOTE: PDF export uses android.graphics.pdf.PdfDocument (built into the SDK)
    //       and ZIP export uses java.util.zip (Java standard library).
    //       No third-party libraries are used anywhere.
}