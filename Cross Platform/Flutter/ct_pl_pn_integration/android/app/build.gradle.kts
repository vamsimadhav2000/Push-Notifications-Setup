plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
    // Applied conditionally below - only when android/app/google-services.json exists locally.
}

/**
 * Reads a KEY=VALUE entry from the project-root secrets.env file (gitignored).
 * Returns [default] if the file or key is missing so builds never break on a fresh clone.
 */
fun readSecret(key: String, default: String = ""): String {
    val file = rootProject.file("../secrets.env")
    if (!file.exists()) return default
    return file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .map {
            val idx = it.indexOf('=')
            it.substring(0, idx).trim() to it.substring(idx + 1).trim()
        }
        .firstOrNull { it.first == key }
        ?.second
        ?: default
}

android {
    namespace = "com.example.ct_pl_pn_integration"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "com.example.ct_pl_pn_integration"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
        multiDexEnabled = true

        // CleverTap credentials injected from secrets.env (gitignored) so they stay local.
        manifestPlaceholders["CLEVERTAP_ACCOUNT_ID"] = readSecret("CLEVERTAP_ACCOUNT_ID")
        manifestPlaceholders["CLEVERTAP_ACCOUNT_TOKEN"] = readSecret("CLEVERTAP_ACCOUNT_TOKEN")
    }

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

// CleverTap auto-integration for FCM push. google-services.json is a local secret
// (gitignored), so the plugin is only applied when the file is present.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

flutter {
    source = "../.."
}

dependencies {
    // CleverTap FCM push + rich media rendering.
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    // CleverTap Push Templates SDK: renders rich media templates (timer, carousel,
    // rating, ...). Handled via NotificationService -> CTFcmMessageHandler.
    implementation("com.clevertap.android:push-templates:2.2.0")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.fragment:fragment:1.7.0")
    // MANDATORY for App Inbox.
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager:viewpager:1.0.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
}