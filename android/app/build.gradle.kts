plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// ---------------------------------------------------------------------------
// Content SDK - integration Step 2 (dependencies)
//
// From the Android SDK package (ContentSdk-Android-<version>):
// 1. Copy `ContentSdk-<version>.aar` into `app/libs/`.
// 2. Copy `content-sdk-dependencies.gradle` into `app/` (next to this file).
// Both files MUST come from the same package. Nothing else needs editing:
// the AAR file name is detected below.
// ---------------------------------------------------------------------------
val contentSdkAarName: String = file("libs")
    .listFiles { file -> file.extension == "aar" && file.name.startsWith("ContentSdk") }
    .orEmpty()
    .let { aars ->
        check(aars.size == 1) {
            if (aars.isEmpty()) {
                "Content SDK AAR not found. Copy ContentSdk-<version>.aar from the SDK package into app/libs/ (see README)."
            } else {
                "Found ${aars.size} Content SDK AARs in app/libs/ (${aars.joinToString { it.name }}). Keep only one."
            }
        }
        aars.single().nameWithoutExtension
    }

check(file("content-sdk-dependencies.gradle").isFile) {
    "content-sdk-dependencies.gradle not found. Copy it from the same SDK package into app/ (see README)."
}

// Adds every library the SDK needs at runtime (AndroidX, media3, ads, ...).
apply(from = "content-sdk-dependencies.gradle")

android {
    namespace = "com.lc.content.sdk.demo"
    // Content SDK requires compileSdk >= 37 and minSdk >= 24.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.lc.content.sdk.demo"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            // The SDK ships its own consumer ProGuard rules, so shrinking works out of the box.
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Content SDK (local AAR resolved through the `flatDir` repository in settings.gradle.kts).
    // Do NOT use `files("libs/xxx.aar")` - some AGP versions fail IDE sync with it.
    add("implementation", mapOf("name" to contentSdkAarName, "ext" to "aar"))

    // ---- Everything below is only for this demo's own UI, not required by the SDK ----
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.11.0")
    // Needed only for the Fragment example in SdkApiCatalog.kt.
    implementation("androidx.fragment:fragment-ktx:1.8.5")
}
