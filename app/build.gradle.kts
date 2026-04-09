plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.play.publisher)
}

fun semverToVersionCode(version: String): Int {
    val parts = version.split(".")
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return major * 10_000 + minor * 100 + patch
}

val releaseVersionName = providers.gradleProperty("releaseVersionName")
    .orElse(providers.environmentVariable("RELEASE_VERSION_NAME"))
    .orElse("0.0.1")
    .get()

val releaseVersionCode = providers.gradleProperty("releaseVersionCode")
    .orElse(providers.environmentVariable("RELEASE_VERSION_CODE"))
    .map(String::toInt)
    .orElse(semverToVersionCode(releaseVersionName))
    .get()

val releaseKeystorePath = providers.gradleProperty("androidKeystorePath")
    .orElse(providers.environmentVariable("ANDROID_KEYSTORE_PATH"))
    .orNull

val releaseKeystorePassword = providers.gradleProperty("androidKeystorePassword")
    .orElse(providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD"))
    .orNull

val releaseKeyAlias = providers.gradleProperty("androidKeyAlias")
    .orElse(providers.environmentVariable("ANDROID_KEY_ALIAS"))
    .orNull

val releaseKeyPassword = providers.gradleProperty("androidKeyPassword")
    .orElse(providers.environmentVariable("ANDROID_KEY_PASSWORD"))
    .orNull

val playServiceAccountFile = providers.gradleProperty("playServiceAccountFile")
    .orElse(providers.environmentVariable("PLAY_SERVICE_ACCOUNT_FILE"))
    .orNull

val hasReleaseSigning = listOf(
    releaseKeystorePath,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.zhravan.noechat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zhravan.noechat"
        minSdk = 26
        targetSdk = 35
        versionCode = releaseVersionCode
        versionName = releaseVersionName
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(requireNotNull(releaseKeystorePath))
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    }
}

play {
    track.set("internal")
    defaultToAppBundles.set(true)
    if (!playServiceAccountFile.isNullOrBlank()) {
        serviceAccountCredentials.set(file(playServiceAccountFile))
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    debugImplementation(libs.ui.tooling)

    testImplementation(libs.junit)
}
