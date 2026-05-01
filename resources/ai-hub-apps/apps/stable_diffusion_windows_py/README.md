## Run Stable Diffusion on Snapdragon X Elite

Follow instructions to run the demo:

1. Enable PowerShell Scripts. Open PowerShell in administrator mode, and run:

```powershell
Set-ExecutionPolicy -Scope CurrentUser Unrestricted -Force
```

2. Open Anaconda PowerShell Prompt in this folder. If you don't have Anaconda PowerShell, use regular PowerShell.

3. Install platform dependencies:

```powershell
.\install_platform_deps.ps1
```

The above script will install:
  * Anaconda for x86-64. We use x86-64 Python for compatibility with other Python packages. However, inference in ONNX Runtime will, for the most part, run natively with ARM64 code.
  * Git for Windows. This is required to load the AI Hub Models package, which contains the application code used by this demo.

4. Open (or re-open) Anaconda Powershell Prompt to continue.

5. Create & activate your python environment:

```powershell
.\activate_venv.ps1 -name AI_Hub
```

6. Install python packages:

```powershell
.\install_python_deps.ps1 -model stable-diffusion-v2-1
```

In your currently active python environment, the above script will install:
  * AI Hub Models and model dependencies for stable diffusion.
  * The onnxruntime-qnn package, both to enable native ARM64 ONNX inference, as well as to enable targeting Qualcomm NPUs.

7. Download the `PRECOMPILED_QNN_ONNX` model files from [Qualcomm HuggingFace Repo](https://huggingface.co/qualcomm/Stable-Diffusion-v2.1) based on your target device, e.g., X-Elite users choose `Snapdragon® X Elite`.

8. Extract the zip to `<APP ROOT>/model` directory. The expected directory structure is:
```
model/
  |_ metadata.yaml
  |_ text_encoder.onnx
  |_ text_encoder_qairt_context.bin
  |_ unet.onnx
  |_ unet_qairt_context.bin
  |_ vae.onnx
  |_ vae_qairt_context.bin
```

9. Run demo:

```powershell
python demo.py --prompt "A girl taking a walk at sunset" --num-steps 20
```
