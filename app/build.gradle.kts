plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
    id ("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.drew.and.colorpicker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.drew.and.colorpicker"
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

    // Allow references to generated code
    kapt {
        correctErrorTypes = true
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation (libs.androidx.camera.core)
    implementation (libs.androidx.camera.camera2)
    // If you want to additionally use the CameraX Lifecycle library
    implementation (libs.androidx.camera.lifecycle)
    // If you want to additionally use the CameraX VideoCapture library
    implementation (libs.androidx.camera.video)
    // If you want to additionally use the CameraX View class
    implementation (libs.androidx.camera.view)
    // If you want to additionally use the CameraX Extensions library
    implementation (libs.androidx.camera.extensions)

    // ML Kit Vision dependencies (if needed for image analysis)
    implementation (libs.vision.common)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.tensorflow.lite)

    //HiltViewModel
    implementation(libs.hilt.android)
    implementation (libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    // ViewModel for Jetpack Compose
    implementation (libs.androidx.lifecycle.viewmodel.compose)

    // LiveData extensions for Jetpack Compose
    implementation(libs.androidx.runtime.livedata)

    //Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.room.runtime)

    // To use Kotlin Symbol Processing (KSP)
    ksp(libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

}