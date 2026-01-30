import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.sumanth.spd"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.sumanth.spd"
        minSdk = 24
        targetSdk = 36
        versionCode = 22
        versionName = "1.10.0"
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
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.2")

    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.navigation:navigation-compose:2.9.6")

    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.squareup.okhttp3:okhttp-urlconnection:5.3.2")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    implementation("se.michaelthelin.spotify:spotify-web-api-java:9.4.0")
    implementation("com.github.teamnewpipe:NewPipeExtractor:v0.25.1")
    implementation("com.github.supersu-man:apkupdater-library:v2.2.0")

    implementation("com.arthenica:smart-exception-java:0.2.1")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
}