# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
"""Bundle a Python app into a standalone directory or zip.

The bundler:
  1. Reads info.yaml and verifies the app is a Python app.
  2. Scans all app .py files for imports from qai_hub_apps_utils.
  3. Copies only the needed SDK modules into the bundle (preserving the
     qai_hub_apps_utils/ directory structure so imports work unchanged).
  4. Reads the base SDK requirements.txt and requirements-<module>.txt for
     each copied SDK module, then merges with the app's requirements.txt
     into requirements.txt.
  5. Writes the result to --output_dir as a directory, or as
     <app_id>.zip when --zip is passed.
"""

from __future__ import annotations

import argparse
import shutil
import sys
import tempfile
import zipfile
from pathlib import Path

from qai_hub_apps_test.bundlers.python.requirements import (
    merge_requirements,
    read_module_requirements,
)
from qai_hub_apps_test.bundlers.python.sdk_collector import (
    collect_all_sdk_files,
    init_files_for_sdk_file,
)
from qai_hub_apps_test.bundlers.python.sdk_resolver import resolve_sdk_root
from qai_hub_apps_test.configs.info_yaml import AppLanguage, QAIHAAppInfo


def bundle(
    app_root: Path, output_dir: Path, sdk_parent: Path, make_zip: bool = False
) -> None:
    app_info, _ = QAIHAAppInfo.from_app(app_root)

    if AppLanguage.PYTHON not in app_info.languages:
        sys.exit(
            f"error: '{app_root}' is not a Python app "
            f"(languages={[l.value for l in app_info.languages]}). "
            "Expected 'Python' in languages."
        )

    app_id = app_info.id
    print(f"Bundling app '{app_id}' from {app_root}")

    # Collect needed SDK files
    sdk_files = collect_all_sdk_files(app_root, sdk_parent)
    if not sdk_files:
        print(
            "No qai_hub_apps_utils imports found; bundle will contain only app files."
        )
    else:
        print(f"Found {len(sdk_files)} SDK module file(s) to include.")

    # Collect requirements: base SDK requirements + per-module requirements files
    sdk_requires: list[str] = []
    sdk_base_req_file = sdk_parent / "requirements.txt"
    if sdk_base_req_file.exists():
        for line in sdk_base_req_file.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if line and not line.startswith("#"):
                sdk_requires.append(line)
    for sdk_file in sdk_files:
        sdk_requires.extend(read_module_requirements(sdk_file))

    # Merge requirements
    app_req_file = app_root / "requirements.txt"
    merged_reqs = merge_requirements(app_req_file, sdk_requires)

    # Collect __init__.py files needed for SDK package structure
    all_sdk_files: set[Path] = set(sdk_files)
    for sdk_file in sdk_files:
        all_sdk_files.update(init_files_for_sdk_file(sdk_file, sdk_parent))

    output_dir.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory() as _tmp:
        tmp_dir = Path(_tmp) / app_id
        shutil.copytree(app_root, tmp_dir)

        # SDK files
        for sdk_file in sorted(all_sdk_files):
            arcname = sdk_file.relative_to(sdk_parent)
            target = tmp_dir / arcname
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(sdk_file, target)

        # requirements.txt
        bundle_reqs_content = "\n".join(merged_reqs) + "\n" if merged_reqs else ""
        (tmp_dir / "requirements.txt").write_text(bundle_reqs_content, encoding="utf-8")

        if make_zip:
            zip_path = output_dir / f"{app_id}.zip"
            with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
                for f in sorted(tmp_dir.rglob("*")):
                    if f.is_file():
                        zf.write(f, f.relative_to(tmp_dir))
            print(f"Bundle written to: {zip_path}")
        else:
            dest = output_dir / app_id
            if dest.exists():
                shutil.rmtree(dest)
            shutil.copytree(tmp_dir, dest)
            print(f"Bundle written to: {dest}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Bundle a Python app and its qai_hub_apps_utils dependencies."
    )
    parser.add_argument(
        "app_root",
        type=Path,
        help="Root directory of the app (must contain info.yaml).",
    )
    parser.add_argument(
        "--output_dir",
        type=Path,
        required=True,
        help="Directory where the bundle will be written.",
    )
    parser.add_argument(
        "--sdk_root",
        type=str,
        default=None,
        help=(
            "Path to the directory containing the qai_hub_apps_utils package. "
            "Auto-detected from the repo structure if omitted."
        ),
    )
    parser.add_argument(
        "--zip",
        action="store_true",
        default=False,
        help="Package the bundle as a zip file instead of copying to a directory.",
    )
    args = parser.parse_args()

    app_root = args.app_root.resolve()
    if not app_root.is_dir():
        sys.exit(f"error: app_root '{app_root}' is not a directory.")

    sdk_parent = resolve_sdk_root(args.sdk_root)
    bundle(app_root, args.output_dir.resolve(), sdk_parent, make_zip=args.zip)


if __name__ == "__main__":
    main()
