# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
"""Resolve the qai_hub_apps_utils root directory."""

from __future__ import annotations

import sys
from pathlib import Path

from qai_hub_apps_test.utils.paths import REPOSITORY_ROOT

_SDK_PACKAGE = "qai_hub_apps_utils"
_DEFAULT_SDK_PARENT = REPOSITORY_ROOT / "apps" / "_shared" / "python"


def resolve_sdk_root(sdk_root_arg: str | None) -> Path:
    if sdk_root_arg:
        p = Path(sdk_root_arg).resolve()
        # Accept either the package dir itself or its parent
        if (p / "__init__.py").exists() and p.name == _SDK_PACKAGE:
            return p.parent
        if (p / _SDK_PACKAGE / "__init__.py").exists():
            return p
        sys.exit(
            f"error: --sdk_root '{p}' does not contain a '{_SDK_PACKAGE}' package."
        )
    if not (_DEFAULT_SDK_PARENT / _SDK_PACKAGE / "__init__.py").exists():
        sys.exit(
            f"error: SDK not found at default location '{_DEFAULT_SDK_PARENT}'. "
            "Pass --sdk_root pointing to the directory that contains "
            "the qai_hub_apps_utils/ package."
        )
    return _DEFAULT_SDK_PARENT
