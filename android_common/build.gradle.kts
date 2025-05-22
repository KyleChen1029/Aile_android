plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

apply(from = "../config.gradle.kts")

android {
    compileSdk = (extra["compileSdkVersion"] as Int)

    defaultConfig {
        minSdk = (extra["minSdkVersion"] as Int)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_ID", project.findProperty("API_ID") as String)
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_ID", project.findProperty("API_ID") as String)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    resourcePrefix("tw.com.chainsea.android.common")

    namespace = "tw.com.chainsea.android.common"
}

dependencies {
    api("com.google.android.material:material:1.12.0")
    api("androidx.legacy:legacy-support-v4:1.0.0")
    api("androidx.appcompat:appcompat:1.7.0")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.squareup.okhttp3:logging-interceptor:4.12.0")
    api("com.google.guava:guava:33.2.1-android")
    api("com.google.code.gson:gson:2.11.0")
    api("com.google.android.gms:play-services-vision:20.1.3")
    api("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    api("com.github.bumptech.glide:glide:4.16.0")
    api("me.leolin:ShortcutBadger:1.1.22")
    implementation("androidx.core:core-ktx:1.13.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-crashlytics")
}
