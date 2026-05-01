# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

from pathlib import Path
from unittest.mock import MagicMock

import pytest

import qai_hub_apps_test.bundlers as bundlers_mod
from qai_hub_apps_test.bundlers import bundle_app
from qai_hub_apps_test.configs.info_yaml import AppLanguage
from qai_hub_apps_test.conftest import make_sample_app_info

pytestmark = pytest.mark.bundler_unit

_SDK = "qai_hub_apps_utils"


def test_bundle_app_by_path_python_app(
    dummy_python_app_path: Path,
    dummy_python_sdk_path: Path,
    tmp_path: Path,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    mock_bundle = MagicMock()
    monkeypatch.setattr(bundlers_mod, "_python_bundle", mock_bundle)

    out = tmp_path / "out"
    bundle_app(dummy_python_app_path, out, sdk_parent=dummy_python_sdk_path)

    mock_bundle.assert_called_once_with(
        dummy_python_app_path, out, dummy_python_sdk_path, False
    )


def test_bundle_app_by_str_id_resolves_dir(
    dummy_python_app_path: Path,
    dummy_python_sdk_path: Path,
    tmp_path: Path,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    mock_bundle = MagicMock()
    monkeypatch.setattr(bundlers_mod, "_python_bundle", mock_bundle)
    monkeypatch.setattr(bundlers_mod, "find_app_dir", lambda _: dummy_python_app_path)

    out = tmp_path / "out"
    bundle_app("my_dummy_app", out, sdk_parent=dummy_python_sdk_path)

    mock_bundle.assert_called_once_with(
        dummy_python_app_path, out, dummy_python_sdk_path, False
    )


def test_bundle_app_non_python_raises(tmp_path: Path) -> None:
    app_dir = tmp_path / "myapp"
    app_dir.mkdir()
    make_sample_app_info(id="myapp", languages=[AppLanguage.CPP]).to_yaml(
        app_dir / "info.yaml", write_if_empty=True
    )
    with pytest.raises(NotImplementedError):
        bundle_app(app_dir, tmp_path / "out")


def test_bundle_app_auto_resolves_sdk_parent(
    dummy_python_app_path: Path, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    fake_sdk = tmp_path / "fake_sdk"
    mock_bundle = MagicMock()
    monkeypatch.setattr(bundlers_mod, "_python_bundle", mock_bundle)
    monkeypatch.setattr(bundlers_mod, "resolve_sdk_root", lambda _: fake_sdk)

    out = tmp_path / "out"
    bundle_app(dummy_python_app_path, out)

    mock_bundle.assert_called_once_with(dummy_python_app_path, out, fake_sdk, False)


def test_bundle_python_app_e2e(
    dummy_python_app_path: Path, dummy_python_sdk_path: Path, tmp_path: Path
) -> None:
    """E2E: bundle a Python app that imports from a dummy SDK (no mocks).

    Verifies that bundle_app:
    - copies app source files
    - resolves and copies the directly imported SDK module
    - follows transitive SDK imports (helper -> math_utils)
    - includes the SDK package __init__.py
    - does NOT copy unreferenced SDK modules
    - merges app requirements with per-module SDK requirements
    """
    out_dir = tmp_path / "out"
    bundle_app(dummy_python_app_path, out_dir, sdk_parent=dummy_python_sdk_path)

    bundle = out_dir / "my_dummy_app"
    assert bundle.is_dir()

    # app source
    assert (bundle / "main.py").exists()

    # directly imported SDK module + package init
    assert (bundle / _SDK / "__init__.py").exists()
    assert (bundle / _SDK / "helper.py").exists()

    # transitively imported SDK module
    assert (bundle / _SDK / "math_utils.py").exists()

    # unreferenced module NOT copied
    assert not (bundle / _SDK / "unreferenced.py").exists()

    # merged requirements: app dep + per-module SDK dep
    reqs = (bundle / "requirements.txt").read_text()
    assert "Pillow>=9.0" in reqs
    assert "numpy>=1.24" in reqs
