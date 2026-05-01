// ---------------------------------------------------------------------
// Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
// SPDX-License-Identifier: BSD-3-Clause
// ---------------------------------------------------------------------
package com.qualcomm.tflite;

import android.content.Context;
import android.util.Log;

import com.google.ai.edge.litert.Accelerator;
import com.google.ai.edge.litert.CompiledModel;

import java.io.IOException;

public class TFLiteHelpers {
    private static final String TAG = "TFLiteHelpers";

    /**
     * Creates a LiteRT CompiledModel by trying accelerators in the provided priority order.
     *
     * TO REPLICATE AN AI HUB JOB:
     * Modify the acceleratorPriorityOrder in AIHubDefaults.java or the logic here
     * to match the runtime configuration used in your AI Hub job.
     */
    public static CompiledModel createModel(Context context,
                                            String modelPath,
                                            Accelerator[] acceleratorPriorityOrder) throws IOException {
        for (Accelerator accelerator : acceleratorPriorityOrder) {
            try {
                // If you need to specify a specific delegate (e.g. QNN), 
                // LiteRT handles this via the Accelerator enum.
                CompiledModel model = CompiledModel.create(
                        context.getAssets(),
                        modelPath,
                        new CompiledModel.Options(accelerator)
                );
                Log.i(TAG, "Successfully created CompiledModel with " + accelerator.name());
                return model;
            } catch (Exception e) {
                Log.w(TAG, "Failed to create CompiledModel with " + accelerator.name() + ": " + e.getMessage());
            }
        }
        throw new RuntimeException("Unable to create a CompiledModel for any accelerator.");
    }
}
