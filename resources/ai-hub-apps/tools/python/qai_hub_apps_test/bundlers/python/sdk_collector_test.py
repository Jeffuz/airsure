# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

from pathlib import Path

import pytest

from qai_hub_apps_test.bundlers.python.sdk_collector import (
    collect_all_sdk_files,
    collect_sdk_imports_from_file,
    init_files_for_sdk_file,
    module_to_sdk_file,
)

pytestmark = pytest.mark.bundler_unit

_SDK = "qai_hub_apps_utils"


def test_no_sdk_imports(tmp_path: Path) -> None:
    f = tmp_path / "app.py"
    f.write_text("import os\nimport sys\n")
    assert collect_sdk_imports_from_file(f) == set()


def test_direct_sdk_import(tmp_path: Path) -> None:
    f = tmp_path / "app.py"
    f.write_text(f"import {_SDK}\n")
    assert collect_sdk_imports_from_file(f) == {_SDK}


def test_from_submodule_import(tmp_path: Path) -> None:
    f = tmp_path / "app.py"
    f.write_text(f"from {_SDK}.draw import something\n")
    assert collect_sdk_imports_from_file(f) == {f"{_SDK}.draw"}


def test_submodule_direct_import(tmp_path: Path) -> None:
    f = tmp_path / "app.py"
    f.write_text(f"import {_SDK}.image_processing\n")
    assert collect_sdk_imports_from_file(f) == {f"{_SDK}.image_processing"}


def test_syntax_error_warns_and_returns_empty(tmp_path: Path) -> None:
    f = tmp_path / "broken.py"
    f.write_text("def foo(\n")
    with pytest.warns(UserWarning, match="Could not parse"):
        result = collect_sdk_imports_from_file(f)
    assert result == set()


def test_multiple_sdk_imports(tmp_path: Path) -> None:
    f = tmp_path / "app.py"
    f.write_text(
        f"from {_SDK}.draw import draw_boxes\n"
        f"import {_SDK}.image_processing\n"
        "import os\n"
    )
    assert collect_sdk_imports_from_file(f) == {
        f"{_SDK}.draw",
        f"{_SDK}.image_processing",
    }


def test_resolves_module_file(tmp_path: Path) -> None:
    sdk_dir = tmp_path / _SDK
    sdk_dir.mkdir()
    draw = sdk_dir / "draw.py"
    draw.write_text("")
    result = module_to_sdk_file(f"{_SDK}.draw", tmp_path)
    assert result == draw


def test_resolves_package_init(tmp_path: Path) -> None:
    sdk_dir = tmp_path / _SDK
    sdk_dir.mkdir()
    init = sdk_dir / "__init__.py"
    init.write_text("")
    result = module_to_sdk_file(_SDK, tmp_path)
    assert result == init


def test_module_file_takes_priority_over_init(tmp_path: Path) -> None:
    sdk_dir = tmp_path / _SDK
    sdk_dir.mkdir()
    (sdk_dir / "__init__.py").write_text("")
    sub = sdk_dir / "sub"
    sub.mkdir()
    (sub / "__init__.py").write_text("")
    mod_file = sdk_dir / "sub.py"
    mod_file.write_text("")
    result = module_to_sdk_file(f"{_SDK}.sub", tmp_path)
    assert result == mod_file


def test_neither_exists_returns_none(tmp_path: Path) -> None:
    assert module_to_sdk_file(f"{_SDK}.missing", tmp_path) is None


def test_collects_inits_along_path(tmp_path: Path) -> None:
    pkg = tmp_path / _SDK
    pkg.mkdir()
    pkg_init = pkg / "__init__.py"
    pkg_init.write_text("")
    sub = pkg / "sub"
    sub.mkdir()
    sub_init = sub / "__init__.py"
    sub_init.write_text("")
    mod = sub / "module.py"
    mod.write_text("")

    inits = init_files_for_sdk_file(mod, tmp_path)
    assert pkg_init in inits
    assert sub_init in inits


def test_missing_init_not_included(tmp_path: Path) -> None:
    pkg = tmp_path / _SDK
    pkg.mkdir()
    sub = pkg / "sub"
    sub.mkdir()
    sub_init = sub / "__init__.py"
    sub_init.write_text("")
    mod = sub / "module.py"
    mod.write_text("")

    inits = init_files_for_sdk_file(mod, tmp_path)
    assert sub_init in inits
    assert (pkg / "__init__.py") not in inits


def test_file_in_root_returns_empty(tmp_path: Path) -> None:
    mod = tmp_path / "module.py"
    mod.write_text("")
    assert init_files_for_sdk_file(mod, tmp_path) == []


def test_no_sdk_imports_empty_result(tmp_path: Path) -> None:
    app = tmp_path / "app"
    app.mkdir()
    (app / "main.py").write_text("import os\n")
    sdk = tmp_path / "sdk"
    sdk.mkdir()
    assert collect_all_sdk_files(app, sdk) == set()


def test_direct_import_resolved(tmp_path: Path) -> None:
    app = tmp_path / "app"
    app.mkdir()
    (app / "main.py").write_text(f"from {_SDK}.draw import x\n")

    sdk = tmp_path / "sdk"
    sdk.mkdir()
    sdk_pkg = sdk / _SDK
    sdk_pkg.mkdir()
    draw_py = sdk_pkg / "draw.py"
    draw_py.write_text("# no further imports\n")

    result = collect_all_sdk_files(app, sdk)
    assert draw_py in result


def test_transitive_imports_followed(tmp_path: Path) -> None:
    app = tmp_path / "app"
    app.mkdir()
    (app / "main.py").write_text(f"from {_SDK}.draw import x\n")

    sdk = tmp_path / "sdk"
    sdk.mkdir()
    sdk_pkg = sdk / _SDK
    sdk_pkg.mkdir()
    draw_py = sdk_pkg / "draw.py"
    draw_py.write_text(f"from {_SDK}.image_processing import y\n")
    img_py = sdk_pkg / "image_processing.py"
    img_py.write_text("# leaf\n")

    result = collect_all_sdk_files(app, sdk)
    assert draw_py in result
    assert img_py in result


def test_unresolvable_module_warns_and_continues(tmp_path: Path) -> None:
    app = tmp_path / "app"
    app.mkdir()
    (app / "main.py").write_text(f"import {_SDK}.missing_module\n")

    sdk = tmp_path / "sdk"
    sdk.mkdir()
    (sdk / _SDK).mkdir()

    with pytest.warns(UserWarning, match="could not be resolved"):
        result = collect_all_sdk_files(app, sdk)
    assert result == set()
