plugins {
    id("com.android.application")
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization") version "2.3.20"
    id("com.android.legacy-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
}

setupMainApk()

kapt {
    correctErrorTypes = true
    useBuildCache = true
    mapDiagnosticLocations = true
    javacOptions {
        option("-Xmaxerrs", "1000")
    }
}

android {
    buildFeatures {
        dataBinding = true
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    defaultConfig {
        proguardFile("proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}

dependencies {
    implementation(project(":core"))
    coreLibraryDesugaring(libs.jdk.libs)

    implementation(libs.rikka.layoutinflater)
    implementation(libs.rikka.insets)
    implementation(libs.rikka.recyclerview)

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.recyclerview)
    implementation(libs.transition)
    implementation(libs.fragment.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.9.7")

    // Navigation 3 + Miuix NavDisplay
    implementation(libs.navigation3.runtime)
    implementation(libs.lifecycle.viewmodel.navigation3)
    implementation(libs.navigationevent.compose)
    implementation(libs.miuix.navigation3.ui)

    // Miuix
    implementation(libs.miuix)
    implementation(libs.miuix.icons)

    // Haze
    implementation(libs.haze)

    // Backdrop (Liquid Glass)
    implementation(libs.backdrop)
    implementation(libs.capsule)

    // Room
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(libs.miuix)
    implementation(libs.miuix.icons)

    // WebKit (WebViewAssetLoader)
    implementation("androidx.webkit:webkit:1.13.0")

    // Haze (Gaussian blur)
    implementation(libs.haze)

    // Backdrop (Liquid Glass)
    implementation(libs.backdrop)
    implementation(libs.capsule)

    // Make sure kapt runs with a proper kotlin-stdlib
    kapt(kotlin("stdlib"))
}

