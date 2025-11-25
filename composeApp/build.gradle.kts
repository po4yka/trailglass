import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.secrets)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

android {
    namespace = "com.po4yka.trailglass"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.po4yka.trailglass"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        lintConfig = file("$rootDir/config/android-lint.xml")
        abortOnError = true
        checkAllWarnings = true
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")

        disable.addAll(
            listOf(
                "MissingTranslation",
                "ExtraTranslation",
                "LintError" // Ignore false positives about config directory
            )
        )

        // Ignore the config directory (false positive)
        disable += setOf("config")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.utils)

    // Preferences
    implementation(libs.datastore.preferences)

    // Background work
    implementation(libs.androidx.work.runtime)

    // Input motion prediction for low-latency touch/stylus input
    implementation(libs.androidx.input.motionprediction)

    // Paging 3 for pagination support
    implementation(libs.androidx.paging.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.perf)

    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.decompose)
    implementation(libs.decompose.extensions.compose)

    // Serialization (required for Decompose navigation)
    implementation(libs.kotlinx.serialization.json)

    // DateTime (for kotlinx.datetime types from shared module)
    implementation(libs.kotlinx.datetime)

    // Logging
    implementation(libs.kotlin.logging)

    implementation(projects.shared)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Detekt plugins
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.compose.rules.detekt)

    // Compose Lint checks
    lintChecks(libs.slack.compose.lints)
}

// Secrets plugin configuration
// API keys should be stored in local.properties:
// MAPS_API_KEY=your_google_maps_api_key_here
secrets {
    // Default properties file for API keys
    propertiesFileName = "local.properties"

    // Default secrets file (optional)
    defaultPropertiesFileName = "local.defaults.properties"

    // Ignore missing secrets in builds (useful for CI/CD)
    ignoreList.add("sdk.*")
}

// ktlint configuration
ktlint {
    version.set("1.5.0")
    android.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    ignoreFailures.set(true) // Ignore violations in generated code

    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

// detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    baseline = file("$rootDir/config/detekt/baseline.xml")

    source.setFrom("src/main/kotlin")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(true)
    }
}
