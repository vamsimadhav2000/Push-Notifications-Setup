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
    namespace = "com.vamsi.ct_pl_pnintegration"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.vamsi.ct_pl_pnintegration"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // CleverTap credentials injected into AndroidManifest.xml meta-data.
        manifestPlaceholders["clevertapAccountId"] = secret("CLEVERTAP_ACCOUNT_ID")
        manifestPlaceholders["clevertapToken"] = secret("CLEVERTAP_TOKEN")
        manifestPlaceholders["clevertapRegion"] = secret("CLEVERTAP_REGION")

        // Secrets exposed to Kotlin code via the generated BuildConfig class.
        buildConfigField("String", "PLOTLINE_API_KEY", "\"${secret("PLOTLINE_API_KEY")}\"")
        buildConfigField("String", "PLOTLINE_USER_ID", "\"${secret("PLOTLINE_USER_ID")}\"")
        buildConfigField("String", "CLEVERTAP_USER_NAME", "\"${secret("CLEVERTAP_USER_NAME")}\"")
        buildConfigField("String", "CLEVERTAP_USER_IDENTITY", "\"${secret("CLEVERTAP_USER_IDENTITY")}\"")
        buildConfigField("String", "CLEVERTAP_USER_EMAIL", "\"${secret("CLEVERTAP_USER_EMAIL")}\"")
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
    // CleverTap core SDK + Push Templates (rich media push)
    implementation(libs.clevertap.android.sdk)
    implementation(libs.clevertap.push.templates)

    // Firebase Cloud Messaging
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Plotline SDK
    implementation(libs.plotline.android.sdk)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
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