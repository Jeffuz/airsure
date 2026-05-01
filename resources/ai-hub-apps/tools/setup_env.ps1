# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
# Create a Python virtual environment and install qai_hub_apps_test.
#
# Usage:
#   . tools/setup_env.ps1 [-Venv <path>] [-Python <exe>] [-Extras <extra>]
#
# Defaults:
#   -Venv    qaiha-dev
#   -Python  python
#   -Extras  dev
#
# Available extras:
#   dev        Full test install: pytest, qai_hub_models, boto3, etc. (default)
#   precommit  Light install: pre-commit + mypy only (for CI lint checks)

param(
    [string]$Venv = "qaiha-dev",
    [string]$Python = "python",
    [string]$Extras = "dev"
)

$ErrorActionPreference = "Stop"

$RepoRoot = git rev-parse --show-toplevel

if (-not (Test-Path $Venv)) {
    Write-Host "Creating virtual environment at $Venv using $Python"
    & $Python -m venv $Venv
} else {
    Write-Host "Virtual environment already exists at $Venv"
}

$InstallTarget = "$RepoRoot\tools\python\[$Extras]"
$TorchIndex = "https://download.pytorch.org/whl/cpu"
$TorchVersion = "torch==2.8.0+cpu"

# Pre-install CPU torch before qai_hub_models so pip doesn't pull in the CUDA build.
if ($Extras -eq "dev") {
    $uvAvailable = Get-Command uv -ErrorAction SilentlyContinue
    if ($uvAvailable) {
        uv pip install --python "$Venv\Scripts\python.exe" --extra-index-url $TorchIndex $TorchVersion
    } else {
        & "$Venv\Scripts\pip.exe" install --extra-index-url $TorchIndex $TorchVersion
    }
}

$uvAvailable = Get-Command uv -ErrorAction SilentlyContinue
if ($uvAvailable) {
    uv pip install --python "$Venv\Scripts\python.exe" -e $InstallTarget
} else {
    & "$Venv\Scripts\pip.exe" install -e $InstallTarget
}

Write-Host ""
Write-Host "Done. Activate with: . $Venv\Scripts\Activate.ps1"
