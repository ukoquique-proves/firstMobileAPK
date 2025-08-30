# MobileSchedule: Android Calendar Application

## Overview

MobileSchedule is a native Android application developed in Kotlin that provides a simple and effective way to manage a personal calendar. Users can reserve dates, add event descriptions, and receive timely notifications for their upcoming events. The application is designed to be straightforward, focusing on core calendar and scheduling functionalities.

The primary view displays a three-month calendar: the current month and the next two. This allows users to easily plan and view their upcoming schedule at a glance. The project was built using modern Android development practices, including Kotlin, AndroidX libraries, and Gradle with the Kotlin DSL.

## Features

- **Three-Month Calendar View**: The main screen displays a grid-based calendar showing the current month, with the ability to navigate to the next two months. Navigation is restricted to this three-month window.
- **Event Management**: Users can tap on any day in the calendar to add a new event. A dialog prompts for a text description of the event.
- **Event Highlighting**: Days that have one or more events scheduled are visually highlighted with a distinct background color, making them easy to spot.
- **Persistent Storage**: Events are saved locally on the device using `SharedPreferences`. The event data is serialized to JSON format using the Gson library, ensuring that the schedule is preserved even after the app is closed.
- **Alarms and Notifications**: For each event, an alarm is scheduled to trigger one day in advance. This is handled by Android's `AlarmManager` for reliability.
- **Rich Notifications**: When an alarm triggers, a push notification is sent to the user. The notification includes sound, vibration, and causes the screen to flash, ensuring the user is alerted.
- **Alarm Dismissal**: Tapping on a notification opens a dedicated screen that displays the event's description and provides a button to dismiss the alarm and stop the sound/vibration.

## Project Structure

The codebase is organized into several key components:

- `MainActivity.kt`: The main entry point of the application. It manages the calendar view, user interactions, event creation, and alarm scheduling.
- `CalendarAdapter.kt`: A custom adapter for the `GridView` that displays the days of the month. It handles cell rendering and highlighting for days with events.
- `Event.kt` & `Date.kt`: Data classes that model the application's core data structures for events and dates.
- `NotificationReceiver.kt`: A `BroadcastReceiver` that listens for alarms triggered by `AlarmManager`. It is responsible for building and displaying the event notifications.
- `NotificationActivity.kt`: The activity that is launched when a user taps on a notification. It displays event details and allows the user to dismiss the alarm.
- **Layouts (`res/layout/`)**: XML files defining the UI for the main calendar (`activity_main.xml`), the notification screen (`activity_notification.xml`), and individual calendar day cells (`grid_item.xml`).

## GitHub Repository

The MobileSchedule project has been uploaded to GitHub and is available at:

[https://github.com/ukoquique-proves/firstMobileAPK](https://github.com/ukoquique-proves/firstMobileAPK)

You can clone the repository, contribute, or report issues through this link.

## Technical Details & Dependencies

- **Language**: Kotlin
- **Build Tool**: Gradle with Kotlin DSL (`.gradle.kts`)
- **Core Libraries**:
  - `androidx.appcompat`: For core app compatibility.
  - `androidx.core:core-ktx`: Kotlin extensions for AndroidX.
  - `com.google.android.material`: For Material Design components.
  - `com.google.code.gson:gson`: For serializing and deserializing event data for storage.
- **Android APIs**:
  - `AlarmManager`: For scheduling background alarms.
  - `NotificationManager`: For creating and managing user notifications.
  - `SharedPreferences`: For simple, persistent key-value storage.

## Setup and Build

To build and run this project, you will need to have the following installed:

1.  **Java Development Kit (JDK)**: OpenJDK 17 or newer is recommended.
2.  **Android SDK**: The project is configured for Android SDK Platform 34.

Follow these steps to build the application from the source code:

1.  **Clone the repository** (or ensure you have the project files).

2.  **Configure the Android SDK Path**:
    - Create a file named `local.properties` in the root directory of the project.
    - Add a single line to this file specifying the path to your Android SDK installation. For example:
      ```properties
      sdk.dir=/path/to/your/Android/sdk
      ```

3.  **Build the APK**:
    - Open a terminal in the project's root directory.
    - Make the Gradle wrapper script executable:
      ```sh
      chmod +x ./gradlew
      ```
    - Run the `assembleDebug` task to build the debug APK:
      ```sh
      ./gradlew assembleDebug
      ```

4.  **Locate the APK**:
    - Upon a successful build, the APK file can be found at:
      `app/build/outputs/apk/debug/app-debug.apk`

This APK can then be installed on an Android emulator or a physical device for testing.

## Troubleshooting

During the development and testing of this application, several issues were encountered and resolved. This section documents them for future reference.

### 1. APK Installation Failure

- **Problem**: The generated `.apk` file failed to install on a physical Android device, showing a "No Archiver found for the stream signature" error. This indicated that the APK file was corrupted or incomplete.
- **Solution**: The issue was resolved by running a clean build cycle. The `clean` task removes all previously compiled files and artifacts, and `assembleDebug` then generates a fresh, valid APK.
  ```sh
  ./gradlew clean
  ./gradlew assembleDebug
  ```

### 2. Application Crash When Adding Events

- **Problem**: The application would crash when adding a second event to the same day, or when adding a new event shortly after a previous one. 
- **Solution**: The root cause was a conflict in the `PendingIntent` request codes used for scheduling alarms with `AlarmManager`. When two events were created close together, they could generate the same `hashCode()`, leading to a system conflict. The fix was to create a more unique request code by combining the event's hash code with the current system time in milliseconds, ensuring that every alarm is registered with a unique ID.

### 3. Notification Scheduling Crash on Modern Android

- **Problem**: The app crashes when creating a new event on Android 13 or higher due to runtime permission issues with `SCHEDULE_EXACT_ALARM` for exact alarm scheduling.
- **Solution**: Notification scheduling has been temporarily disabled in the code to prevent crashes. To re-enable notifications, runtime permission handling for `SCHEDULE_EXACT_ALARM` must be implemented. This is a more complex change that will be addressed in a future update.

### 4. Environment Setup on Non-Debian Systems (Puppy Linux)

- **Problem**: The setup instructions in `EMULATOR.md` are based on the `apt` package manager and may fail on other Linux distributions. On Puppy Linux, this resulted in errors because `apt` is not available, and Java was not pre-installed.
- **Solution**: The environment was set up by manually installing the required dependencies. This involved downloading the OpenJDK 17 `.tar.gz` archive, extracting it, and setting the `JAVA_HOME` environment variable. Additionally, the emulator failed to start due to timing issues and incorrect AVD paths. This was resolved by using a script to wait for the emulator to fully boot before attempting to install the application. For a detailed guide, see the updated `EMULATOR.md` file.
