plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "app.oie"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.oie" // permanent (decision 0002): never rename
        minSdk = 26
        targetSdk = 35
        versionCode = 23
        versionName = "0.23.0"
    }

    // Release signing comes only from CI secrets (docs/release.md). Without them the release build is unsigned.
    val ks = System.getenv("OIE_KEYSTORE_FILE")
    signingConfigs {
        if (ks != null) create("release") {
            storeFile = file(ks)
            storePassword = System.getenv("OIE_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("OIE_KEY_ALIAS")
            keyPassword = System.getenv("OIE_KEY_PASSWORD")
        }
    }

    // One APK per architecture (about half the size), plus the universal one that older copies of
    // Oiê and manual installs use (docs/release.md). Only ARM: the WhatsApp link is built for ARM.
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = true
        }
    }

    buildTypes {
        release {
            if (ks != null) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.zxing.core)
    // The WhatsApp link (link/, whatsmeow via gomobile). Build it first: link/build-aar.sh.
    implementation(files("../link/oielink.aar"))
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
