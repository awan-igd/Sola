plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "sola.aigd"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "sola.aigd"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // For 16KB
        ndk {
            // Use only 64-bit for Play Store, faster & 16KB ready
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            optimization { enable = false }
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // FIX FOR 16KB - THIS IS MAIN
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    bundle {
        // For App Bundle
        abi {
            enableSplit = false
        }
    }
}

dependencies {
    implementation("com.startapp:inapp-sdk:5.3.1")

    // UPDATE CAMERA to 16KB ready
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")

    // FIX: ML Kit 17.3.0+ is 16KB compatible
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Guava - use lite version for 16KB
    implementation("com.google.guava:guava:32.1.2-android")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.activity.ktx)
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.play.services.vision.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}