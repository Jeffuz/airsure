// ---------------------------------------------------------------------
// Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
// SPDX-License-Identifier: BSD-3-Clause
// ---------------------------------------------------------------------
package com.qualcomm.qti.objectdetection;

import android.content.Context;
import android.graphics.Bitmap;

import com.qualcomm.tflite.TFLiteHelpers;
import com.google.ai.edge.litert.Accelerator;
import com.google.ai.edge.litert.CompiledModel;
import com.google.ai.edge.litert.TensorBuffer;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectDetection implements AutoCloseable {
    private final CompiledModel liteRTModel;
    private final List<TensorBuffer> inputBuffers;
    private final List<TensorBuffer> outputBuffers;
    private final List<String> labelList;
    private final int numClasses;
    private final int numAnchors;
    private long preprocessingTime;
    private long inferenceTime;
    private long postprocessingTime;

    // Re-usable memory
    private final ByteBuffer inputByteBuffer;
    private final float[] inputFloatArray;
    private final Mat inputMatAbgr;
    private final Mat inputMatRgb;
    private final float[][] updatedBoxes;
    private final float[] scores;
    private final int[] classIdx;
    private final NMS nmsProcessor;

    private final static float INVALID_ANCHOR = -10000.0f;
    private final float scoreThreshold = 0.50f;
    private final int maxDetections = 10;

    public ObjectDetection(Context context,
                           String modelPath,
                           String labelsPath,
                           Accelerator[] acceleratorPriorityOrder) throws IOException, NoSuchAlgorithmException {
        new OpenCVNativeLoader().init();

        try (BufferedReader labelsFile = new BufferedReader(new InputStreamReader(context.getAssets().open(labelsPath)))) {
            labelList = labelsFile.lines().collect(Collectors.toCollection(ArrayList::new));
        }

        liteRTModel = TFLiteHelpers.createModel(context, modelPath, acceleratorPriorityOrder);

        try {
            inputBuffers = liteRTModel.createInputBuffers();
            outputBuffers = liteRTModel.createOutputBuffers();

            android.util.Log.i("ObjectDetection", "Number of output buffers: " + outputBuffers.size());
            for (int i = 0; i < outputBuffers.size(); i++) {
                android.util.Log.i("ObjectDetection", "Output buffer " + i + " size: " + outputBuffers.get(i).readFloat().length);
            }

            float[] sampleData = outputBuffers.get(0).readFloat();
            int totalOutputElements = sampleData.length;
            numAnchors = 8400; // YOLO default for 640x640

            if (outputBuffers.size() > 1) {
                // Case: Boxes and Scores are in separate buffers
                numClasses = outputBuffers.get(1).readFloat().length / numAnchors;
            } else {
                // Case: Combined buffer [1, 4 + numClasses, 8400]
                int numRows = totalOutputElements / numAnchors;
                numClasses = numRows - 4;
            }
            android.util.Log.i("ObjectDetection", "Detected " + numClasses + " classes and " + numAnchors + " anchors.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize LiteRT buffers: " + e.getMessage());
        }

        inputByteBuffer = ByteBuffer.allocateDirect(640 * 640 * 3 * 4);
        inputByteBuffer.order(ByteOrder.nativeOrder());
        inputFloatArray = new float[640 * 640 * 3];
        inputMatAbgr = new Mat(640, 640, CvType.CV_8UC4);
        inputMatRgb = new Mat(640, 640, CvType.CV_8UC3);

        updatedBoxes = new float[numAnchors][4];
        scores = new float[numAnchors];
        classIdx = new int[numAnchors];
        nmsProcessor = new NMS();
    }

    public int getInputWidth() { return 640; }
    public int getInputHeight() { return 640; }

    @Override
    public void close() {
        for (TensorBuffer buffer : inputBuffers) buffer.close();
        for (TensorBuffer buffer : outputBuffers) buffer.close();
        liteRTModel.close();
    }

    public long getLastPreprocessingTime() { return preprocessingTime; }
    public long getLastInferenceTime() { return inferenceTime; }
    public long getLastPostprocessingTime() { return postprocessingTime; }

    public void predict(Bitmap image, int sensorOrientation, ArrayList<RectangleBox> BBlist) {
        long preStartTime = System.nanoTime();

        Utils.bitmapToMat(image, inputMatAbgr);
        Imgproc.cvtColor(inputMatAbgr, inputMatRgb, Imgproc.COLOR_BGRA2RGB);

        Mat correctRotInputImageRgb = new Mat();
        switch (sensorOrientation) {
            case 0: Core.rotate(inputMatRgb, correctRotInputImageRgb, Core.ROTATE_90_COUNTERCLOCKWISE); break;
            case 90: correctRotInputImageRgb = inputMatRgb; break;
            case 180: Core.rotate(inputMatRgb, correctRotInputImageRgb, Core.ROTATE_90_CLOCKWISE); break;
            case 270: Core.rotate(inputMatRgb, correctRotInputImageRgb, Core.ROTATE_180); break;
            default: break;
        }

        int inputHeight = 640;
        int inputWidth = 640;
        int srcWidth = correctRotInputImageRgb.width();
        int srcHeight = correctRotInputImageRgb.height();
        int origWidth = image.getWidth();
        int origHeight = image.getHeight();

        float scale = Math.min((float) inputWidth / srcWidth, (float) inputHeight / srcHeight);
        int newWidth = Math.round(srcWidth * scale);
        int newHeight = Math.round(srcHeight * scale);

        Mat resizedImage = new Mat();
        Imgproc.resize(correctRotInputImageRgb, resizedImage, new org.opencv.core.Size(newWidth, newHeight), 0, 0, Imgproc.INTER_CUBIC);

        Mat scaledImage = new Mat(inputHeight, inputWidth, CvType.CV_8UC3, new org.opencv.core.Scalar(0, 0, 0));
        int dx = (inputWidth - newWidth) / 2;
        int dy = (inputHeight - newHeight) / 2;
        resizedImage.copyTo(scaledImage.submat(dy, dy + newHeight, dx, dx + newWidth));
        scaledImage.convertTo(scaledImage, CvType.CV_32FC3, 1 / 255f);

        final float padX = dx;
        final float padY = dy;
        final float ratio = scale;

        scaledImage.get(0, 0, inputFloatArray);

        long inferenceStartTime = System.nanoTime();
        preprocessingTime = inferenceStartTime - preStartTime;
        long postStartTime = System.nanoTime();

        try {
            inputBuffers.get(0).writeFloat(inputFloatArray);
            liteRTModel.run(inputBuffers, outputBuffers);
            
            float[] boxesData = outputBuffers.get(0).readFloat();
            float[] scoresData = outputBuffers.size() > 1 ? outputBuffers.get(1).readFloat() : boxesData;

            for (int i = 0; i < numAnchors; i++) {
                float maxScore = -1000f;
                int maxClass = -1;

                for (int c = 0; c < numClasses; c++) {
                    float score;
                    if (outputBuffers.size() > 1) {
                        // Channels Last: [1, 8400, numClasses]
                        score = scoresData[i * numClasses + c];
                    } else {
                        // Combined Planar: [1, 4 + 80, 8400]
                        score = scoresData[(c + 4) * numAnchors + i];
                    }
                    
                    if (score > maxScore) {
                        maxScore = score;
                        maxClass = c;
                    }
                }

                if (maxClass == -1) {
                    this.scores[i] = INVALID_ANCHOR;
                    continue;
                }

                // AI Hub YOLO models usually have the sigmoid already applied in the TFLite graph.
                // If the values are > 1 or < 0, then they are raw logits.
                float confidence = maxScore;
                if (maxScore > 1.0f || maxScore < 0.0f) {
                    // Only apply sigmoid if it looks like a raw logit (not 0, which is likely background)
                    if (maxScore == 0.0f) {
                        confidence = 0.0f;
                    } else {
                        confidence = (float) (1.0 / (1.0 + Math.exp(-maxScore)));
                    }
                }

                if (confidence >= scoreThreshold) {
                    float x0_raw, y0_raw, x1_raw, y1_raw;
                    if (outputBuffers.size() > 1) {
                        // Channels Last Boxes: [1, 8400, 4]
                        float b0 = boxesData[i * 4];
                        float b1 = boxesData[i * 4 + 1];
                        float b2 = boxesData[i * 4 + 2];
                        float b3 = boxesData[i * 4 + 3];

                        // Handle both XYXY and CXCYWH
                        if (b2 < b0 || b3 < b1) { // Likely CXCYWH
                            x0_raw = b0 - b2 / 2f;
                            y0_raw = b1 - b3 / 2f;
                            x1_raw = b0 + b2 / 2f;
                            y1_raw = b1 + b3 / 2f;
                        } else { // Likely XYXY
                            x0_raw = b0;
                            y0_raw = b1;
                            x1_raw = b2;
                            y1_raw = b3;
                        }
                    } else {
                        // Combined Planar: [1, 4 + 80, 8400]
                        float cx = boxesData[i];
                        float cy = boxesData[numAnchors + i];
                        float w = boxesData[2 * numAnchors + i];
                        float h = boxesData[3 * numAnchors + i];
                        x0_raw = cx - w / 2f;
                        y0_raw = cy - h / 2f;
                        x1_raw = cx + w / 2f;
                        y1_raw = cy + h / 2f;
                    }

                    // Scale to pixels if normalized
                    if (x1_raw <= 1.01f && y1_raw <= 1.01f && x1_raw > 0) {
                        x0_raw *= 640f;
                        y0_raw *= 640f;
                        x1_raw *= 640f;
                        y1_raw *= 640f;
                    }

                    float x0 = (x0_raw - padX) / ratio;
                    float y0 = (y0_raw - padY) / ratio;
                    float x1 = (x1_raw - padX) / ratio;
                    float y1 = (y1_raw - padY) / ratio;

                    this.scores[i] = confidence;
                    this.classIdx[i] = maxClass;

                    switch (sensorOrientation) {
                        case 0:
                            updatedBoxes[i][0] = srcHeight - y1; updatedBoxes[i][1] = x0;
                            updatedBoxes[i][2] = srcHeight - y0; updatedBoxes[i][3] = x1;
                            break;
                        case 90:
                            updatedBoxes[i][0] = x0; updatedBoxes[i][1] = y0;
                            updatedBoxes[i][2] = x1; updatedBoxes[i][3] = y1;
                            break;
                        case 180:
                            updatedBoxes[i][0] = y0; updatedBoxes[i][1] = origWidth - x1;
                            updatedBoxes[i][2] = y1; updatedBoxes[i][3] = origWidth - x0;
                            break;
                        case 270:
                            updatedBoxes[i][0] = origWidth - x1; updatedBoxes[i][1] = origHeight - y1;
                            updatedBoxes[i][2] = origWidth - x0; updatedBoxes[i][3] = origHeight - y0;
                            break;
                        default: break;
                    }
                } else {
                    this.scores[i] = INVALID_ANCHOR;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ObjectDetection", "Inference failed: " + e.getMessage());
        }

        inferenceTime = System.nanoTime() - postStartTime;
        
        List<Integer> sortedIndices = new ArrayList<>();
        for (int i = 0; i < numAnchors; i++) {
            if (this.scores[i] != INVALID_ANCHOR) sortedIndices.add(i);
        }
        sortedIndices.sort((a, b) -> Float.compare(this.scores[b], this.scores[a]));
        
        int[] sortedIndicesArray = sortedIndices.stream().mapToInt(Integer::intValue).toArray();
        int[] result_indices = nmsProcessor.nmsScoreFilter(updatedBoxes, this.scores, sortedIndicesArray, maxDetections, 0.45f);

        for (int index : result_indices) {
            float[] temp_boxes = updatedBoxes[index];
            RectangleBox tempbox = new RectangleBox();
            tempbox.left = temp_boxes[0];
            tempbox.top = temp_boxes[1];
            tempbox.right = temp_boxes[2];
            tempbox.bottom = temp_boxes[3];
            tempbox.confidence = this.scores[index];
            tempbox.classIdx = this.classIdx[index];
            tempbox.label = labelList.get(this.classIdx[index] % labelList.size());
            BBlist.add(tempbox);
        }
        postprocessingTime = System.nanoTime() - postStartTime - inferenceTime;
    }

    public class NMS {
        private float computeOverlapAreaRate(float[] anchor1, float[] anchor2){
            float xx1 = Math.max(anchor1[0], anchor2[0]);
            float yy1 = Math.max(anchor1[1], anchor2[1]);
            float xx2 = Math.min(anchor1[2], anchor2[2]);
            float yy2 = Math.min(anchor1[3], anchor2[3]);
            float w = xx2 - xx1 + 1;
            float h = yy2 - yy1 + 1;
            if(w<0||h<0) return 0;
            float inter = w * h;
            float area1 = (anchor1[2] - anchor1[0] + 1)*(anchor1[3] - anchor1[1] + 1);
            float area2 = (anchor2[2] - anchor2[0] + 1)*(anchor2[3] - anchor2[1] + 1);
            return inter / (area1 + area2 - inter);
        }

        public int[] nmsScoreFilter(float[][] anchors, float[] score, int[] sortedIndices, int topN, float thresh){
            List<Integer> keptIndices = new ArrayList<>();
            for(int i : sortedIndices){
                boolean keep = true;
                for(int kept : keptIndices) {
                    if (computeOverlapAreaRate(anchors[i], anchors[kept]) > thresh) {
                        keep = false;
                        break;
                    }
                }
                if (keep) {
                    keptIndices.add(i);
                    if (keptIndices.size() >= topN) break;
                }
            }
            return keptIndices.stream().mapToInt(Integer::intValue).toArray();
        }
    }
}
