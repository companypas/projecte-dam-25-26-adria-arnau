plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "com.example.pi_androidapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pi_androidapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL base de la API - cambiar según el entorno
        //buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8069/\"")
        buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.116:8069/\"")

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
        buildConfig = true
    }

    // SQLDelight 2.x + AGP 8.x: el directorio generado no siempre se registra
    // automáticamente como source set. Sin esto, KSP (Hilt) no encuentra AppDatabase
    // aunque el archivo exista en disco.
    sourceSets {
        getByName("debug") {
            java.srcDir("build/generated/sqldelight/code/AppDatabase/debug")
        }
        getByName("release") {
            java.srcDir("build/generated/sqldelight/code/AppDatabase/release")
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.pi_androidapp.data.local")
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)

    // Navigation
    implementation(libs.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    // DataStore + Security
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)

    // Image Loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.coroutines.android)

    // Google Maps
    implementation(libs.maps.compose)

    // SQLDelight
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.coroutines.extensions)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Asegura que SQLDelight genere AppDatabase antes de que KSP (Hilt) lo procese.
// Tanto KSP como SQLDelight registran sus tasks de forma lazy, por eso afterEvaluate
// + findByName falla (devuelve null). tasks.configureEach a nivel raíz se ejecuta
// cuando Gradle registra cada task, independientemente de si es lazy o eager.
tasks.configureEach {
    if (name == "kspDebugKotlin") {
        dependsOn("generateDebugAppDatabaseInterface")
    }
    if (name == "kspReleaseKotlin") {
        dependsOn("generateReleaseAppDatabaseInterface")
    }
}