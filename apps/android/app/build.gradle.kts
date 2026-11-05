import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
}

val localApiProperties = Properties().apply {
    val contents = providers.fileContents(layout.projectDirectory.file("../local.properties")).asText.orNull
    if (contents != null) load(contents.reader())
}
val configuredApiUrl = providers.gradleProperty("apiBaseUrl")
    .orElse(providers.environmentVariable("ANDROID_API_BASE_URL"))
    .getOrElse(localApiProperties.getProperty("api.baseUrl", "http://10.0.2.2:3000/"))
    .let { if (it.endsWith('/')) it else "$it/" }
val apiUri = URI(configuredApiUrl)
require(apiUri.scheme in listOf("http", "https") && !apiUri.host.isNullOrBlank() && apiUri.userInfo == null && apiUri.query == null && apiUri.fragment == null) {
    "apiBaseUrl debe ser una URL HTTP(S) sin credenciales ni parámetros"
}
val validateReleaseApiUrl = tasks.register("validateReleaseApiUrl") {
    val targetUrl = configuredApiUrl
    doLast { check(URI(targetUrl).scheme == "https") { "Configura apiBaseUrl con HTTPS para compilar release" } }
}
tasks.configureEach { if (name == "preReleaseBuild") dependsOn(validateReleaseApiUrl) }

android {
    namespace = "com.app.android"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.app.android"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"${configuredApiUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.runtime:runtime:1.12.1")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor) // Intercepta los json de llamadas HTTP
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.coil)
}
