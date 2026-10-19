This is a Kotlin Multiplatform project targeting Android, iOS.

* [/shared](./shared/src) is the Kotlin Multiplatform module shared by all platforms: models, rule engine,
  repositories and the Compose Multiplatform UI.
    - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
    - [androidMain](./shared/src/androidMain/kotlin) and [iosMain](./shared/src/iosMain/kotlin) hold
      platform-specific implementations (e.g. phone number normalization).

* [/androidApp](./androidApp/src/main) is the Android application: `MainActivity`, the
  `CallScreeningService`, notifications and system state checks.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :androidApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :androidApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…