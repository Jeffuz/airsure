# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

from qai_hub_models.utils.base_config import BaseQAIHMConfig

from qai_hub_apps_test.utils.paths import APPS_ROOT

_DEFAULT_APP_VERSIONS: VersionsRegistry | None = None


class VersionsRegistry(BaseQAIHMConfig):
    """Stores default versions for all SDKs/build tools used by AI Hub Apps."""

    qairt_sdk: str
    qairt_sdk_llm: str
    onnx_runtime: str
    tf_lite: str

    # Android Specific Versions
    android_min_api: int
    android_target_api: int
    android_compile_api: int
    android_cmdline_tools: str
    android_ndk: str
    tf_lite_support: str
    gradle: str
    java_sdk: str

    @staticmethod
    def load() -> VersionsRegistry:
        """
        Load the default build tool versions cache.
        The object is a singleton and will only be created from disk once.
        """
        global _DEFAULT_APP_VERSIONS  # noqa: PLW0603
        if not _DEFAULT_APP_VERSIONS:
            _DEFAULT_APP_VERSIONS = VersionsRegistry.from_yaml(
                APPS_ROOT / "versions.yaml"
            )
        return _DEFAULT_APP_VERSIONS
