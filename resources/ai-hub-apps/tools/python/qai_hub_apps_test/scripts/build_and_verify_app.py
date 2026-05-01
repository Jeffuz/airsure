# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
import os

from qai_hub_models.models.common import Precision
from qai_hub_models.scorecard.device import ScorecardDevice
from tap import Tap
from typing_extensions import assert_never

from qai_hub_apps_test.configs.info_yaml import AppType, QAIHAAppInfo
from qai_hub_apps_test.configs.versions_yaml import VersionsRegistry
from qai_hub_apps_test.utils.android.android_gradle_helpers import (
    build_android_app,
    clean_android_app,
    verify_android_app_versions_match,
)
from qai_hub_apps_test.utils.models.verify_model import verify_model_asset_is_compatible
from qai_hub_apps_test.utils.verify_result import VerifyResult
from qai_hub_apps_test.utils.windows.windows_vs_helpers import (
    build_windows_app,
    clean_windows_app,
    verify_windows_app_versions_match,
)


class BuildAppParser(Tap):
    app: str  # The app name, identified by the subfolder under 'apps'. For example, 'android/ObjectDetection'
    model_id: str | None = (
        None  # The model ID to build the app with. If None, can use any model supported by the given app.
    )
    precision: Precision | None = (
        None  # The precision to build the app with. If None, can use any model precision supported by the given app, usually float.
    )
    device: ScorecardDevice | None = (
        None  # The device to build the app for. If None, uses a generic android device, or raises an error if a device must be specified for the given app.
    )
    qaihm_version: str | None = (
        None  # The AI Hub Models version from which the model should be fetched. If None, uses the currently installed version of AI Hub Models.
    )
    verify: bool = True  # If True, verifies the app and model match standard SDK versions required by this repository before building.
    build: bool = True  # If True, builds the app.
    clean: bool = False  # If True, cleans the build. If combined with `build=True`, the cleaning will happen before the build.

    def configure(self) -> None:
        self.add_argument("--precision", type=Precision.parse)
        self.add_argument("--device", type=ScorecardDevice.parse)


def main() -> None:
    args = BuildAppParser().parse_args()
    versions = VersionsRegistry.load()
    app_info, app_root = QAIHAAppInfo.from_app(args.app)
    versions = VersionsRegistry.load()

    model_id = args.model_id or app_info.related_models[0]
    precision = args.precision or app_info.precisions[0]
    device = args.device or app_info.app_type.default_device

    if args.verify:
        verify_model_asset_is_compatible(
            versions,
            model_id,
            app_info.runtime,
            precision,
            device,
            qaihm_version_tag=args.qaihm_version,
        )

        verify_result = verify_app(
            app_info,
            app_root,
            versions,
        )
        assert not verify_result.has_errors, (
            f"App verification has errors:\n{verify_result.pretty_errors}"
        )
        if verify_result.has_warnings:
            print(f"App verification has warnings:\n: {verify_result.pretty_warnings}")

    if args.build:
        build_app(
            app_info,
            app_root,
            model_id,
            precision,
            args.device,
            args.qaihm_version,
            args.clean,
        )
    elif args.clean:
        clean_app(app_info, app_root)


def build_app(
    app_info: QAIHAAppInfo,
    app_root: str | os.PathLike,
    model_id: str,
    precision: Precision,
    device: ScorecardDevice | None = None,
    qaihm_version_tag: str | None = None,
    clean_build: bool = False,
) -> None:
    """
    Builds an application using the specified configuration and model parameters.

    Parameters
    ----------
    app_info
        Metadata and configuration details for the app to be built.
    app_root
        Path to the root directory of the application source code.
    model_id
        AI Hub Models ID for the model to be embedded in the app. If None, any app-supported model is included.
    precision
        Desired precision level for the model (e.g., FP32, INT8). If None, any supported precision for the app may be used.
    device
        Target device for fetching model assets. If None, assets used by this app must be universal.
    qaihm_version_tag
        Version tag of AI Hub Models from which models should be fetched. If None, the currently installed version is used.
    clean_build
        If True, performs a clean build by removing previous build artifacts.
    """
    if app_info.app_type == AppType.ANDROID:
        build_android_app(
            app_info,
            app_root,
            model_id,
            precision,
            device,
            qaihm_version_tag,
            clean_build,
        )
    elif app_info.app_type == AppType.WINDOWS:
        build_windows_app(
            app_info,
            app_root,
            model_id,
            precision,
            device,
            qaihm_version_tag,
            clean_build,
        )
    elif app_info.app_type == AppType.UBUNTU:
        raise NotImplementedError("Building Ubuntu Python apps is not supported.")
    else:
        assert_never(app_info.app_type)


def verify_app(
    app_info: QAIHAAppInfo,
    app_root: str | os.PathLike,
    versions: VersionsRegistry,
) -> VerifyResult:
    """
    Verifies that the app at the given root directory
    uses the same versions of runtimes as are listed in the versions yaml.

    Parameters
    ----------
    app_info
        App metadata
    app_root
        Path to the root directory of the application source code.
    versions
        Registry of version information for dependencies and components.

    Returns
    -------
    VerifyResult
        The warnings and errors produced by this verification process.
    """
    if app_info.app_type == AppType.ANDROID:
        verify_result = verify_android_app_versions_match(app_root, app_info, versions)
    elif app_info.app_type == AppType.WINDOWS:
        verify_result = verify_windows_app_versions_match(app_root, app_info, versions)
    elif app_info.app_type == AppType.UBUNTU:
        verify_result = VerifyResult()
    else:
        assert_never(app_info.app_type)

    return verify_result


def clean_app(app_info: QAIHAAppInfo, app_root: str | os.PathLike) -> None:
    """
    Clean previous builds for this app.

    Parameters
    ----------
    app_info
        App metadata
    app_root
        Path to the root directory of the application source code.
    """
    if app_info.app_type == AppType.ANDROID:
        clean_android_app(app_root)
    elif app_info.app_type == AppType.WINDOWS:
        clean_windows_app(app_root)


if __name__ == "__main__":
    main()
