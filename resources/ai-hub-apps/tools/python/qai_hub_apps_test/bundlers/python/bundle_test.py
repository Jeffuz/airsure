# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

import zipfile
from pathlib import Path

import pytest

from qai_hub_apps_test.bundlers.python.bundle import bundle
from qai_hub_apps_test.configs.info_yaml import AppLanguage
from qai_hub_apps_test.conftest import make_sample_app_info

pytestmark = pytest.mark.bundler_unit

_SDK = "qai_hub_apps_utils"


def test_non_python_app_exits(tmp_path: Path) -> None:
    app_dir = tmp_path / "myapp"
    app_dir.mkdir()
    make_sample_app_info(id="myapp", languages=[AppLanguage.CPP]).to_yaml(
        app_dir / "info.yaml", write_if_empty=True
    )
    sdk = tmp_path / "sdk"
    (sdk / _SDK).mkdir(parents=True)
    (sdk / _SDK / "__init__.py").write_text("")
    with pytest.raises(SystemExit):
        bundle(app_dir, tmp_path / "out", sdk)


def test_bundle_creates_output_dir(
    dummy_python_app_path: Path, dummy_python_sdk_path: Path, tmp_path: Path
) -> None:
    out = tmp_path / "out"
    bundle(dummy_python_app_path, out, dummy_python_sdk_path)
    assert (out / "my_dummy_app").is_dir()


def test_bundle_copies_app_files(
    dummy_python_app_path: Path, dummy_python_sdk_path: Path, tmp_path: Path
) -> None:
    out = tmp_path / "out"
    bundle(dummy_python_app_path, out, dummy_python_sdk_path)
    assert (out / "my_dummy_app" / "main.py").exists()


def test_bundle_writes_requirements_txt(
    dummy_python_app_path: Path, dummy_python_sdk_path: Path, tmp_path: Path
) -> None:
    out = tmp_path / "out"
    bundle(dummy_python_app_path, out, dummy_python_sdk_path)
    reqs = (out / "my_dummy_app" / "requirements.txt").read_text()
    assert "Pillow>=9.0" in reqs


def test_bundle_no_sdk_imports_no_sdk_dir(tmp_path: Path) -> None:
    app_dir = tmp_path / "myapp"
    app_dir.mkdir()
    make_sample_app_info(id="myapp").to_yaml(app_dir / "info.yaml", write_if_empty=True)
    (app_dir / "main.py").write_text("import os\n")
    sdk = tmp_path / "sdk"
    (sdk / _SDK).mkdir(parents=True)
    (sdk / _SDK / "__init__.py").write_text("")
    bundle(app_dir, tmp_path / "out", sdk)
    assert not (tmp_path / "out" / "myapp" / _SDK).exists()


def test_bundle_with_sdk_import_copies_sdk_file(
    dummy_python_app_path: Path, dummy_python_sdk_path: Path, tmp_path: Path
) -> None:
    out = tmp_path / "out"
    bundle(dummy_python_app_path, out, dummy_python_sdk_path)
    assert (out / "my_dummy_app" / _SDK / "helper.py").exists()


def test_bundle_make_zip_creates_zip_file(
    dummy_python_app_path: Path, dummy_python_sdk_path: Path, tmp_path: Path
) -> None:
    out = tmp_path / "out"
    bundle(dummy_python_app_path, out, dummy_python_sdk_path, make_zip=True)
    assert (out / "my_dummy_app.zip").is_file()


def test_bundle_zip_contains_app_files(
    dummy_python_app_path: Path, dummy_python_sdk_path: Path, tmp_path: Path
) -> None:
    out = tmp_path / "out"
    bundle(dummy_python_app_path, out, dummy_python_sdk_path, make_zip=True)
    with zipfile.ZipFile(out / "my_dummy_app.zip") as zf:
        names = zf.namelist()
    assert "main.py" in names


def test_bundle_overwrites_existing_dest(
    dummy_python_app_path: Path, dummy_python_sdk_path: Path, tmp_path: Path
) -> None:
    out = tmp_path / "out"
    bundle(dummy_python_app_path, out, dummy_python_sdk_path)
    (dummy_python_app_path / "main.py").write_text("# v2\n")
    bundle(dummy_python_app_path, out, dummy_python_sdk_path)
    assert "v2" in (out / "my_dummy_app" / "main.py").read_text()
