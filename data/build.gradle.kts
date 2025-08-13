plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // Kotlin Serialization
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoner.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {

    implementation(project(":domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Kotlinx JSON serialization
    implementation(libs.kotlinx.serialization.json)
    // Retrofit
    implementation(libs.retrofit)
    // Gson converter for Retrofit
    implementation(libs.converter.gson)
    //Logging
    implementation(libs.logging.interceptor)
    //DataStore
    implementation(libs.androidx.datastore.preferences)
    // Kotlin Date Time
    implementation(libs.kotlinx.datetime)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // KSP
    ksp(libs.androidx.room.compiler)
    //WorkManager
    implementation(libs.androidx.work.runtime.ktx)

}