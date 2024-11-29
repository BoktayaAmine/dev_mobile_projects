plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.apollographql.apollo") version "4.1.0"
}

android {
    namespace = "ma.ensaj.comptes_appllo"
    compileSdk = 34

    defaultConfig {
        applicationId = "ma.ensaj.comptes_apollo"
        minSdk = 31
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0") // Replace 1.15.0 with 1.12.0
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.apollographql.apollo:apollo-runtime:4.1.0")
    implementation("com.apollographql.apollo:apollo-normalized-cache:4.1.0")
// If you want caching support

    // RecyclerView depende
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Other necessary dependencies like Coroutine and lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

}
apollo {
    service("service") {
        packageName.set("ma.ensaj")
        schemaFile.set(file("src/main/graphql/ma/ensaj/schema.graphqls"))
    }

}