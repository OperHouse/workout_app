plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.workoutapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.workoutapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            // Исключаем конфликтующие файлы из сборки
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
            // На всякий случай добавим другие частые конфликты для JavaMail
            excludes += "/META-INF/LICENSE-spec.txt"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.preference)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("androidx.sqlite:sqlite:2.6.2")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")

    implementation ("it.xabaras.android:recyclerview-swipedecorator:1.4")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.paging:paging-runtime:3.3.6")
    implementation("androidx.health.connect:connect-client:1.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
    implementation("com.google.guava:guava:33.5.0-android")

    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.google.android.material:material:1.13.0")

    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("com.airbnb.android:lottie:6.7.1")


    implementation("androidx.paging:paging-common:3.3.6")


    implementation("com.sun.mail:android-mail:1.6.8")
    implementation("com.sun.mail:android-activation:1.6.8")



    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
}