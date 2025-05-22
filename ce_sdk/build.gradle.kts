import io.sentry.android.gradle.extensions.InstrumentationFeature
import java.util.EnumSet

plugins {
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("io.sentry.android.gradle") version "4.10.0"
}

apply(from = "../config.gradle.kts")

android {
    compileSdk = (extra["compileSdkVersion"] as Int)

    defaultConfig {
        minSdk = (extra["minSdkVersion"] as Int)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
//    kotlinOptions {
//        jvmTarget = "17"
//    }
    namespace = "tw.com.chainsea.ce.sdk"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":android_common"))
    api("pl.droidsonroids.gif:android-gif-drawable:1.1.17")

    // 全局事件廣播
    api("org.greenrobot:eventbus:3.3.1")

    api("io.reactivex.rxjava2:rxjava:2.2.21")
    api("io.reactivex.rxjava2:rxandroid:2.1.1")
    api(project(":custom_view"))
    api("io.socket:socket.io-client:1.0.0") {
        // excluding org.json which is provided by Android
        exclude(group = "org.json", module = "json")
    }
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // moshi
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-adapters:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // viewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-analytics")

    //Picasso
    implementation("com.squareup.picasso:picasso:2.8")

    implementation ("io.sentry:sentry-android:7.12.0")
    implementation ("io.sentry:sentry-okhttp:7.12.0")
    sentry {
        org.set("aile-cloud")
        projectName.set("android")
        includeSourceContext.set(true)
        tracingInstrumentation {
            enabled.set(true)
            features.set(EnumSet.allOf(InstrumentationFeature::class.java) - InstrumentationFeature.OKHTTP)
        }
    }
}
