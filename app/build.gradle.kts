plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)  // No explicit version here
}

android {
    namespace = "com.kamina.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kamina.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"  // Compose Compiler version
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.compose.material3:material3:1.1.0")

    implementation("androidx.compose.ui:ui:1.7.1")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Ktor for networking
    implementation("io.ktor:ktor-client-core:2.3.2")
    implementation("io.ktor:ktor-client-cio:2.3.2")
    implementation("io.ktor:ktor-client-json:2.3.2")
    implementation("io.ktor:ktor-client-serialization:2.3.2")

    implementation("androidx.compose.ui:ui-tooling-preview:1.7.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.1")

    implementation("io.coil-kt:coil-compose:2.3.0")

    implementation("androidx.compose.foundation:foundation:1.7.1")

    implementation("androidx.navigation:navigation-compose:2.8.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer:2.18.3")
    implementation ("com.google.android.exoplayer:exoplayer-core:2.18.3")
    implementation ("com.google.android.exoplayer:exoplayer-hls:2.18.3")
    implementation ("com.google.android.exoplayer:exoplayer-dash:2.18.3")
    implementation("androidx.media3:media3-exoplayer:1.0.1")
    implementation("org.videolan.android:libvlc-all:3.3.13")
    implementation("androidx.webkit:webkit:1.8.0")
    implementation ("androidx.browser:browser:1.3.0")
    implementation(libs.filament.android)
    implementation(libs.androidx.benchmark.macro)


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.1")
}
