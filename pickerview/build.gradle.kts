plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

apply(from = "../config.gradle.kts")

val verMajor = 1
val verMinor = 0
val verUpdate = 0

afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication::class) {
                from(components["release"])
                groupId = "com.bigkoo.pickerview"
                artifactId = "pickerview"
                version = "$verMajor.$verMinor.$verUpdate"
            }
        }
    }
}

extra["bintrayRepo"] = "maven"
extra["bintrayName"] = "pickerview"
extra["publishedGroupId"] = "com.ramesh.mypicker"
extra["libraryName"] = "PickerVIew"
extra["artifact"] = "pickerview"
extra["libraryDescription"] = "Picker View For Android like IOS"
extra["siteUrl"] = "https://github.com/RameshBhupathi/PickerView"
extra["gitUrl"] = "https://github.com/RameshBhupathi/PickerView.git"
extra["libraryVersion"] = "1.0"
extra["developerId"] = "ramesh"
extra["developerName"] = "Ramesh Bhupathi"
extra["developerEmail"] = "bhupatiramesh@gmail.com"
extra["licenseName"] = "The Apache Software License, Version 2.0"
extra["licenseUrl"] = "http://www.apache.org/licenses/LICENSE-2.0.txt"
extra["allLicenses"] = arrayOf("Apache-2.0")

android {
    compileSdk = (extra["compileSdkVersion"] as Int)

    defaultConfig {
        minSdk = (extra["minSdkVersion"] as Int)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    resourcePrefix = "com.bigkoo.pickerview"
    namespace = "com.bigkoo.pickerview"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.13.1")
}
