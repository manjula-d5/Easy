
plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.easy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.easy"
        minSdk = 24
        targetSdk = 34
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
        compose = true
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
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    implementation ("com.google.code.gson:gson:2.8.9")

    implementation ("io.coil-kt:coil-compose:2.2.2")

    //implementation ("androidx.compose.material3:material3-icons-extended:1.0.0")

    //implementation ("androidx.compose.material3:material3:1.0.0")
    //implementation ("androidx.compose.material3:material3-icons-extended:1.1.0")

    // Jetpack Compose dependencies
   // implementation ("androidx.compose.ui:ui:1.4.0")  // Or the latest version
    //implementation ("androidx.compose.material3:material3:1.0.0") // Material 3
    //implementation ("androidx.compose.material3:material3-icons-extended:1.0.0" ) // Material 3 Icons
    //implementation ("androidx.activity:activity-compose:1.6.1" )// Activity for Compose

        //implementation ("androidx.compose.material3:material3:1.4.0" )// or latest stable version
        //implementation ("androidx.compose.material:material-icons-extended:1.4.0" )// Ensure this is included for icons



}

