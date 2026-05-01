#!/usr/bin/env bash
# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
# Create a Python virtual environment and install qai_hub_apps_test.
#
# Usage:
#   bash tools/setup_env.sh [--venv <path>] [--python <exe>] [--extras <extra>]
#
# Defaults:
#   --venv    qaiha-dev
#   --python  python3
#   --extras  dev
#
# Available extras:
#   dev        Full test install: pytest, qai_hub_models, boto3, etc. (default)
#   precommit  Light install: pre-commit + mypy only (for CI lint checks)

set -euo pipefail

VENV_PATH="qaiha-dev"
PYTHON="python3"
EXTRAS="dev"

while [ $# -gt 0 ]; do
    case $1 in
        --venv=*)    VENV_PATH="${1##--venv=}" ;;
        --venv)      VENV_PATH="$2"; shift ;;
        --python=*)  PYTHON="${1##--python=}" ;;
        --python)    PYTHON="$2"; shift ;;
        --extras=*)  EXTRAS="${1##--extras=}" ;;
        --extras)    EXTRAS="$2"; shift ;;
        *) echo "Unknown option: $1" >&2; exit 1 ;;
    esac
    shift
done

REPO_ROOT="$(git rev-parse --show-toplevel)"

if [ ! -d "$VENV_PATH" ]; then
    echo "Creating virtual environment at $VENV_PATH using $PYTHON"
    "$PYTHON" -m venv "$VENV_PATH"
else
    echo "Virtual environment already exists at $VENV_PATH"
fi

INSTALL_TARGET="$REPO_ROOT/tools/python/[$EXTRAS]"
TORCH_INDEX="https://download.pytorch.org/whl/cpu"
TORCH_VERSION="torch==2.8.0+cpu"

# Pre-install CPU torch before qai_hub_models so pip doesn't pull in the CUDA build.
if [ "$EXTRAS" = "dev" ]; then
    if command -v uv &>/dev/null; then
        uv pip install --python "$VENV_PATH/bin/python" --extra-index-url "$TORCH_INDEX" "$TORCH_VERSION"
    else
        "$VENV_PATH/bin/pip" install --extra-index-url "$TORCH_INDEX" "$TORCH_VERSION"
    fi
fi

if command -v uv &>/dev/null; then
    uv pip install --python "$VENV_PATH/bin/python" -e "$INSTALL_TARGET"
else
    "$VENV_PATH/bin/pip" install -e "$INSTALL_TARGET"
fi

echo ""
echo "Done. Activate with: source $VENV_PATH/bin/activate"
