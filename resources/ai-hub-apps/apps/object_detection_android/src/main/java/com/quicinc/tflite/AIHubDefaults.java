// ---------------------------------------------------------------------
// Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
// SPDX-License-Identifier: BSD-3-Clause
// ---------------------------------------------------------------------
package com.quicinc.tflite;

import com.google.ai.edge.litert.Accelerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AIHubDefaults {
    // Accelerators enabled to replicate AI Hub's defaults on Qualcomm devices.
    public static final Set<Accelerator> enabledAccelerators = new HashSet<>(Arrays.asList(
            Accelerator.NPU,
            Accelerator.GPU
    ));

    // Number of threads AI Hub uses by default for layers running on CPU.
    public static final int numCPUThreads = Runtime.getRuntime().availableProcessors() / 2;

    // The default accelerator priority order for AI Hub.
    public static final Accelerator[] acceleratorPriorityOrder = new Accelerator[] {
            Accelerator.NPU,
            Accelerator.GPU,
            Accelerator.CPU
    };
}
