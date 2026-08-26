plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.mikepenz.aboutlibraries.plugin") version "15.1.1"
}

android {
    namespace = "com.czczypsoson.datausagewidget"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.czczypsoson.datausagewidget"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Odstraní nepoužitý kód
            isShrinkResources = true // Odstraní nepoužité zdroje (obrázky atd.)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material3)
    // Glance pro tvorbu Jetpack Compose widgetů
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}