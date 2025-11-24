# iOS Build Configuration Guide

This document describes the critical build configuration for the Trailglass iOS app with Kotlin Multiplatform integration.

## Critical Build Settings

### Framework Search Paths
The project is configured to search for the Shared Kotlin framework in multiple locations:
- `$(SRCROOT)/../shared/build/xcode-frameworks` - Primary location (created by `embedAndSignAppleFrameworkForXcode`)
- `$(SRCROOT)/../shared/build/bin/iosSimulatorArm64/debugFramework` - Simulator debug builds
- `$(SRCROOT)/../shared/build/bin/iosArm64/debugFramework` - Device debug builds

### Framework Linking
The Shared framework is linked via `OTHER_LDFLAGS`:
```
-framework Shared
```

### Swift Version
- **Current**: Swift 5.10
- **Minimum**: Swift 5.0 (for compatibility)

### Deployment Target
- **Current**: iOS 16.0
- **Previous**: iOS 18.2 (too high, reduced for better compatibility)

## Build Script Configuration

### "Compile Kotlin Framework" Build Phase
Located before the Sources phase, this script:
1. Sets Java 17+ environment (required for Gradle)
2. Runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode`
3. Has proper input/output paths for build caching

### Input Paths (for dependency tracking)
- `$(SRCROOT)/../shared/build.gradle.kts`
- `$(SRCROOT)/../gradle.properties`

### Output Paths (for build caching)
- `$(SRCROOT)/../shared/build/xcode-frameworks/Shared.framework`
- `$(SRCROOT)/../shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework`
- `$(SRCROOT)/../shared/build/bin/iosArm64/debugFramework/Shared.framework`

## Build Process

1. **Pre-build**: Kotlin framework is compiled via build script
2. **Compile**: Swift sources are compiled with framework available
3. **Link**: Shared framework is linked via OTHER_LDFLAGS
4. **Run**: App runs with Kotlin Multiplatform code available

## Troubleshooting

### Framework Not Found
- Ensure build script runs successfully
- Check framework search paths are correct
- Verify `embedAndSignAppleFrameworkForXcode` task completes

### Java Version Issues
- Script automatically detects Java 17+ or falls back to Java 21
- Ensure JAVA_HOME is set correctly

### Build Caching
- Input/output paths enable proper incremental builds
- Clean build if framework changes aren't detected

## Configuration Files

- `iosApp/Configuration/Config.xcconfig` - Base configuration
- `iosApp/iosApp.xcodeproj/project.pbxproj` - Xcode project settings
- `iosApp/iosApp/Info.plist` - App metadata and permissions

