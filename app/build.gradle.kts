plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    alias(libs.plugins.dagger.hilt)
}

kapt {
    // Tell kapt to be more lenient about unresolved types in generated stubs
    correctErrorTypes = true
}

android {
    namespace = "com.codewithfk.expensetracker.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.codewithfk.expensetracker.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // Enable desugaring for java.time APIs on older devices
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Material Components (Android) for XML themes (Theme.MaterialComponents.DayNight)
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.constraintlayout)
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    implementation(libs.dagger.hilt.andriod)
    kapt(libs.dagger.hilt.compiler)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.compose.navigation)
    implementation(libs.dagger.hilt.compose)
    implementation("androidx.compose.foundation:foundation:1.7.0-beta07")
    // Provide the Material icons (Visibility, VisibilityOff) via the icons-extended artifact.
    implementation("androidx.compose.material:material-icons-extended:1.5.1")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Desugaring library to allow java.time on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // WorkManager for background daily processing
    implementation("androidx.work:work-runtime-ktx:2.8.1")
}