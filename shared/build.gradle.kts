import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    id("org.jetbrains.kotlinx.kover") version "0.9.3"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
        iosTarget.compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlin.logging)
            implementation(libs.kotlin.inject.runtime)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Ktor client for networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // Compass - Location toolkit
            implementation(libs.compass.core)
            implementation(libs.compass.geocoder)
            implementation(libs.compass.geolocation)

            // Moko Resources - Multiplatform resource management
            api(libs.moko.resources)
            api(libs.moko.resources.compose)

            // Kotlinx IO - File operations
            implementation(libs.kotlinx.io.core)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.slf4j.android)

            // Ktor Android engine (OkHttp)
            implementation(libs.ktor.client.okhttp)

            // DataStore for preferences
            implementation(libs.datastore.preferences.core)

            // Security crypto for encrypted preferences
            implementation(libs.androidx.security.crypto)

            // Location services
            implementation(libs.play.services.location)
            implementation(libs.kotlinx.coroutines.play.services)

            implementation(libs.datastore.preferences)
            implementation(libs.androidx.exifinterface)

            // Paging 3 - Pagination support (Android only)
            api(libs.androidx.paging.common)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native)

            // Ktor iOS engine (Darwin)
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.kotest.assertions)
            implementation(libs.ktor.client.mock)
        }
        androidUnitTest.dependencies {
            implementation(libs.mockk.android)
            implementation(libs.kotlin.testJunit)
            implementation(libs.sqldelight.sqlite)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.espresso.core)
        }
    }
}

android {
    namespace = "com.po4yka.trailglass.shared"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }
}

sqldelight {
    databases {
        create("TrailGlassDatabase") {
            packageName.set("com.po4yka.trailglass.db")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}

// Moko Resources configuration
multiplatformResources {
    resourcesPackage.set("com.po4yka.trailglass.resources")
    resourcesClassName.set("SharedRes")
}

// KSP configuration for kotlin-inject
dependencies {
    add("kspCommonMainMetadata", libs.kotlin.inject.compiler)
    add("kspAndroid", libs.kotlin.inject.compiler)
    add("kspIosArm64", libs.kotlin.inject.compiler)
    add("kspIosSimulatorArm64", libs.kotlin.inject.compiler)

    // Detekt plugins
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.compose.rules.detekt)
}

// Code coverage configuration
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.db.*", // Generated SQLDelight code
                    "*.ComposableSingletons*",
                    "*_Factory", // Generated kotlin-inject code
                    "*Component*" // kotlin-inject components
                )
            }
        }
        verify {
            rule {
                minBound(75) // Target: 75%+ coverage
            }
        }
    }
}

// ktlint configuration
ktlint {
    version.set("1.5.0")
    android.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    ignoreFailures.set(true) // Ignore violations in generated code

    filter {
        exclude { entry ->
            entry.file.toString().contains("build/generated") ||
                entry.file.toString().contains("build\\generated") ||
                entry.file.toString().contains("/generated/") ||
                entry.file.toString().contains("\\generated\\") ||
                entry.file.toString().contains("moko-resources") ||
                entry.file.toString().contains("/db/") ||
                entry.file.name.endsWith("_Factory.kt") ||
                entry.file.name == "InjectAppComponent.kt" ||
                entry.file.name == "SharedRes.kt"
        }
    }
}

// detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    baseline = file("$rootDir/config/detekt/baseline.xml")

    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin"
    )
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(true)
    }
}
