plugins {
    // Content SDK requires Kotlin 2.3.10 (or a compatible 2.3.x).
    // AGP 9 compiles Kotlin itself; the Compose compiler plugin pins the Kotlin version.
    id("com.android.application") version "9.3.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
}
