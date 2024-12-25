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
            isMinifyEnabled = true
            isShrinkResources = true
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

    implementation(libs.timber)
    implementation("com.github.Edsuns.AdblockAndroid:ad-filter:0.9.1") {
        exclude(group = "org.jetbrains.anko", module = "anko-commons")
        exclude(group = "org.jetbrains.anko", module = "anko-design")
    }



    implementation (libs.okhttp)
    implementation(libs.androidx.lifecycle.runtime.ktx.v286)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.material)
    implementation(libs.material3) // Use only the stable version
    implementation(libs.androidx.material.icons.core) // Latest stable version of material-icons-core
    implementation(libs.material.icons.extended) // Optional if you need extended icons


    implementation(libs.ui)
    implementation(libs.androidx.activity.compose.v192)

    // Ktor for networking
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.serialization)

    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.runtime.livedata)

    implementation(libs.coil.compose)

    implementation(libs.androidx.foundation)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.rxjava)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)

    // ExoPlayer
    implementation(libs.exoplayer)
    implementation (libs.exoplayer.core)
    implementation (libs.google.exoplayer.hls)
    implementation (libs.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.libvlc.all)
    implementation(libs.androidx.webkit.v1121)
    implementation (libs.androidx.browser)
    implementation(libs.filament.android)
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.espresso.core)
    implementation(libs.androidx.ui.test.android)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}

configurations.all {
    resolutionStrategy {
        force (libs.androidx.espresso.core)
    }
}
