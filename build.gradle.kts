// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id ("com.google.devtools.ksp") version "1.9.0-1.0.12" apply false
    id ("org.jetbrains.kotlin.jvm") version "1.8.20" // Use the latest version
    id ("org.jetbrains.kotlin.kapt") version "1.8.20" // Use the latest version
}