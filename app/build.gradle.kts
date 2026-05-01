plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.efficientdet_lite"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.efficientdet_lite"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    androidResources {
        noCompress += "tflite"
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
/*
    bundle {
        deviceTargetingConfig {
            config.set(file("device_targeting_configuration.xml"))
        }
    }
*/
    dynamicFeatures += setOf(
        ":litert_npu_runtime_libraries:qualcomm_runtime_v69",
        ":litert_npu_runtime_libraries:qualcomm_runtime_v73",
        ":litert_npu_runtime_libraries:qualcomm_runtime_v75",
        ":litert_npu_runtime_libraries:qualcomm_runtime_v79",
        ":litert_npu_runtime_libraries:qualcomm_runtime_v81"
    )
}

//tasks.register("prepareKotlinBuildScriptModel") {
//    // Dummy task to satisfy the IDE sync
//}


tasks.register("prepareKotlinBuildScriptModel") {}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.opencv)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.litert)
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.5.0-alpha18")

    implementation("androidx.compose.material:material-icons-extended")
    implementation(project(":litert_npu_runtime_libraries:runtime_strings"))
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
