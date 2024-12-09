plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id ("kotlin-kapt")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.medisight"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.medisight"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
        mlModelBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.material)
    implementation (libs.androidx.core.ktx)
    implementation (libs.androidx.core)
    implementation (libs.ccp)
    implementation (libs.circleimageview)
    implementation (libs.hilt.android)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.hilt.navigation.fragment)
    implementation(libs.generativeai)
    implementation (libs.okhttp)
    implementation (libs.play.services.auth)
    implementation (libs.firebase.auth.ktx)
    implementation (libs.androidx.viewpager2)
    implementation (libs.tensorflow.lite)
    implementation (libs.okhttp.v490)
    implementation (libs.logging.interceptor)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp.v4100)
    implementation (libs.retrofit.v290)
    implementation (libs.converter.gson.v290)
    //noinspection GradleDependency
    implementation (libs.glide)
    //noinspection GradleDependency,GradleDependency
    implementation (libs.com.google.firebase.firebase.storage.ktx)
    //noinspection GradleDependency,GradleDependency
    implementation (libs.firebase.database.ktx)
    //noinspection GradleDependency
    implementation (libs.kotlinx.coroutines.play.services)
    implementation (libs.androidx.databinding.runtime)
    //noinspection GradleDependency
    implementation (libs.androidx.work.runtime.ktx)
    implementation (libs.play.services.location)
}