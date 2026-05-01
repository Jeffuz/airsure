# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from pathlib import Path

import pytest

from qai_hub_apps_test.configs.info_yaml import QAIHAAppInfo
from qai_hub_apps_test.configs.versions_yaml import VersionsRegistry
from qai_hub_apps_test.scripts.build_and_verify_app import (
    build_app,
    clean_app,
    verify_app,
)
from qai_hub_apps_test.test.helpers import path_idfn
from qai_hub_apps_test.utils.models.verify_model import verify_model_asset_is_compatible
from qai_hub_apps_test.utils.paths import get_all_apps
from qai_hub_apps_test.utils.verify_result import VerifyResult


def skip_if_requested(app_info: QAIHAAppInfo) -> None:
    if app_info.skip_test:
        pytest.skip(
            f'Skipping {app_info.name} as requested due to "{app_info.skip_test}"'
        )


def assert_verify_result(verify_result: VerifyResult) -> None:
    assert not verify_result.has_errors, (
        f"App verification has errors:\n{verify_result.pretty_errors}"
    )
    if verify_result.has_warnings:
        print(f"App verification has warnings:\n: {verify_result.pretty_warnings}")


@pytest.mark.parametrize("app_dir", get_all_apps(), ids=path_idfn)
def test_verify_apps(app_dir: Path) -> None:
    app_info, app_root = QAIHAAppInfo.from_app(app_dir)

    skip_if_requested(app_info)

    versions = VersionsRegistry.load()
    verify_app(app_info, app_root, versions)

    for model_id in app_info.related_models:
        for precision in app_info.precisions:
            verify_model_asset_is_compatible(
                versions,
                model_id,
                app_info.runtime,
                precision,
                app_info.app_type.default_device,
                qaihm_version_tag=app_info.qaihm_version,
            )


@pytest.mark.parametrize("app_dir", get_all_apps(), ids=path_idfn)
def test_build_apps(app_dir: Path) -> None:
    app_info, app_root = QAIHAAppInfo.from_app(app_dir)

    skip_if_requested(app_info)

    for model_id in app_info.related_models:
        for precision in app_info.precisions:
            try:
                build_app(
                    app_info,
                    app_root,
                    model_id,
                    precision,
                    app_info.app_type.default_device,
                    app_info.qaihm_version,
                    True,
                )
            finally:
                # CI machines run out of disk space without this step.
                print("Build test complete; cleaning up build artifacts.")
                clean_app(app_info, app_root)
