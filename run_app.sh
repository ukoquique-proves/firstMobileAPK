#!/bin/bash

# Script to automate running the MobileSchedule app on the emulator

echo "--- MobileSchedule App Runner ---"

# 1. Set KVM permissions for hardware acceleration
echo "[1/5] Setting /dev/kvm permissions..."
sudo chmod 666 /dev/kvm

# 2. Define paths and variables
SDK_PATH="/files/projects/FOSSAPUP/Android/sdk"
EMULATOR_PATH="$SDK_PATH/emulator/emulator"
ADB_PATH="$SDK_PATH/platform-tools/adb"
AVD_HOME="/root/.config/.android/avd"
AVD_NAME="pixel_emulator"
PACKAGE_NAME="com.example.mobileschedule"
MAIN_ACTIVITY=".MainActivity"

# 3. Start the emulator if not already running
if ! $ADB_PATH devices | grep -q 'emulator-'; then
    echo "[2/5] Starting emulator '$AVD_NAME'..."
    ANDROID_AVD_HOME=$AVD_HOME $EMULATOR_PATH -avd $AVD_NAME -gpu swiftshader_indirect -no-snapshot-load -no-snapshot-save &>/dev/null &
else
    echo "[2/5] Emulator already running."
fi

# 4. Wait for the emulator to boot
echo "[3/5] Waiting for emulator to boot (this may take a minute)..."
$ADB_PATH wait-for-device

# Wait until the boot animation is complete
while [[ "`$ADB_PATH shell getprop sys.boot_completed | tr -d '\r'`" != "1" ]] ; do
    echo "    ...waiting for boot to complete..."
    sleep 5
done
echo "Emulator is fully booted."

# 5. Build and install the app
echo "[4/5] Building and installing the app..."
./gradlew installDebug
if [ $? -ne 0 ]; then
    echo "Gradle build or installation failed. Aborting."
    exit 1
fi

# 6. Launch the main activity
echo "[5/5] Launching the app..."
$ADB_PATH shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"

echo "--- Script finished ---"
