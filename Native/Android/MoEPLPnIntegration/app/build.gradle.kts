import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

// Load secrets from the gitignored secrets.properties at the repo root,
// falling back to environment variables (useful for CI). The real file is
// never committed - see secrets.properties.example.
val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties()
if (secretsFile.exists()) {
    secretsFile.inputStream().use { secrets.load(it) }
}

fun secret(key: String): String =
    secrets.getProperty(key) ?: System.getenv(key) ?: ""

android {
    namespace = "com.vamsi.moe_pl_pn_integration"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.vamsi.moe_pl_pn_integration"
        // MoEngage SDK requires a minimum SDK of 23.
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Secrets exposed to Kotlin code via the generated BuildConfig class.
        // Data center code maps to the MoEngage DataCenter (1..6).
        buildConfigField("String", "MOENGAGE_APP_ID", "\"${secret("MOENGAGE_APP_ID")}\"")
        buildConfigField("String", "MOENGAGE_DATA_CENTER", "\"${secret("MOENGAGE_DATA_CENTER").ifBlank { "1" }}\"")
        buildConfigField("String", "MOE_USER_NAME", "\"${secret("MOE_USER_NAME")}\"")
        buildConfigField("String", "MOE_USER_ID", "\"${secret("MOE_USER_ID")}\"")
        buildConfigField("String", "MOE_USER_EMAIL", "\"${secret("MOE_USER_EMAIL")}\"")
        buildConfigField("String", "PLOTLINE_API_KEY", "\"${secret("PLOTLINE_API_KEY")}\"")
        buildConfigField("String", "PLOTLINE_USER_ID", "\"${secret("PLOTLINE_USER_ID")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // MoEngage core SDK + auto-integration FCM module (moe-push-firebase) +
    // In-App messaging + Rich Notification (push templates / rich media).
    // Versions are managed by the MoEngage BOM.
    implementation(platform(libs.moengage.bom))
    implementation(libs.moengage.android.sdk)
    implementation(libs.moengage.inapp)
    implementation(libs.moengage.rich.notification)

    // Firebase Cloud Messaging
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Plotline SDK
    implementation(libs.plotline.android.sdk)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}