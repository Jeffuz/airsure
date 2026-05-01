# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
import os
import shutil
from pathlib import Path
from tempfile import TemporaryDirectory
from zipfile import Path as ZipFilePath
from zipfile import ZipFile, is_zipfile

from qai_hub_models.models.common import Precision, TargetRuntime
from qai_hub_models.scorecard.device import ScorecardDevice
from qai_hub_models.utils.fetch_static_assets import fetch_static_assets

from qai_hub_apps_test.configs.info_yaml import QAIHAAppInfo
from qai_hub_apps_test.utils.aws import (
    QAIHM_PRIVATE_S3_BUCKET,
    get_qaihm_s3,
    s3_download,
)


def install_model(
    app_root: str | os.PathLike,
    app_info: QAIHAAppInfo,
    relative_dst_model_paths: str
    | os.PathLike
    | list[os.PathLike]
    | list[str]
    | list[Path],
    model_id: str,
    runtime: TargetRuntime,
    precision: Precision,
    device: ScorecardDevice | None = None,
    qaihm_version_tag: str | None = None,
) -> list[Path]:
    """
    Install the given model in the given app.

    Parameters
    ----------
    app_root
        Path to the root directory of the Android application.
    app_info
        App metadata.
    relative_dst_model_paths
        Relative paths within the app directory where the model should be installed.
        Can be a single path or a list of multiple destination paths.
    model_id
        AI Hub Models ID of the model to be installed.
    runtime
        Type of model to install.
    precision
        Precision of the model (e.g., FP32, INT8).
    device
        Target device for fetching model assets. If None, assets for the given runtime must be universal.
    qaihm_version_tag
        Specific version tag of AI Hub Models to use for model installation. If None, the currently installed version is used.

    Returns
    -------
    list[Path]
        List of absolute paths where the model was installed within the app directory.

    Notes
    -----
    It is recommended to call verify_model_asset_is_compatible before installing a model.
    """
    # Download model asset
    with TemporaryDirectory() as tmpdir:
        if (
            device is not None
            and (model_s3 := app_info.private_model_s3_paths.get(model_id))
            and (model_s3_by_chipset := model_s3.get(precision))
            and (model_s3_path := model_s3_by_chipset.get(device.chipset))
        ):
            s3_bucket = get_qaihm_s3(QAIHM_PRIVATE_S3_BUCKET)[0]
            dst_path = os.path.join(tmpdir, os.path.basename(model_s3_path))
            s3_download(s3_bucket, model_s3_path, dst_path)
            paths = [dst_path]
        else:
            paths = fetch_static_assets(
                model_id,
                runtime,
                precision,
                device.reference_device if device is not None else None,
                qaihm_version_tag=qaihm_version_tag,
                output_folder=tmpdir,
            )[0]

        dst_paths = (
            relative_dst_model_paths
            if isinstance(relative_dst_model_paths, list)
            else [relative_dst_model_paths]
        )
        if len(paths) != len(dst_paths):
            raise ValueError(
                f"Expected {len(dst_paths)} files for model {model_id}, but got {len(paths)} files."
            )

        for src_path, relative_dst_path in zip(paths, dst_paths, strict=False):
            install_dst = Path(app_root) / relative_dst_path
            if os.path.exists(install_dst):
                if os.path.isdir(install_dst):
                    shutil.rmtree(install_dst)
                else:
                    os.remove(install_dst)

            if is_zipfile(filename=src_path):
                # Unzip contents to the install_dst
                zip_extract_dir = Path(str(src_path) + ".extracted")
                with ZipFile(src_path) as zipfile:
                    top_level_entries = list(ZipFilePath(zipfile).iterdir())
                    zipfile.extractall(zip_extract_dir)
                    if len(top_level_entries) == 1 and top_level_entries[0].is_dir():
                        # This is a special case where the zip contains a single top-level folder:
                        #   my_zip.zip
                        #      ↳ zip_root_folder
                        #         ↳ all zip contents
                        #
                        # This will move `zip_root_folder` to `install_dst` (which is defined by the above for loop).
                        shutil.move(
                            zip_extract_dir / top_level_entries[0].name, install_dst
                        )
                    else:
                        # Otherwise, just move everything in the zip file into the target folder (install_dst)
                        os.makedirs(install_dst)
                        for entry in top_level_entries:
                            shutil.move(
                                zip_extract_dir / entry.name, install_dst / entry.name
                            )

                shutil.rmtree(zip_extract_dir)
            else:
                if os.path.exists(install_dst):
                    os.remove(install_dst)
                install_dst.parent.mkdir(exist_ok=True, parents=True)
                shutil.move(src_path, install_dst)

        return [Path(x) for x in dst_paths]
