# Android Emulator Setup Guides

This document provides setup instructions for different Linux distributions. Find the section that matches your operating system.

---

## Guide for Debian-Based Systems (Linux Mint, Ubuntu)

This guide provides the complete set of commands to install and configure the Android SDK and Emulator on a fresh Linux Mint or other Debian-based system.

### Step 1: Update System and Install Dependencies

First, update your package list and install the necessary dependencies, including the Java Development Kit (JDK), `unzip`, and `wget`.

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk wget unzip
```

### Step 2: Download & Set Up Android Tools

Download and configure the Android command-line tools.

```bash
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
mkdir -p ~/Android/sdk/cmdline-tools
unzip commandlinetools-linux-11076708_latest.zip -d ~/Android/sdk/cmdline-tools
mv ~/Android/sdk/cmdline-tools/cmdline-tools ~/Android/sdk/cmdline-tools/latest
rm commandlinetools-linux-11076708_latest.zip
```

### Step 3: Set Environment Variables

Add the Android SDK paths to your environment.

```bash
echo 'export ANDROID_HOME=$HOME/Android/sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator' >> ~/.bashrc
source ~/.bashrc
```

### Step 4: Install SDK Packages

Download the required SDK packages.

```bash
yes | sdkmanager "platform-tools" "platforms;android-34" "emulator"
yes | sdkmanager "system-images;android-34;default;x86_64"
```

### Step 5: Create an Android Virtual Device (AVD)

Create a new virtual device named `pixel_emulator`.

```bash
echo "no" | avdmanager create avd -n pixel_emulator -k "system-images;android-34;default;x86_64"
```

### Step 6: Launch the Emulator & Run the App

Launch the emulator and install the application.

```bash
emulator -avd pixel_emulator &
./gradlew installDebug
```

---

## Guide for Puppy Linux

This guide provides instructions for setting up the Android development environment on Puppy Linux, which requires manual installation of dependencies.

### Step 1: Install Java Development Kit (JDK)

Puppy Linux may not have Java pre-installed. Download and configure OpenJDK 17 manually.

```bash
# Download OpenJDK 17
wget https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz

# Extract and set up
sudo mkdir -p /usr/lib/jvm
sudo tar -xzf openjdk-17.0.2_linux-x64_bin.tar.gz -C /usr/lib/jvm

# Configure environment variables
echo 'export JAVA_HOME=/usr/lib/jvm/jdk-17.0.2' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# Clean up
rm openjdk-17.0.2_linux-x64_bin.tar.gz
```

### Step 2: Download & Set Up Android Tools

Download and configure the Android command-line tools.

```bash
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
mkdir -p ~/Android/sdk/cmdline-tools
unzip commandlinetools-linux-11076708_latest.zip -d ~/Android/sdk/cmdline-tools
mv ~/Android/sdk/cmdline-tools/cmdline-tools ~/Android/sdk/cmdline-tools/latest
rm commandlinetools-linux-11076708_latest.zip
```

### Step 3: Set Environment Variables

Add the Android SDK paths to your environment.

```bash
echo 'export ANDROID_HOME=$HOME/Android/sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator' >> ~/.bashrc
source ~/.bashrc
```

### Step 4: Install SDK Packages

Download the required SDK packages.

```bash
yes | sdkmanager "platform-tools" "platforms;android-34" "emulator"
yes | sdkmanager "system-images;android-34;default;x86_64"
```

### Step 5: Create an Android Virtual Device (AVD)

Create a new virtual device named `pixel_emulator`.

```bash
echo "no" | avdmanager create avd -n pixel_emulator -k "system-images;android-34;default;x86_64"
```

### Step 6: Build and Run the Application

First, build the application using Gradle.

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

Next, launch the emulator and install the app. This command ensures the emulator is fully booted before attempting to start the application.

```bash
# Set the AVD home directory and launch the emulator
export ANDROID_AVD_HOME=/root/.config/.android/avd
emulator -avd pixel_emulator &

# Wait for the device to boot completely
adb wait-for-device
while [[ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]]; do 
    echo 'Waiting for emulator to boot...'; 
    sleep 2; 
done

# Launch the main activity
adb shell am start -n com.example.mobileschedule/com.example.mobileschedule.MainActivity
```

---

## Guide for EasyOS

This guide provides instructions for setting up the Android development environment on EasyOS. It includes critical fixes for virtualization and emulator path issues.

### Step 1: Install Dependencies
Follow the same manual dependency installation as the "Guide for Puppy Linux" to install the JDK and Android Tools.

### Step 2: Fix KVM Permissions
The Android Emulator requires hardware acceleration (KVM), but on EasyOS, the current user may not have the required permissions.

- **Problem**: The emulator fails to start, sometimes silently, due to permission errors with `/dev/kvm`.
- **Solution**: Grant read/write permissions to the KVM device. Run this command before launching the emulator:
  ```bash
  sudo chmod 666 /dev/kvm
  ```

### Step 3: Build the Application
Build the application using the Gradle wrapper.

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

### Step 4: Launch the Emulator with Correct AVD Path
When launching the emulator, you must specify the correct path for the Android Virtual Device (AVD).

- **Problem**: The emulator cannot find the AVD because it was created in a non-standard directory (`/root/.config/.android/avd`).
- **Solution**: Set the `ANDROID_AVD_HOME` environment variable when launching the emulator.

```bash
# Set the AVD home directory and launch the emulator
export ANDROID_AVD_HOME=/root/.config/.android/avd
emulator -avd pixel_emulator &

# Wait for the device to boot completely
adb wait-for-device
while [[ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]]; do 
    echo 'Waiting for emulator to boot...'; 
    sleep 2; 
done

# Install the app and launch the main activity
./gradlew installDebug
adb shell am start -n com.example.mobileschedule/com.example.mobileschedule.MainActivity
```
