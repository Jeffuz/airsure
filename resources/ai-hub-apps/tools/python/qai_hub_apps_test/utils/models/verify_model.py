# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from packaging.version import Version
from qai_hub_models.configs.perf_yaml import QAIHMModelPerf
from qai_hub_models.models.common import Precision, QAIRTVersion, TargetRuntime
from qai_hub_models.scorecard.device import ScorecardDevice
from qai_hub_models.utils.version_helpers import QAIHMVersion

from qai_hub_apps_test.configs.versions_yaml import VersionsRegistry
from qai_hub_apps_test.utils.verify_result import VerifyResult


def verify_model_asset_is_compatible(
    versions: VersionsRegistry,
    model_id: str,
    runtime: TargetRuntime,
    precision: Precision = Precision.float,
    device: ScorecardDevice | None = None,
    components: list[str] | None = None,
    qaihm_version_tag: str | None = None,
) -> VerifyResult:
    """
    Verify the AI Hub Models asset for the given model + runtime + precision + device exists and
    is compatible with the given app versions.

    Parameters
    ----------
    versions:
        Registry of version information for dependencies and components.
    model_id
        AI Hub Models ID of the model to be installed.
    runtime
        Type of model to install.
    precision
        Precision of the model (e.g., FP32, INT8).
    device
        Target device for fetching model assets. If None, assets for the given runtime must be universal.
    components:
        Component names to verify. If None, all components are verified.
    qaihm_version_tag
        Specific version tag of AI Hub Models to use for model installation. If None, the currently installed version is used.

    Returns
    -------
    VerifyResult
        The warnings and errors produced by this verification process.
    """
    qaihm_version_tag = (
        QAIHMVersion.tag_from_string(qaihm_version_tag)
        if qaihm_version_tag
        else QAIHMVersion.current_tag
    )
    if qaihm_version_tag != QAIHMVersion.current_tag:
        return VerifyResult()  # verification for older releases is not possible

    errors = []
    perf_precision = QAIHMModelPerf.from_model(model_id).precisions.get(precision)
    if not perf_precision:
        errors.append(
            f"Precision {perf_precision} is not supported for model {model_id}."
        )
        return VerifyResult(errors)

    perf_components = (
        [
            (component, perf_precision.components.get(component))
            for component in components
        ]
        if isinstance(components, list)
        else list(perf_precision.components.items())
    )

    for component_name, perf_component in perf_components:
        if perf_component is None:
            errors.append(f"Component {component_name} not found for model {model_id}.")
            continue

        if runtime.is_aot_compiled:
            if device is None:
                errors.append(
                    "Must specify a device if the runtime is compiled ahead-of-time."
                )
                break

            device_assets = perf_component.device_assets.get(device)
            rt_assets = (
                [v for k, v in device_assets.items() if k.runtime == runtime]
                if device_assets
                else None
            )
        else:
            rt_assets = [
                v
                for k, v in perf_component.universal_assets.items()
                if k.runtime == runtime
            ]

        if not rt_assets:
            errors.append(
                f"Asset for device {device} / runtime {runtime.value} not found for component {component_name}"
            )
            continue

        toolchains = rt_assets[0].tool_versions
        if toolchains.qairt is not None:
            app_qairt_version = QAIRTVersion(
                versions.qairt_sdk_llm
                if runtime.is_exclusively_for_genai
                else versions.qairt_sdk,
                validate_exists_on_ai_hub=False,
            )
            if (app_qairt_version.api_version != toolchains.qairt.api_version) or (
                app_qairt_version.sdk_flavor is not None
                and app_qairt_version.sdk_flavor != toolchains.qairt.sdk_flavor
            ):
                errors.append(
                    f"Incompatible QAIRT version for model {model_id}. Versions.yaml states this app uses version {app_qairt_version.full_version_with_flavor} but model was compiled with version {toolchains.qairt.full_version_with_flavor}."
                )

        if toolchains.tflite is not None:
            app_version = versions.tf_lite
            if Version(toolchains.tflite) > Version(app_version):
                errors.append(
                    f"Incompatible TF Lite version for model {model_id}. Versions.yaml states this app uses version {app_version} but model was compiled with version {toolchains.tflite}."
                )

        if toolchains.onnx_runtime is not None:
            app_version = versions.onnx_runtime
            if Version(toolchains.onnx_runtime) > Version(app_version):
                errors.append(
                    f"Incompatible ONNX Runtime version for model {model_id}. Versions.yaml states this app uses version {app_version} but model was compiled with version {toolchains.tflite}."
                )

    return VerifyResult(errors)
