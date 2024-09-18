plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services") // Apply the Google Services plugin
    id ("dagger.hilt.android.plugin")

}

android {
    namespace = "com.example.cloudvibe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cloudvibe"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.gson)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Core libraries
    implementation(libs.androidx.core.ktx.v1101)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material.v190)

    // ConstraintLayout
    implementation(libs.androidx.constraintlayout)

    // Navigation components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android.v173)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Room for local storage
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // Retrofit for API calls
    implementation(libs.retrofit.v2110)
    implementation(libs.converter.gson.v2110)

    // OkHttp logging interceptor for Retrofit
    implementation(libs.logging.interceptor)

    // RecyclerView for lists
    implementation(libs.androidx.recyclerview)

    // Location services
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    // Glide for image loading
    implementation(libs.glide.v4151)
    kapt(libs.compiler.v4151)

    // ThreeTenABP for date/time handling
    implementation(libs.threetenabp)

    // Material Components for the Navigation Drawer
    implementation(libs.material)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)

    // AndroidX AppCompat
    implementation(libs.androidx.appcompat)

    // AndroidX ConstraintLayout
    implementation(libs.androidx.constraintlayout)

    // AndroidX Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation (libs.androidx.appcompat)  // or the latest version

    // Google Maps SDK
    implementation (libs.play.services.maps)

    // Google Play Services Location
    implementation (libs.play.services.location)

    // Google Analytics
    implementation (libs.firebase.analytics)

    // Google Maps Android SDK
    implementation (libs.osmdroid.android)

    // JSON to Kotlin
    implementation (libs.library)

    // Dagger Hilt
    implementation (libs.hilt.android)
    kapt (libs.hilt.android.compiler)

    implementation (libs.gson.v2110)

}