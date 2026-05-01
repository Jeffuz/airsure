# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------

import os

from .process import Colors, echo


def on_github() -> bool:
    """Return True if running inside a GitHub Actions workflow."""
    return "GITHUB_ACTION" in os.environ


def start_group(group_name: str) -> None:
    """Begin a collapsible log group (GitHub Actions) or print a colored header."""
    if on_github():
        echo(f"::group::{group_name}")
    else:
        echo(f"{Colors.GREEN}{group_name}{Colors.OFF}")


def end_group() -> None:
    """End the current log group (GitHub Actions only)."""
    if on_github():
        echo("::endgroup::")


def set_github_output(key: str, value: str) -> None:
    """Write a key=value pair to the GitHub Actions output file."""
    if on_github():
        with open(os.environ["GITHUB_OUTPUT"], "a") as fh:
            print(f"{key}={value}", file=fh)
