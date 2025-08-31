# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Refactored codebase to adhere to clean architecture principles by introducing `EventRepository` and `CalendarViewModel`.
- Split large `updateCalendar()` function in `MainActivity` into smaller, single-responsibility functions.
- Extracted date formatting and comparison logic into `DateUtils` utility class for consistency.
- Marked unused code and files (`Date.kt`, notification-related code) with comments to indicate they are disabled or deprecated, enhancing code clarity.

### Added
- Added `hour` field to `Event` class to allow events to be scheduled at specific times within a day.
- Updated UI to include a time picker for selecting event hours when adding new events.
- Modified event display to show the hour alongside the event description.

## [1.0.2] - 2025-08-30

### Added
- Created `run_app.sh` script to automate emulator setup, app installation, and launch process.

### Fixed
- Addressed persistent emulator UI freezes by forcing a stable software GPU renderer (`swiftshader_indirect`) in the `run_app.sh` script.

## [1.0.1] - 2025-08-30

### Fixed
- Resolved app crash when creating a new event due to notification scheduling issues on modern Android versions.
  - Corrected `PendingIntent` flags in `MainActivity.kt` and `NotificationReceiver.kt` to use `FLAG_IMMUTABLE`.
  - Added `SCHEDULE_EXACT_ALARM` permission to `AndroidManifest.xml`.
  - Properly registered `NotificationReceiver` in `AndroidManifest.xml`.
  - Temporarily disabled notification scheduling in `MainActivity.kt` to prevent crashes related to runtime permission issues.

## [1.0.0] - Initial Release

### Added
- Initial release of MobileSchedule with basic calendar and event management functionality.

## [0.1.0] - 2023-10-12

### Added
- Added permissions necessary for scheduling notifications.

### Fixed
- Fixed notification scheduling.
- Fixed emulator launch by forcing software GPU rendering.
