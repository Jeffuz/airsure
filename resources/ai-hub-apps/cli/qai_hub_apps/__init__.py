# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from packaging.version import parse as parse_version

from qai_hub_apps._version import __version__


def _is_dev() -> bool:
    """Return True if the current install is a development (pre-release) build."""
    return parse_version(__version__).is_devrelease


__all__ = ["__version__", "_is_dev"]
