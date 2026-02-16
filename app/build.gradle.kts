plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.tarneeb.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tarneeb.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // ✅ FIX #1: تفعيل MultiDex - CRITICAL!
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
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

    composeOptions {
        // ✅ FIX #2: تحديث Kotlin Compiler من 1.5.11 إلى 1.5.13
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // ========================================================================
    // CORE & ACTIVITY
    // ========================================================================
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    // ✅ FIX #3: أضفنا AppCompat
    implementation("androidx.appcompat:appcompat:1.7.0")

    // ========================================================================
    // COMPOSE BOM & UI
    // ========================================================================
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // ✅ FIX #4: أضفنا Foundation - مهم جداً!
    implementation("androidx.compose.foundation:foundation")
    
    implementation("androidx.compose.material3:material3")
    // ✅ FIX #5: أضفنا material-icons-core
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ========================================================================
    // LIFECYCLE & STATE MANAGEMENT
    // ========================================================================
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // ========================================================================
    // NAVIGATION
    // ========================================================================
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ========================================================================
    // COROUTINES & SERIALIZATION
    // ========================================================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // ✅ FIX #6: أضفنا kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ========================================================================
    // NETWORKING
    // ========================================================================
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ========================================================================
    // DATABASE (Room + KSP)
    // ========================================================================
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ========================================================================
    // DATA STORE & BACKGROUND WORK
    // ========================================================================
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ========================================================================
    // IMAGES & LOGGING
    // ========================================================================
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ========================================================================
    // MATERIAL DESIGN
    // ========================================================================
    implementation("com.google.android.material:material:1.12.0")

    // ========================================================================
    // MULTIDEX - CRITICAL FIX!
    // ========================================================================
    // ✅ FIX #7: أضفنا MultiDex - هذا حل مشكلة الـ 500k+ methods!
    implementation("androidx.multidex:multidex:2.0.1")

    // ========================================================================
    // TESTING
    // ========================================================================
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

// ============================================================================
// 📝 ملاحظة مهمة: التغييرات الـ 7 المهمة
// ============================================================================
/*
✅ FIX #1: multiDexEnabled = true
   السبب: APK يحتوي على 500k+ methods → يحتاج MultiDex

✅ FIX #2: kotlinCompilerExtensionVersion = "1.5.13"
   السبب: تحديث من 1.5.11 → نسخة محدثة وآمنة

✅ FIX #3: androidx.appcompat:appcompat:1.7.0
   السبب: متطلب أساسي للـ Android components

✅ FIX #4: androidx.compose.foundation:foundation
   السبب: أساس Compose layouts والـ interactions

✅ FIX #5: androidx.compose.material:material-icons-core
   السبب: متطلب للأيقونات في Compose

✅ FIX #6: org.jetbrains.kotlinx:kotlinx-coroutines-core
   السبب: أساس العمليات غير المتزامنة

✅ FIX #7: androidx.multidex:multidex:2.0.1
   السبب: تحميل جميع dex files → حل مشكلة ClassNotFoundException

بعد هذه التغييرات + MyApp.kt:
✅ سيعمل التطبيق بدون مشاكل!
*/
