plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.kazakago.storeflowable.example"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":storeflowable"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("io.coil-kt:coil:1.4.0")
    implementation("com.github.lisawray.groupie:groupie:2.10.0")
    implementation("com.github.lisawray.groupie:groupie-viewbinding:2.10.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
