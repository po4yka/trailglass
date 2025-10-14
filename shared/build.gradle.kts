import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    id("org.jetbrains.kotlinx.kover") version "0.9.3"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
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
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.slf4j.android)

            // Ktor Android engine (OkHttp)
            implementation(libs.ktor.client.okhttp)

            // DataStore for preferences
            implementation(libs.datastore.preferences.core)
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
            implementation(libs.sqldelight.sqlite)
        }
        androidUnitTest.dependencies {
            implementation(libs.mockk.android)
            implementation(libs.kotlin.testJunit)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.espresso.core)
        }
    }
}

android {
    namespace = "com.po4yka.trailglass.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
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

// KSP configuration for kotlin-inject
dependencies {
    add("kspCommonMainMetadata", libs.kotlin.inject.compiler)
    add("kspAndroid", libs.kotlin.inject.compiler)
    add("kspIosArm64", libs.kotlin.inject.compiler)
    add("kspIosSimulatorArm64", libs.kotlin.inject.compiler)
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
