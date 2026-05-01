# AirSure: AI-Powered Travel Assistant

AirSure is an advanced Android travel companion that leverages on-device AI to help travelers manage their airport experience. By combining real-time computer vision and local speech processing, AirSure provides automated carry-on size checking and proactive flight alert monitoring.

## Team Members
*   **Mathew Raju** - mathew2002raj@gmail.com
*   **Jeff Zhang** - jeffzhang0049@gmail.com
*   **Mona Zhao** - monazhao59@gmail.com

## Project Description
AirSure is designed to reduce the stress of air travel by providing intelligent, private, and offline-first assistance. 
*   **Vision AI**: Uses **YOLOv11** (sourced from Qualcomm AI Hub) to scan carry-on luggage against airline restrictions.
*   **Audio AI**: Employs a local **Distil-Whisper** model (sourced from Qualcomm AI Hub) to transcribe airport announcements and filter for specific flight alerts (gate changes, delays).
*   **Edge Processing**: The majority of the application logic and all AI inference runs locally on the device for maximum privacy and low latency.

## Key Features

### 1. Smart Carry-On Scanner (Vision AI)
Uses **YOLOv11** via LiteRT to detect and analyze luggage in real-time.
*   Object Detection: Real-time identification of carry-on bags.
*   Restriction Management: Automatically compares detected items against airline-specific size regulations.
*   Hardware Acceleration: Optimized for Qualcomm NPUs via the Qualcomm AI Stack.

### 2. Live Flight Alerts (Audio AI)
Employs a local **Distil-Whisper** model to transcribe airport announcements.
*   On-Device Transcription: Privacy-focused audio processing using hardware-optimized models from Qualcomm AI Hub.
*   Pattern Matching: Intelligently listens for specific flight numbers for gate changes, delays, and final calls.
*   Live Transcription UI: Real-time visual feedback of the transcription process.

## Setup Instructions (From Scratch)

### Prerequisites
*   **Android Studio Ladybug** (2024.2.1) or newer.
*   **Android SDK 31+**.
*   **Physical Device**: Qualcomm Snapdragon 8 Gen 1 or newer recommended for NPU acceleration.
*   **Git LFS**: Ensure Git Large File Storage is installed to handle model files.

### Dependencies
The project uses the following major dependencies:
*   **LiteRT (TFLite)**: For on-device model inference.
*   **Jetpack Compose**: For the modern UI layer.
*   **CameraX**: For high-performance camera access.
*   **Qualcomm AI Stack (QNN)**: For hardware-specific optimizations.

### Build Steps
1.  **Clone the Repository**: `git clone <repository-url>`
2.  **Pull Large Files**: Run `git lfs pull` to ensure models are downloaded.
3.  **Open in Android Studio**: Let Gradle sync and download all dependencies.
4.  **Configure JAVA_HOME**: If building via CLI, ensure your path points to the bundled JDK in Android Studio (`jbr`).
5.  **Run**: Select your device and click **Run** (Shift + F10).

## Run and Usage Instructions

### Using the Carry-On Scanner
1.  Navigate to the **Scanner** tab.
2.  Point the camera at your luggage.
3.  The AI will detect the item and display its classification.
4.  View "Carry-on Status" to see if it matches standard airline dimensions.

### Using Flight Alerts
1.  Enter your **Flight Code** in the Boarding Pass section.
2.  Navigate to the **Alerts** tab.
3.  The device will begin listening locally.
4.  When an announcement is detected, it will be transcribed live.
5.  If a match is found for your flight, an alert card (Gate Change/Delay) will appear.

## Technical Stack
*   **Language**: Kotlin & Java
*   **UI Framework**: Jetpack Compose (Material 3)
*   **AI Engine**: Google LiteRT
*   **Models**: **YOLOv11** (Vision), **Distil-Whisper** (Audio) - Both optimized via Qualcomm AI Hub.

## Tests and Verification
*   **Unit Tests**: Located in `app/src/test`. Run via `./gradlew test`.
*   **Logic Verification**: Use the **Debug Menu** in the Flight Alerts screen (click the "Listening" indicator) to simulate test announcements via pre-loaded assets.

## Notes
*   **Privacy**: All audio recording and transcription stays on-device. No data is sent to external servers.
*   **Performance**: NPU acceleration is automatically selected if supported by the hardware, falling back to GPU or CPU otherwise.

## References
*   [Qualcomm AI Hub](https://aihub.qualcomm.com/)
*   [Google LiteRT Documentation](https://ai.google.dev/edge/litert)
*   [Qualcomm AI Stack](https://www.qualcomm.com/products/technology/processors/snapdragon-8-gen-1)

## License
This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
