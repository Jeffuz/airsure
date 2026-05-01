# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------

from __future__ import annotations

import functools
import os
import shutil
import subprocess
from sys import platform
from typing import Any

if platform == "win32":
    SHELL_EXECUTABLE = shutil.which("powershell")
else:
    SHELL_EXECUTABLE = shutil.which("bash")


class Colors:
    GREEN = "\033[0;32m"
    RED = "\033[0;31m"
    YELLOW = "\033[0;33m"
    OFF = "\033[0m"


def echo(value: str, **kwargs: Any) -> None:
    """Print to stdout without buffering."""
    print(value, flush=True, **kwargs)


def run(command: str) -> subprocess.CompletedProcess:
    """Run a shell command, raising on non-zero exit."""
    return subprocess.run(command, shell=True, check=True, executable=SHELL_EXECUTABLE)


def on_ci() -> bool:
    """Return True if running in CI (QAIHM_CI env var is set to a truthy value)."""
    val = os.environ.get("QAIHM_CI", None)
    if val is None:
        return False
    return val.lower() in ("1", "true", "yes")


@functools.cache
def uv_installed() -> bool:
    """Return True if the uv package manager is available on PATH."""
    return shutil.which("uv") is not None


@functools.cache
def get_pip() -> str:
    """Return 'uv pip' if uv is available, otherwise 'pip'."""
    return "uv pip" if uv_installed() else "pip"


def get_venv_pip(venv_path: str) -> str:
    """Return the pip executable path inside the given venv."""
    if platform == "win32":
        return os.path.join(venv_path, "Scripts", "pip")
    return os.path.join(venv_path, "bin", "pip")


def get_venv_python(venv_path: str) -> str:
    """Return the python executable path inside the given venv."""
    if platform == "win32":
        return os.path.join(venv_path, "Scripts", "python")
    return os.path.join(venv_path, "bin", "python")


def get_venv_uv_pip(venv_path: str) -> str | None:
    """Return 'uv pip' command with the venv's python if uv is available, else None."""
    if uv_installed():
        python = get_venv_python(venv_path)
        return f"uv pip --python {python}"
    return None
