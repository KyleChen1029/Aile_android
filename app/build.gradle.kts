import io.sentry.android.gradle.extensions.InstrumentationFeature
import java.text.SimpleDateFormat
import java.util.EnumSet

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("io.sentry.android.gradle") version "4.10.0"
    id("com.google.firebase.crashlytics")
}

apply(from = "../config.gradle.kts")

// 大幅調整使用者操作習慣或系統架構的發版 + 1, 第二，三，四碼歸
val verMajor = 3
// 事先规划的新需求迭代 + 1，第三，四碼歸零
val verMinor = 8
// 紧急插入需求或者主要修復缺陷創建小迭代 +1，迭代缺陷 hotfix
val verUpdate = 0
// 給 QA 測試的版本辨識
val qaUpdate = "07"

fun getVerBuild(): Int {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    val date = sdf.parse("2017/01/01 00:00:00")
    return ((System.currentTimeMillis() - date.time) / 1000).toInt()
}

fun Project.getVerBuildName(variant: String): String {
    val androidExtension = extensions.getByName("android") as com.android.build.gradle.BaseExtension
    val defaultConfig = androidExtension.defaultConfig
    return "Aile-${defaultConfig.versionName}-$variant-build-$qaUpdate-${getVerBuild()}.apk"
}

android {

    compileSdk = (extra["compileSdkVersion"] as Int)

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        configurations.all {
            resolutionStrategy {
                force("androidx.core:core:1.13.1")
                force("androidx.core:core-ktx:1.13.1")
            }
        }

        applicationId = "tw.com.chainsea.chat"
        minSdk = (extra["minSdkVersion"] as Int)
        targetSdk = (extra["targetSdkVersion"] as Int)
        versionCode = getVerBuild()
        versionName = "$verMajor.$verMinor.$verUpdate"
        multiDexEnabled = true
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../tw.com.chainsea.chat.jks")
            storePassword = "jiding123"
            keyAlias = "key"
            keyPassword = "jiding123"
        }
        create("release") {
            storeFile = file("../tw.com.chainsea.chat.jks")
            storePassword = "jiding123"
            keyAlias = "key"
            keyPassword = "jiding123"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            androidComponents {
                onVariants { variant ->
                    if (variant.buildType == "debug") {
                        variant.outputs.forEach { output ->
                            if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                                output.outputFileName = getVerBuildName("debug")
                            }
                        }
                    }
                }
            }

            buildConfigField("boolean", "isCpMode", "true")
            buildConfigField("String", "qaUpdate", "\"$qaUpdate\"")
            buildConfigField("String", "BUILD_TYPE", "\"debug\"")
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            androidComponents {
                onVariants { variant ->
                    if (variant.buildType == "release") {
                        variant.outputs.forEach { output ->
                            if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                                output.outputFileName = getVerBuildName("release")
                            }
                        }
                    }
                }
            }

            buildConfigField("boolean", "isCpMode", "true")
            buildConfigField("String", "qaUpdate", "\"$qaUpdate\"")
            buildConfigField("String", "BUILD_TYPE", "\"release\"")
        }
    }
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            isDefault = true
            applicationIdSuffix = ".dev"
        }

        create("qa") {
            applicationIdSuffix = ".qa"
            dimension = "environment"
        }

        create("uat") {
            applicationIdSuffix = ".uat"
            dimension = "environment"
        }

        create("beta") {
        }

        create("prod") {
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    namespace = "tw.com.chainsea.chat"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
    api("androidx.multidex:multidex:2.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api("androidx.cardview:cardview:1.0.0")
    api("com.google.android.material:material:1.12.0")
    api("androidx.legacy:legacy-support-v4:1.0.0")
    implementation(project(":pickerview"))
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")

    // navigation
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")

    api(project(":ce_sdk"))
    api(project(":custom_view"))
    implementation("com.github.CPPAlien:DaVinci:1.3.2")

    api("com.github.houbb:pinyin:0.3.0")

    api("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    api("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    val playerVersion = "2.19.1"
    api("com.google.android.exoplayer:exoplayer-core:$playerVersion")
    api("com.google.android.exoplayer:exoplayer-dash:$playerVersion")
    api("com.google.android.exoplayer:exoplayer-hls:$playerVersion")
    api("com.google.android.exoplayer:exoplayer-smoothstreaming:$playerVersion")
    api("com.google.android.exoplayer:exoplayer-ui:$playerVersion")

    // Permissions
    implementation("com.github.getActivity:XXPermissions:13.2")

    // Picture Selector
    implementation("io.github.lucksiege:pictureselector:v3.11.1")

    implementation("androidx.room:room-runtime:2.4.0-alpha03")
    ksp("androidx.room:room-compiler:2.4.0-alpha03")
    // lottie
    implementation("com.airbnb.android:lottie:4.2.1")
    // shimmer loading
    implementation("com.github.skydoves:androidveil:1.1.2")
    implementation("com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.7")
    implementation("com.github.anzaizai:EasySwipeMenuLayout:1.1.4")
    implementation("net.danlew:android.joda:2.10.14")
    implementation("io.getstream:avatarview-glide:1.0.4")
    kapt("androidx.lifecycle:lifecycle-common-java8:2.4.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.1")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.work:work-runtime:2.7.1")

    // lifecycle
    implementation("android.arch.lifecycle:runtime:1.1.1")
    kapt("android.arch.lifecycle:common-java8:1.1.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("com.github.centerzx:ShapeBlurView:1.0.5")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // moshi
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")

    implementation("android.arch.navigation:navigation-fragment-ktx:1.0.0")

    // startup
    implementation("androidx.startup:startup-runtime:1.1.1")
//    //Firebase
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
    implementation("com.google.firebase:firebase-messaging-directboot:24.0.0")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:latest.release")
    implementation("com.facebook.android:facebook-login:latest.release")

    // Google Play
    implementation("com.google.android.play:asset-delivery:2.2.2")
    // For Kotlin users also import the Kotlin extensions library for Play Asset Delivery:
    implementation("com.google.android.play:asset-delivery-ktx:2.2.2")
    implementation("com.google.android.play:feature-delivery:2.1.0")
    // For Kotlin users, also import the Kotlin extensions library for Play Feature Delivery:
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")
    implementation("com.google.android.play:review:2.0.1")
    // For Kotlin users, also import the Kotlin extensions library for Play In-App Review:
    implementation("com.google.android.play:review-ktx:2.0.1")
    implementation("com.google.android.play:app-update:2.1.0")
    // For Kotlin users, also import the Kotlin extensions library for Play In-App Update:
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Picasso
    implementation("com.squareup.picasso:picasso:2.8")

    implementation("commons-net:commons-net:3.6")

    implementation("org.jsoup:jsoup:1.12.1")

    implementation("com.github.omegaes:Android-Link-Preview:1.0.2")

    implementation("androidx.media3:media3-exoplayer:1.4.0")

    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    implementation("com.google.zxing:core:3.4.1")

    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // 可以查看 app 是否有 memory leak 的工具
//        debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
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

tasks.whenTaskAdded {
    if (name == "assembleDevDebug") {
        val sourceFile = file("${rootProject.projectDir}/pre-commit")
        val targetDir = file("${rootProject.projectDir}/.git/hooks")
        if (sourceFile.exists()) {
            copy {
                from(sourceFile)
                into(targetDir)
            }
            println("✅ pre-commit hook 已複製到 .git/hooks")
        } else {
            println("⚠️ 找不到 pre-commit 檔案，跳過複製")
        }
    }
}
