# qai-hub-apps

CLI for browsing and downloading [Qualcomm® AI Hub Apps](https://aihub.qualcomm.com/apps) —
sample apps for deploying ML models on Qualcomm® devices.

## Installation

```bash
pip install qai-hub-apps
```

## Quick Start

```bash
qai-hub-apps list                          # browse available apps
qai-hub-apps info whisper_windows_py       # inspect an app
qai-hub-apps fetch whisper_windows_py      # download source to current directory
```

## Commands

### list

List all available apps.

```bash
qai-hub-apps list
```

```
Qualcomm® AI Hub Apps  (N apps)

ID                                      Name
──────────────────────────────────────────────────────────────────────────────
whisper_windows_py                      Whisper Windows
stable_diffusion_py                     Stable Diffusion
...
```

### info

Show details for an app.

```bash
qai-hub-apps info <app_id>
```

```
Whisper Windows
══════════════════════════════════════════════════

ID:         whisper_windows_py
Type:       windows
Runtime:    onnx
Domain:     Audio
Use Case:   Speech Recognition
Precisions: float
Models:     whisper_base

Speech to text on Windows

Run Whisper on-device using ONNX.

Repo:  https://github.com/qualcomm/ai-hub-apps/tree/main/apps/whisper_windows_py
```

### fetch

Download and extract an app's source to a local directory.

```bash
# Download app source only
qai-hub-apps fetch <app_id>

# Download app source + model for a specific chipset
qai-hub-apps fetch <app_id> --model <model_id> --chipset <chipset>
```

| Flag | Description |
|------|-------------|
| `--output-dir PATH` | Output directory (default: current directory) |
| `--model MODEL_ID` | Also download a model supported by the app |
| `--chipset CHIPSET` | Target chipset for the model download |

**Example — fetch app with model:**

```bash
qai-hub-apps fetch stable_diffusion_windows_py --model stable_diffusion_v2_1 --chipset qualcomm-snapdragon-x-elite
```
