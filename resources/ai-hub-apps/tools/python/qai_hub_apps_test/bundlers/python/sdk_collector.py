# ---------------------------------------------------------------------
# Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause
# ---------------------------------------------------------------------
"""Collect qai_hub_apps_utils source files needed by an app."""

from __future__ import annotations

import ast
import warnings
from pathlib import Path

from qai_hub_apps_test.bundlers.python.sdk_resolver import _SDK_PACKAGE


def collect_sdk_imports_from_file(py_file: Path) -> set[str]:
    """Return set of qai_hub_apps_utils module dotted names imported by py_file."""
    try:
        tree = ast.parse(py_file.read_text(encoding="utf-8"), filename=str(py_file))
    except SyntaxError as e:
        warnings.warn(f"Could not parse {py_file}: {e}", stacklevel=2)
        return set()

    modules: set[str] = set()
    for node in ast.walk(tree):
        if isinstance(node, ast.Import):
            for alias in node.names:
                name = alias.name
                if name == _SDK_PACKAGE or name.startswith(_SDK_PACKAGE + "."):
                    modules.add(name)
        elif isinstance(node, ast.ImportFrom):
            mod = node.module or ""
            if mod == _SDK_PACKAGE or mod.startswith(_SDK_PACKAGE + "."):
                modules.add(mod)
    return modules


def module_to_sdk_file(module: str, sdk_parent: Path) -> Path | None:
    """
    Resolve a dotted module name to its .py file path under sdk_parent.

    Tries two resolution strategies in order:
    1. ``<sdk_parent>/<parts>.py`` — regular module file.
    2. ``<sdk_parent>/<parts>/__init__.py`` — package init file.
    """
    parts = module.split(".")
    rel = Path(*parts)
    as_module = sdk_parent / rel.with_suffix(".py")
    as_pkg = sdk_parent / rel / "__init__.py"
    if as_module.exists():
        return as_module
    if as_pkg.exists():
        return as_pkg
    return None


def collect_all_sdk_files(app_root: Path, sdk_parent: Path) -> set[Path]:
    """
    Recursively find all SDK .py files needed by app_root source files.
    Also walks SDK module files themselves for transitive imports.
    """
    py_files = list(app_root.rglob("*.py"))
    visited_modules: set[str] = set()
    needed_files: set[Path] = set()
    stack: list[str] = []

    # Seed from app source files
    for py_file in py_files:
        for mod in collect_sdk_imports_from_file(py_file):
            if mod not in visited_modules:
                visited_modules.add(mod)
                stack.append(mod)

    # DFS over SDK module imports
    while stack:
        mod = stack.pop()
        sdk_file = module_to_sdk_file(mod, sdk_parent)
        if sdk_file is None:
            warnings.warn(
                f"Imported SDK module '{mod}' could not be resolved to a file "
                f"under '{sdk_parent}'. It will be skipped.",
                stacklevel=2,
            )
            continue
        if sdk_file in needed_files:
            continue
        needed_files.add(sdk_file)
        for transitive_mod in collect_sdk_imports_from_file(sdk_file):
            if transitive_mod not in visited_modules:
                visited_modules.add(transitive_mod)
                stack.append(transitive_mod)

    return needed_files


def init_files_for_sdk_file(sdk_file: Path, sdk_parent: Path) -> list[Path]:
    """Return all __init__.py files from sdk_parent down to sdk_file's package."""
    inits: list[Path] = []
    rel = sdk_file.relative_to(sdk_parent)
    current = sdk_parent
    for part in rel.parts[:-1]:  # exclude the file itself
        current = current / part
        init = current / "__init__.py"
        if init.exists():
            inits.append(init)
    return inits
