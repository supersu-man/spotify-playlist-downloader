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
        versionCode = 17
        versionName = "1.7.3"
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
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.compose.ui:ui:1.8.1")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling:1.8.1")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    implementation("com.squareup.okhttp3:okhttp-urlconnection:5.0.0-alpha.14")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.github.supersu-man:apkupdater-library:v2.0.1")
    implementation("se.michaelthelin.spotify:spotify-web-api-java:8.0.0")
    implementation("com.github.teamnewpipe:NewPipeExtractor:v0.24.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


}