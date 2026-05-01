# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

from pathlib import Path

import pytest

import qai_hub_apps_test.bundlers.python.sdk_resolver as resolver_mod
from qai_hub_apps_test.bundlers.python.sdk_resolver import (
    _SDK_PACKAGE,
    resolve_sdk_root,
)

pytestmark = pytest.mark.bundler_unit


def _make_sdk(parent: Path) -> Path:
    """Create a minimal qai_hub_apps_utils package under parent."""
    sdk = parent / _SDK_PACKAGE
    sdk.mkdir(parents=True, exist_ok=True)
    (sdk / "__init__.py").write_text("")
    return sdk


def test_resolve_default_when_sdk_exists(
    monkeypatch: pytest.MonkeyPatch, tmp_path: Path
) -> None:
    _make_sdk(tmp_path)
    monkeypatch.setattr(resolver_mod, "_DEFAULT_SDK_PARENT", tmp_path)
    assert resolve_sdk_root(None) == tmp_path


def test_resolve_default_missing_calls_sys_exit(
    monkeypatch: pytest.MonkeyPatch, tmp_path: Path
) -> None:
    monkeypatch.setattr(resolver_mod, "_DEFAULT_SDK_PARENT", tmp_path)
    with pytest.raises(SystemExit):
        resolve_sdk_root(None)


def test_resolve_arg_parent_dir(tmp_path: Path) -> None:
    _make_sdk(tmp_path)
    result = resolve_sdk_root(str(tmp_path))
    assert result == tmp_path


def test_resolve_arg_package_dir(tmp_path: Path) -> None:
    sdk = _make_sdk(tmp_path)
    result = resolve_sdk_root(str(sdk))
    assert result == tmp_path


def test_resolve_arg_invalid_calls_sys_exit(tmp_path: Path) -> None:
    with pytest.raises(SystemExit):
        resolve_sdk_root(str(tmp_path))
