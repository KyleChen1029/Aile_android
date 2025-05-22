plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val VERSION_CODE = 3
val VERSION_NAME = "0.0.3"

apply(from = "../config.gradle.kts")

android {
    compileSdk = extra["compileSdkVersion"] as Int

    defaultConfig {
        minSdk = extra["minSdkVersion"] as Int
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    resourcePrefix = "tw.com.chainsea.custom.view"
    namespace = "tw.com.chainsea.custom.view"
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api(project(":android_common"))
    api("androidx.appcompat:appcompat:1.7.0")
    api("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-ktx:1.13.1")
}
