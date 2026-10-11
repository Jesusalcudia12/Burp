apply plugin: "com.android.application"
apply plugin: "kotlin.android"

android {
    compileSdk = 30  // 4.2.2 funciona bien hasta aquí

    defaultConfig {
        applicationId = "com.burpmobile"
        minSdk = 23
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
}
