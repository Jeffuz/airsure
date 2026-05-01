# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from pathlib import Path

import pytest

from qai_hub_apps_test.configs.info_yaml import QAIHAAppInfo
from qai_hub_apps_test.test.helpers import path_idfn
from qai_hub_apps_test.utils.paths import get_all_apps


@pytest.mark.parametrize("app_dir", get_all_apps(), ids=path_idfn)
def test_load_app_info_yaml(app_dir: Path) -> None:
    try:
        QAIHAAppInfo.from_app(app_dir)
    except Exception as e:
        raise ValueError(f"Unable to load info.yaml for app: {app_dir} | {e!s}") from e
