# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

import warnings

from packaging.version import Version
from pydantic import model_validator

from qai_hub_apps import __version__, _is_dev
from qai_hub_apps.configs.app_yaml import AppInfo
from qai_hub_apps.configs.base_config import BaseConfig


class AppRegistry(BaseConfig):
    schema_version: str
    min_cli_version: str
    version: str | None = None
    apps: list[AppInfo]

    @model_validator(mode="after")
    def _unique_ids(self) -> AppRegistry:
        ids = [a.id for a in self.apps]
        dupes = {i for i in ids if ids.count(i) > 1}
        if dupes:
            raise ValueError(f"Registry contains duplicate app IDs: {sorted(dupes)}")
        return self

    @model_validator(mode="after")
    def _check_cli_version(self) -> AppRegistry:
        if self.version is None:
            msg = (
                "Registry has no version (dev registry). "
                "Use generate_registry --build_and_upload to produce a versioned registry."
            )
            if _is_dev():
                warnings.warn(msg, stacklevel=2)
            else:
                raise ValueError(
                    "Registry is missing a version. "
                    "The registry may be corrupt. Please upgrade: pip install -U qai-hub-apps"
                )
            return self

        cli = Version(__version__)
        for required, reason in [
            (self.min_cli_version, f"requires qai-hub-apps >= {self.min_cli_version}"),
            (self.version, f"was released with qai-hub-apps {self.version}"),
        ]:
            if cli < Version(required):
                msg = (
                    f"This registry {reason}, "
                    f"but you have {__version__}. Please upgrade: pip install -U qai-hub-apps"
                )
                if _is_dev():
                    warnings.warn(msg, stacklevel=2)
                else:
                    raise ValueError(msg)
        return self
