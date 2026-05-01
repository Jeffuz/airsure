# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

import shutil
import tempfile
from collections.abc import ValuesView
from pathlib import Path
from typing import Any

from qai_hub_models_cli.fetch import get_asset_url
from qai_hub_models_cli.utils import download, get_next_free_path
from qai_hub_models_cli.versions import CURRENT_VERSION as QAIHM_VERSION

from qai_hub_apps import _is_dev

try:
    from qai_hub_apps_test.bundlers import bundle_app as _bundle_app
except ImportError:  # pragma: no cover
    _bundle_app = None
from qai_hub_apps.configs.app_yaml import AppInfo, AppLanguage
from qai_hub_apps.configs.model_asset import ModelAsset
from qai_hub_apps.configs.registry_yaml import AppRegistry
from qai_hub_apps.errors import (
    AppIncompatibleError,
    AppNotFoundError,
    ModelAssetNotFoundError,
    QAIHubAppsError,
)
from qai_hub_apps.validate import is_app_supported


class App:
    """CLI-layer wrapper around AppInfo. Owns presentation logic."""

    def __init__(self, info: AppInfo) -> None:
        self._info = info

    def __getattr__(self, name: str) -> Any:
        # Transparently delegate field access to the underlying AppInfo.
        return getattr(self._info, name)

    def fetch(
        self,
        dest: Path,
        model_asset: ModelAsset | None = None,
    ) -> Path:
        """Download and extract app source. Returns the extraction path."""
        app_dest = dest / self.id

        if app_dest.exists():
            new_dest = get_next_free_path(app_dest)
            print(f"Warning: {app_dest} already exists, saving to {new_dest} instead.")
            app_dest = new_dest

        has_url = self.url is not None
        with tempfile.TemporaryDirectory() as _tmp:
            tmp = Path(_tmp)
            staged = tmp / self.id

            source_download_url = None
            if not has_url and _is_dev():
                # Dev install + no URL: bundle from source on-the-fly
                if _bundle_app is None:
                    raise QAIHubAppsError(
                        "Dev install detected but qai_hub_apps_test is not installed. "
                        "Install it with: pip install -e tools/python/"
                    )
                print(f"Dev install: bundling '{self.id}' from source...")
                # bundle_app with make_zip=False writes to tmp/<app_id>/ == staged
                _bundle_app(self.id, tmp, make_zip=False)
            elif not has_url:
                raise QAIHubAppsError(
                    "No source URL found in registry. "
                    "The registry may be outdated. Please upgrade: pip install -U qai-hub-apps"
                )
            else:
                source_download_url = self.url.source

            model_download_url = None
            if model_asset is not None:
                if model_asset.model_id not in self.related_models:
                    available = ", ".join(self.related_models) or "none"
                    raise AppIncompatibleError(
                        f"Model '{model_asset.model_id}' is not supported for this app. Supported models: {available}"
                    )

                if not self.model_file_path:
                    raise AppIncompatibleError(
                        f"No model_file_path configured for app '{self.id}'."
                    )

                try:
                    model_download_url = get_asset_url(
                        model_asset.model_id,
                        runtime=self.runtime,
                        precision=self.precisions[0],
                        version=QAIHM_VERSION,
                        chipset=model_asset.chipset,
                    )
                except FileNotFoundError as e:
                    raise ModelAssetNotFoundError(
                        model_asset.model_id, model_asset.chipset
                    ) from e

            if source_download_url:
                print(f"Fetching from: {source_download_url}")
                staged = download(source_download_url, staged, extract=True)

            if model_download_url:
                models_dir = staged / self.model_file_path
                download(model_download_url, models_dir, extract=True)

            shutil.move(staged, app_dest)

        return app_dest

    def __repr__(self) -> str:
        lines = [self.name, "\u2550" * 50, ""]
        for label, value in self.detail_fields():
            lines.append(f"{label + ':':<12}{value}")
        lines.append("")
        if self.headline:
            lines.append(f"{self.headline}\n")
        if self.description:
            lines.append(f"{self.description}\n")
        if self.app_repo_url:
            lines.append(f"Repo:  {self.app_repo_url}")
        return "\n".join(lines)

    def detail_fields(self) -> list[tuple[str, str]]:
        fields: list[tuple[str, str]] = [
            ("ID", self.id or "-"),
            ("Type", self.app_type.value),
        ]
        if self.runtime:
            fields.append(("Runtime", self.runtime.value))
        if self.domain:
            fields.append(("Domain", self.domain))
        if self.use_case:
            fields.append(("Use Case", self.use_case))
        if self.precisions:
            fields.append(("Precisions", ", ".join(p.value for p in self.precisions)))
        if self.related_models:
            fields.append(("Models", ", ".join(str(m) for m in self.related_models)))
        return fields


class Registry:
    """CLI-layer wrapper around AppRegistry. Singleton — one instance per process."""

    _instance: Registry | None = None

    def __init__(self, raw: AppRegistry) -> None:
        self._raw = raw
        self._apps = {a.id: _make_app(a) for a in raw.apps}

    @classmethod
    def load(cls, path: str | Path) -> Registry:
        if cls._instance is None:
            cls._instance = cls(AppRegistry.from_yaml(Path(path)))
        return cls._instance

    @property
    def apps(self) -> ValuesView[App]:
        return self._apps.values()

    def find_by_id(self, app_id: str) -> App:
        app = self._apps.get(app_id.lower())
        if app is None:
            raise AppNotFoundError(app_id)
        return app

    @property
    def version(self) -> str:
        return self._raw.version or "dev"

    def fetch_app(
        self,
        app_id: str,
        dest: Path,
        model_asset: ModelAsset | None = None,
    ) -> Path:
        """Find app by ID and download + extract it. Returns the extraction path."""
        app = self.find_by_id(app_id)

        if not is_app_supported(app):
            print("Warning: This app may not be supported on the current device.")

        return app.fetch(dest, model_asset=model_asset)


def _make_app(info: AppInfo) -> App:
    """Return the appropriate App subclass based on the app's languages."""
    if AppLanguage.PYTHON in info.languages:
        from qai_hub_apps.registry.python_app import PythonApp

        return PythonApp(info)
    return App(info)
