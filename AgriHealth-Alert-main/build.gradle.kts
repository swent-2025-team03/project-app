// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}

sonar {
    properties {
        property("sonar.projectKey", "swent-2025-team03_project-app")
        property("sonar.organization", "swent-2025-team03")
    }
}