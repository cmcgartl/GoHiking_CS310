plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gohiking_cs310"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gohiking_cs310"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-base:18.1.0")
    implementation("com.google.firebase:firebase-firestore:24.1.0")
    implementation("com.google.firebase:firebase-auth:21.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.play.services.maps)
    implementation(libs.constraintlayout)
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(libs.cardview)
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0") // JUnit 5 for unit tests
    testImplementation("org.mockito:mockito-core:5.6.0") // Mockito core
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0") // Mockito Kotlin
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-inline:4.11.0") // For mocking final classes if needed
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(libs.ext.junit) // Android JUnit
    androidTestImplementation(libs.espresso.core) // Espresso for UI testing
    androidTestImplementation("org.mockito:mockito-android:5.6.0")
    androidTestImplementation(libs.testng) // Mockito for instrumentation tests
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.material:material:1.12.0")

}

buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

secrets {
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}
