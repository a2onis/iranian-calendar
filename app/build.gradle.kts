plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ir.gahshomar.parsi"
    compileSdk = 34

    defaultConfig {
        applicationId = "ir.gahshomar.parsi"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "2.2.0"
        vectorDrawables { useSupportLibrary = true }
    }

    // کلید امضای نصبِ بیرون‌از‌فروشگاه (همان کلید در همهٔ نسخه‌ها تا به‌روزرسانی ممکن باشد).
    // برای انتشار در فروشگاه، کلید خودتان را جای آن بگذارید.
    signingConfigs {
        create("sideload") {
            storeFile = file("keystore/gahshomar-demo.p12")
            storeType = "PKCS12"
            storePassword = "gahshomar"
            keyAlias = "gahshomar"
            keyPassword = "gahshomar"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            // کوچک‌سازی و بهینه‌سازی کد: برنامه سبک‌تر و بسیار سریع‌تر بالا می‌آید
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("sideload")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xjvm-default=all")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
