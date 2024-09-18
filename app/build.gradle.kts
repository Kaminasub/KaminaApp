plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.google.android.material:material:1.9.0") // MaterialComponents library
    implementation("androidx.compose.material3:material3:1.3.0")

    implementation("androidx.compose.ui:ui:1.7.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    // Ktor Dependencies for networking
    implementation("io.ktor:ktor-client-core:2.3.0") // Update to the latest stable version
    implementation("io.ktor:ktor-client-cio:2.3.0")
    implementation("io.ktor:ktor-client-json:2.3.0")
    implementation("io.ktor:ktor-client-serialization:2.3.0")

    // Jetpack Compose (Consistent Versioning)
    implementation("androidx.compose.ui:ui:1.7.1")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.1")

    // Compose for activity
    implementation("androidx.activity:activity-compose:1.9.2")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.3.0") // Update to the latest version

    // Foundation and Runtime libraries
    implementation("androidx.compose.foundation:foundation:1.7.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.1")

    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Retrofit for Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")

    // RxJava2 for Reactive programming
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

    // AndroidX Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.5")

    // AppCompat for backward compatibility
    implementation("androidx.appcompat:appcompat:1.7.0")

    // RecyclerView for scrolling avatars
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // JUnit for Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose UI Test Dependencies
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.1")


}

