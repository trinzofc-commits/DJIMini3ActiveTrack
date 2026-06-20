plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.djimini3activetrack"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.djimini3activetrack"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // DJI SDK V5 supports both, but arm64-v8a is preferred for modern devices
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    packaging {
        jniLibs {
            pickFirsts += setOf(
                "lib/arm64-v8a/libc++_shared.so",
                "lib/armeabi-v7a/libc++_shared.so"
            )
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // DJI SDK V5 - Using implementation for both to ensure they are included in APK
    implementation("com.dji:dji-sdk-v5-aircraft:5.10.0")
    // Note: 'compileOnly' was likely causing the NoClassDefFoundError because it doesn't include the class in APK
    // We use implementation for the provided part too if it contains necessary runtime classes
    implementation("com.dji:dji-sdk-v5-aircraft-provided:5.10.0")

    // Google ML Kit for Object Detection and Tracking
    implementation("com.google.mlkit:object-detection:17.0.1")
    implementation("com.google.mlkit:object-detection-common:17.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
