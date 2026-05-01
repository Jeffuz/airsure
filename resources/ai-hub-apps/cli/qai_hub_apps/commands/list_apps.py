# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
from __future__ import annotations

from qai_hub_apps.registry import Registry


def run_list(registry: Registry) -> None:
    apps = registry.apps

    print(f"Qualcomm\u00ae AI Hub Apps  ({len(apps)} apps)\n")
    id_w, name_w = 38, 38
    header = f"{'ID':<{id_w}}  {'Name':<{name_w}}"
    print(header)
    print("\u2500" * len(header))
    for app in apps:
        app_id = (app.id or app.name) or ""
        print(f"{app_id:<{id_w}}  {app.name:<{name_w}}")


def run_info(app_id: str, registry: Registry) -> None:
    app = registry.find_by_id(app_id)
    print(app)
