# Changelog

All notable changes to the MobileSchedule app will be documented in this file.

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
