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
        versionCode = 2
        versionName = "2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.startapp:inapp-sdk:5.3.1")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
// ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
// Guava for ListenableFuture
    implementation("com.google.guava:guava:31.1-android")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.play.services.vision.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}