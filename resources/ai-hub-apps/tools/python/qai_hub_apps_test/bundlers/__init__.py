# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

from pathlib import Path
from typing import Protocol

from qai_hub_apps_test.bundlers.python.bundle import bundle as _python_bundle
from qai_hub_apps_test.bundlers.python.sdk_resolver import resolve_sdk_root
from qai_hub_apps_test.configs.info_yaml import AppLanguage, QAIHAAppInfo
from qai_hub_apps_test.utils.paths import find_app_dir


class Bundler(Protocol):
    def __call__(
        self, app_root: Path, output_dir: Path, sdk_parent: Path, make_zip: bool = False
    ) -> None: ...


def bundle_app(
    app: str | Path,
    output_dir: Path,
    sdk_parent: Path | None = None,
    make_zip: bool = False,
) -> None:
    """
    Bundle an app by app ID or directory path.

    Parameters
    ----------
    app:
        Either a string app ID (resolved via find_app_dir) or a Path to
        the app's root directory.
    output_dir:
        Directory where the bundle will be written.
    sdk_parent:
        Path to the directory containing qai_hub_apps_utils. Auto-resolved
        from the repository structure if None.
    make_zip:
        If True, produce a zip archive; otherwise copy to a subdirectory.

    Raises
    ------
    NotImplementedError
        If the app does not include Python in its languages.
    """
    app_dir: Path = find_app_dir(app) if isinstance(app, str) else app
    app_info, _ = QAIHAAppInfo.from_app(app_dir)

    if AppLanguage.PYTHON not in app_info.languages:
        raise NotImplementedError(
            f"App '{app_info.id}' does not support Python bundling "
            f"(languages={[lang.value for lang in app_info.languages]}). "
            "Only Python apps can be bundled at this time."
        )

    if sdk_parent is None:
        sdk_parent = resolve_sdk_root(None)

    _python_bundle(app_dir, output_dir, sdk_parent, make_zip)
