# Development Guide

Complete guide for setting up, running, and debugging TrailGlass locally.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Running the App](#running-the-app)
- [Hot Reload](#hot-reload)
- [Debugging](#debugging)
- [Common Issues](#common-issues)
- [Development Workflow](#development-workflow)
- [Tools and IDE](#tools-and-ide)

## Prerequisites

### Required

- **JDK 11+** (JDK 17 recommended for best Kotlin performance)
- **Android Studio** (Hedgehog 2023.1.1 or later)
- **Xcode 15+** (for iOS development, macOS only)
- **Git** (for version control)

### Optional but Recommended

- **Kotlin Plugin** (latest version in Android Studio)
- **Android Emulator** (API 24+ for testing)
- **iOS Simulator** (included with Xcode)
- **Homebrew** (macOS package manager)

## Environment Setup

### 1. Clone the Repository

```bash
git clone https://github.com/po4yka/trailglass.git
cd trailglass
```

### 2. Install Dependencies

#### macOS

```bash
# Install Homebrew (if not already installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java (if needed)
brew install openjdk@17

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### Linux

```bash
# Install OpenJDK
sudo apt update
sudo apt install openjdk-17-jdk

# Verify installation
java -version
```

#### Windows

1. Download and install [OpenJDK 17](https://adoptium.net/)
2. Add Java to PATH:
   - System Properties → Environment Variables
   - Add `JAVA_HOME` pointing to JDK installation
   - Add `%JAVA_HOME%\bin` to PATH

### 3. Android Studio Setup

1. **Download Android Studio**: https://developer.android.com/studio
2. **Install**:
   - Follow the installation wizard
   - Install Android SDK (API 24-36)
   - Install Android SDK Build-Tools
   - Install Android Emulator

3. **Configure SDK**:
   ```
   Tools → SDK Manager
   - Android SDK Platform 36 (target)
   - Android SDK Platform 24 (minimum)
   - Android SDK Build-Tools 34.0.0+
   - Android Emulator
   - Android SDK Platform-Tools
   ```

4. **Install Kotlin Plugin**:
   ```
   Settings → Plugins → Installed
   - Verify Kotlin plugin is enabled
   ```

### 4. Xcode Setup (macOS only)

```bash
# Install Xcode from App Store
# Or download from https://developer.apple.com/xcode/

# Install Command Line Tools
xcode-select --install

# Accept license
sudo xcodebuild -license accept

# Verify installation
xcodebuild -version
```

### 5. Configure Environment Variables

Create `local.properties` in project root:

```properties
# Android SDK location
sdk.dir=/Users/yourname/Library/Android/sdk

# Optional: Increase Gradle memory
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

## Running the App

### Android

#### From Android Studio

1. **Open Project**:
   ```
   File → Open → Select trailglass directory
   ```

2. **Sync Gradle**:
   ```
   File → Sync Project with Gradle Files
   ```

3. **Create/Start Emulator**:
   ```
   Tools → Device Manager → Create Device
   - Select Pixel 5 or similar
   - System Image: API 33 (Android 13) or higher
   - Click Play to start
   ```

4. **Run App**:
   - Select `composeApp` configuration
   - Click Run (▶) or press `Shift+F10`

#### From Command Line

```bash
# List available devices
./gradlew :composeApp:installDebug

# Install on connected device
adb devices
./gradlew :composeApp:installDebug

# Run on specific device
adb -s DEVICE_ID install composeApp/build/outputs/apk/debug/composeApp-debug.apk

# View logs
adb logcat | grep TrailGlass
```

### iOS

#### From Xcode

1. **Open Project**:
   ```bash
   cd iosApp
   open iosApp.xcodeproj
   ```

2. **Select Target**:
   - Select iosApp scheme
   - Select simulator (iPhone 15 recommended)

3. **Build Shared Framework**:
   ```
   Product → Build (⌘B)
   ```

4. **Run**:
   ```
   Product → Run (⌘R)
   ```

#### From Command Line

```bash
# Build shared framework
./gradlew :shared:embedAndSignAppleFrameworkForXcode

# Build and run iOS app
cd iosApp
xcodebuild -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 15' \
  build run
```

## Hot Reload

### Android (Jetpack Compose)

Compose has built-in hot reload (Live Edit):

1. **Enable Live Edit**:
   ```
   Android Studio → Settings → Editor → Live Edit
   ☑ Enable Live Edit
   ```

2. **Usage**:
   - Run app in debug mode
   - Make changes to Composable functions
   - Changes apply automatically (no rebuild)

3. **Limitations**:
   - Only works for `@Composable` functions
   - Doesn't work for data classes or logic changes
   - May require full rebuild for state changes

### iOS (SwiftUI)

SwiftUI has built-in preview and hot reload:

1. **Use Previews**:
   ```swift
   struct ContentView_Previews: PreviewProvider {
       static var previews: some View {
           ContentView()
       }
   }
   ```

2. **Live Preview**:
   - Open SwiftUI file
   - Click Resume button in preview canvas
   - Changes update automatically

3. **Inject Changes**:
   - Run app on simulator
   - Make changes to SwiftUI views
   - Press `⌘R` to inject changes (faster than full rebuild)

### Shared Code (Kotlin)

For shared Kotlin code changes:

```bash
# Rebuild shared module
./gradlew :shared:build

# Android: Stop and restart app
./gradlew :composeApp:installDebug

# iOS: Rebuild framework
./gradlew :shared:embedAndSignAppleFrameworkForXcode
# Then rebuild in Xcode
```

## Debugging

### Android Debugging

#### Using Android Studio Debugger

1. **Set Breakpoints**:
   - Click in gutter next to line number
   - Red dot appears

2. **Start Debug Session**:
   - Click Debug (🐞) or press `Shift+F9`

3. **Debug Controls**:
   - Step Over: `F8`
   - Step Into: `F7`
   - Step Out: `Shift+F8`
   - Resume: `F9`

4. **Evaluate Expressions**:
   - Select code → Right-click → Evaluate Expression
   - Or press `Alt+F8`

#### Logcat

```kotlin
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class MyClass {
    fun myMethod() {
        logger.debug { "Debug message" }
        logger.info { "Info message" }
        logger.warn { "Warning message" }
        logger.error { "Error message" }
    }
}
```

View logs in Android Studio:
```
View → Tool Windows → Logcat
Filter: package:com.po4yka.trailglass
```

Or via command line:
```bash
adb logcat | grep TrailGlass
```

#### Database Inspection

```
View → Tool Windows → App Inspection → Database Inspector
```

Or use:
```bash
# Pull database from device
adb exec-out run-as com.po4yka.trailglass \
  cat databases/trailglass.db > local-db.db

# Open with SQLite browser
sqlite3 local-db.db
```

### iOS Debugging

#### Using Xcode Debugger

1. **Set Breakpoints**:
   - Click in gutter next to line number
   - Blue arrow appears

2. **Start Debug Session**:
   - Product → Run (⌘R)
   - App runs in debug mode by default

3. **Debug Controls**:
   - Step Over: `F6`
   - Step Into: `F7`
   - Step Out: `F8`
   - Continue: `⌘^Y`

4. **LLDB Console**:
   ```
   Debug → Debug Workflow → Show Debug Console
   ```

   Common commands:
   ```lldb
   # Print variable
   po variableName

   # Print expression
   p expression

   # Continue execution
   continue
   ```

#### Logging

```swift
import os.log

let logger = Logger(subsystem: "com.po4yka.trailglass", category: "general")

logger.debug("Debug message")
logger.info("Info message")
logger.warning("Warning message")
logger.error("Error message")
```

View logs:
```
Console.app → Filter: "trailglass"
```

Or Xcode console:
```
View → Debug Area → Show Debug Area
```

#### Database Inspection

```bash
# Find app container
xcrun simctl get_app_container booted com.po4yka.trailglass data

# Navigate to database
cd $(xcrun simctl get_app_container booted com.po4yka.trailglass data)
cd Library/Application Support/databases/

# Open database
sqlite3 trailglass.db
```

### Debugging Shared Code

Since shared code runs on both platforms:

1. **Unit Tests**: Best way to debug shared logic
   ```bash
   ./gradlew :shared:test --tests "MyTest"
   ```

2. **Add Logging**:
   ```kotlin
   private val logger = logger()  // kotlin-logging

   fun myFunction() {
       logger.debug { "Shared code debug message" }
   }
   ```

3. **Platform-Specific Debugging**: Debug through platform app

## Common Issues

### Gradle Sync Failures

**Problem**: "Gradle sync failed: Could not resolve dependencies"

**Solutions**:
```bash
# Clean build
./gradlew clean

# Refresh dependencies
./gradlew --refresh-dependencies

# Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew build
```

### Android Emulator Issues

**Problem**: Emulator won't start or is slow

**Solutions**:
1. **Enable Hardware Acceleration**:
   - Windows: Enable Hyper-V or HAXM
   - macOS: Already enabled
   - Linux: Enable KVM

2. **Allocate More RAM**:
   ```
   AVD Manager → Edit Device → Advanced Settings
   RAM: 4096 MB (or more)
   ```

3. **Use ARM Image on Apple Silicon**:
   - Select ARM64 system image for better performance

### iOS Build Failures

**Problem**: "Framework not found Shared"

**Solution**:
```bash
# Rebuild shared framework
./gradlew :shared:embedAndSignAppleFrameworkForXcode

# Clean Xcode build
Product → Clean Build Folder (⇧⌘K)

# Rebuild
Product → Build (⌘B)
```

**Problem**: "Code signing error"

**Solution**:
1. Xcode → Signing & Capabilities
2. Select your team
3. Enable "Automatically manage signing"

### SQLDelight Errors

**Problem**: "Cannot resolve symbol generated by SQLDelight"

**Solutions**:
```bash
# Regenerate SQLDelight code
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface

# Clean and rebuild
./gradlew clean build

# In Android Studio: File → Invalidate Caches → Invalidate and Restart
```

### Location Permission Issues

**Problem**: App crashes when requesting location

**Solutions**:

**Android**:
1. Add permissions to `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
   ```

2. Request runtime permissions before accessing location

**iOS**:
1. Add to `Info.plist`:
   ```xml
   <key>NSLocationWhenInUseUsageDescription</key>
   <string>TrailGlass needs location to track your travels</string>
   <key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
   <string>TrailGlass tracks location in background for travel logging</string>
   ```

### Memory Issues

**Problem**: "OutOfMemoryError" during build

**Solution**: Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC
org.gradle.parallel=true
org.gradle.caching=true
```

### Kotlin Version Conflicts

**Problem**: "Incompatible Kotlin version"

**Solution**:
```bash
# Check Kotlin version
./gradlew -version

# Update in libs.versions.toml
kotlin = "2.2.20"  # Use consistent version

# Sync Gradle
./gradlew --refresh-dependencies
```

## Development Workflow

### Daily Development

1. **Pull Latest Changes**:
   ```bash
   git pull origin main
   ```

2. **Create Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Changes**:
   - Edit code
   - Test locally
   - Run tests

4. **Test Changes**:
   ```bash
   # Run unit tests
   ./gradlew :shared:test

   # Run Android tests
   ./gradlew :composeApp:testDebugUnitTest

   # Run coverage
   ./gradlew koverHtmlReport
   ```

5. **Commit Changes**:
   ```bash
   git add .
   git commit -m "feat: add your feature"
   ```

6. **Push and Create PR**:
   ```bash
   git push origin feature/your-feature-name
   # Create PR on GitHub
   ```

### Code Generation

#### SQLDelight

After modifying `.sq` files:
```bash
./gradlew :shared:generateCommonMainTrailGlassDatabaseInterface
```

#### Compose Icons (Android)

Icons auto-generate on build. To force regeneration:
```bash
./gradlew :composeApp:generateComposeResClass
```

### Database Migrations

1. **Create Migration File**:
   ```
   shared/src/commonMain/sqldelight/migrations/
   └── 2.sqm  # Version 2 migration
   ```

2. **Write Migration SQL**:
   ```sql
   -- 2.sqm
   ALTER TABLE place_visits ADD COLUMN notes TEXT;
   ```

3. **Update Schema Version**:
   ```kotlin
   // In database setup
   TrailGlassDatabase.Schema.migrate(
       driver = driver,
       oldVersion = 1,
       newVersion = 2
   )
   ```

## Tools and IDE

### Recommended Android Studio Plugins

1. **Kotlin** (bundled)
2. **Rainbow Brackets**: Colorize bracket pairs
3. **GitToolBox**: Enhanced Git integration
4. **JSON Viewer**: Format JSON logs
5. **ADB Idea**: Quick ADB commands

Install via: `Settings → Plugins → Marketplace`

### Recommended Xcode Extensions

1. **SwiftLint**: Code linting
2. **SwiftFormat**: Code formatting

### Command Line Tools

```bash
# Gradle tasks
./gradlew tasks

# Specific module tasks
./gradlew :shared:tasks

# Build all
./gradlew build

# Clean all
./gradlew clean

# Run tests with coverage
./scripts/run-tests.sh

# Format code
./gradlew spotlessApply  # If configured
```

### Git Hooks

Set up pre-commit hook for tests:
```bash
# .git/hooks/pre-commit
#!/bin/bash
./gradlew :shared:test
```

Make executable:
```bash
chmod +x .git/hooks/pre-commit
```

## Performance Tips

### Build Performance

1. **Enable Gradle Daemon**: Already enabled by default
2. **Use Build Cache**: Add to `gradle.properties`:
   ```properties
   org.gradle.caching=true
   org.gradle.parallel=true
   org.gradle.configureondemand=true
   ```

3. **Increase Memory**:
   ```properties
   org.gradle.jvmargs=-Xmx4g
   ```

4. **Use Kotlin Compilation Cache**: Enabled by default in Kotlin 2.0+

### Android Emulator Performance

1. Use ARM64 image on Apple Silicon Macs
2. Enable hardware acceleration (HAXM/KVM)
3. Allocate sufficient RAM (4GB+)
4. Use "Cold Boot" instead of snapshots for debugging

### iOS Simulator Performance

1. Use latest Xcode
2. Close unused simulators
3. Reset simulator periodically:
   ```
   Device → Erase All Content and Settings
   ```

## Related Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture overview
- [TESTING.md](TESTING.md) - Testing guide
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [LOCATION_TRACKING.md](LOCATION_TRACKING.md) - Location tracking setup
