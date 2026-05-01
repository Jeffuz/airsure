# AirSure: AI-Powered Travel Assistant

AirSure is an advanced Android travel companion that leverages on-device AI to help travelers manage their airport experience. By combining real-time computer vision and local speech processing, AirSure provides automated carry-on size checking and proactive flight alert monitoring.

<img width="888" height="497" alt="Image" src="https://github.com/user-attachments/assets/c46db3d1-3139-4195-b9e6-ae4bb8e732c3" />

## Key Features

### 1. Smart Carry-On Scanner (Vision AI)
Uses EfficientDet-Lite via LiteRT to detect and analyze luggage in real-time.
*   Object Detection: Real-time identification of carry-on bags and personal items.
*   Restriction Management: Automatically compares detected items against airline-specific size regulations.
*   Hardware Acceleration: Optimized for Qualcomm NPUs to ensure high-speed, low-power inference.

### 2. Live Flight Alerts (Audio AI)
Employs a local Whisper model to transcribe and process airport announcements.
*   On-Device Transcription: Privacy-focused audio processing that never sends your voice to the cloud.
*   Pattern Matching: Intelligently listens for your specific flight number to detect:
    *   Gate Changes
    *   Flight Delays
    *   Final Boarding Calls
*   Live Transcription UI: Real-time visual feedback of what the AI is hearing at the gate.

### 3. Integrated Travel Experience
*   Boarding Pass Integration: Store flight details locally for targeted monitoring.
*   Modern UI: Built with Jetpack Compose for a smooth, fluid, and responsive user experience.

## Technical Stack

*   Language: Kotlin & Java
*   UI Framework: Jetpack Compose (Material 3)
*   AI Engine: Google LiteRT (formerly TFLite)
*   Models:
    *   Vision: efficientdet_lite0 (Quantized for NPU)
    *   Audio: Distil-Whisper (Encoder/Decoder architecture)
*   Hardware Optimization: Qualcomm AI Stack (QNN) for NPU acceleration.
*   Camera: CameraX API for robust image capture.

## Project Structure

*   app/src/main/java/com/example/efficientdet_lite/vision/: Computer vision logic, analyzers, and camera implementation.
*   app/src/main/java/com/example/efficientdet_lite/audio/: Whisper model integration, audio recording, and transcription services.
*   app/src/main/java/com/example/efficientdet_lite/announcements/: Logic for processing transcripts and matching flight alerts.
*   app/src/main/assets/: Contains pre-trained TFLite models (detector.tflite, whisper_encoder/decoder).

## Getting Started

### Prerequisites
*   Android Studio Ladybug or newer.
*   A physical Android device (Qualcomm Snapdragon 8 Gen 1 or newer recommended for NPU features).

### Build & Run
1.  Clone the repository.
2.  Open in Android Studio: Let Gradle sync and download dependencies.
3.  Deploy: Select your device and click Run.
4.  Backend Selection: The app automatically attempts to use the NPU, falling back to GPU or CPU if unavailable. You can monitor performance via logcat tags: EfficientDetPerf and AudioDebug.

### Exporting to APK

To generate an APK for installation:

#### Via Android Studio
1.  Go to **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
2.  Android Studio will generate the APK and show a notification with a **Locate** link to open the folder.

#### Via Command Line
To build using the command line:

1.  **Open Terminal:** Use the **Terminal** tab at the bottom of Android Studio, or open a Command Prompt/PowerShell (Windows) or Terminal (macOS/Linux).
2.  **Navigate to Project Root:** Ensure you are in the `airsure` directory.
3.  **Run the Build Task:** Use the following command based on your OS:

**Windows:**
```bat
./gradlew.bat :app:assembleDebug
```

> **Note:** If you see a `JAVA_HOME is not set` error, run the command for your terminal type:
> 
> **For PowerShell (default):**
> `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; $env:Path += ";$env:JAVA_HOME\bin"`
> 
> **For Bash (e.g., Git Bash):**
> `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"; export PATH="$JAVA_HOME/bin:$PATH"`

**Linux/macOS:**
```bash
./gradlew :app:assembleDebug
```

The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

## Testing Debug Tools
The Flight Alerts screen includes a debug menu (accessible via the "Listening" indicator) to simulate various scenarios:
*   Test Boarding/Delay/Gate Change announcements via local assets.
*   Clear active alerts to reset the state.

---
Built for the future of private, on-device AI assistance.
