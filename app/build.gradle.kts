plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.sumanth.spd"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.sumanth.spd"
        minSdk = 24
        targetSdk = 35
        versionCode = 19
        versionName = "1.8.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.07.00"))
    implementation("androidx.compose.ui:ui:1.8.3")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling:1.8.3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.navigation:navigation-compose:2.9.3")

    implementation("com.squareup.okhttp3:okhttp-urlconnection:5.1.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.github.supersu-man:apkupdater-library:v2.0.1")
    implementation("se.michaelthelin.spotify:spotify-web-api-java:9.3.0")
    implementation("com.github.teamnewpipe:NewPipeExtractor:v0.24.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation("com.arthenica:smart-exception-java:0.2.1")
}