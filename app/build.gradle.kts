plugins {
    id("com.android.application") version "8.1.0"
}

android {
    namespace = "com.burpmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.burpmobile"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
