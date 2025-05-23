plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.umtualgames.squadmaster"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.umtualgames.squadmaster"
        minSdk = 23
        targetSdk = 35
        versionCode = 35
        versionName = "1.6.2"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.kotlin.stdlib.jdk8)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.extensions)
    implementation(libs.rxandroid)
    implementation(libs.rxjava)
    implementation(libs.rxbinding)
    implementation(libs.multidex)
    implementation(libs.hawk)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.flexbox)
    implementation(libs.lottie)
    implementation(libs.swiperefreshlayout)
    implementation(libs.play.services.ads)
    implementation(libs.viewpager2)
    implementation(libs.eventbus)
    implementation(libs.play.services.auth)
    implementation(libs.review)
    implementation(libs.review.ktx)
    implementation(libs.onesignal)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.glide.transformations)
    implementation(libs.carouselrecyclerview)
    implementation(libs.bundles.network)
    implementation(libs.billing)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.unityads)
    implementation(platform(libs.firebase.bom))
}