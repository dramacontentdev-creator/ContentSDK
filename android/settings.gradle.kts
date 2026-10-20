pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        // Content SDK - integration Step 1 (repositories)
        // The SDK uses Google Mobile Ads with mediation adapters. These vendor
        // repositories are required to resolve them. Keep all three.
        maven("https://android-sdk.is.com/")
        maven("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven("https://artifact.bytedance.com/repository/pangle/")

        // Folder that holds the downloaded ContentSdk-<version>.aar.
        flatDir {
            dirs("app/libs")
        }
    }
}

rootProject.name = "ContentSdkDemo"
include(":app")
