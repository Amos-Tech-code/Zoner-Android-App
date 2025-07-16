import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Kotlin Serialization
    alias(libs.plugins.kotlin.serialization)
    // Kotlin Parcelize
    id("kotlin-parcelize")
}

android {
    namespace = "com.zoner.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zoner.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val secretProperties = Properties()
    val secretPropertiesFile = File(rootDir, "secret.properties")
    if (secretPropertiesFile.exists() && secretPropertiesFile.isFile) {
        secretPropertiesFile.inputStream().use {
            secretProperties.load(it)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Store secrets securely in BuildConfig
            buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"${secretProperties.getProperty("GOOGLE_SERVER_CLIENT_ID")}\"")

        }

        debug {
            // Store secrets securely in BuildConfig
            buildConfigField("String", "GOOGLE_SERVER_CLIENT_ID", "\"${secretProperties.getProperty("GOOGLE_SERVER_CLIENT_ID")}\"")
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

    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Koin
    implementation(libs.koin.android)
    implementation(libs.koin.android.compose)

    //Navigation
    implementation(libs.androidx.navigation.compose)
    //Adaptive Navigation
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    // Adaptive Layouts
    implementation(libs.material3.adaptive)
    //Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    // Kotlinx JSON serialization
    implementation(libs.kotlinx.serialization.json)
    //Material3-Extended icons
    implementation (libs.androidx.material.icons.extended)
    //SplashScreen
    implementation(libs.androidx.core.splashscreen)
    //Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    //Google
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    //Foundation Permissions API
    implementation(libs.androidx.foundation)
    //Accompanist Permissions
    implementation(libs.accompanist.permissions)
    //Kotlin Date Time
    implementation(libs.kotlinx.datetime)
    // ExoPlayer for video playback
    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-ui:1.7.1")
    // Camera
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("androidx.camera:camera-extensions:1.4.2")

}