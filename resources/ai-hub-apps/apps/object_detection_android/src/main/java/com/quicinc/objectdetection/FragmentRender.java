// ---------------------------------------------------------------------
// Copyright (c) 2025 Qualcomm Technologies, Inc. and/or its subsidiaries.
// SPDX-License-Identifier: BSD-3-Clause
// ---------------------------------------------------------------------
package com.quicinc.objectdetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Matrix;
import android.util.Size;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

/**
 * FragmentRender draws the final prediction image and overlays debugging text.
 */

public class FragmentRender extends View {
    private final ReentrantLock mLock = new ReentrantLock();
    private Bitmap mBitmap = null;
    private Size mCameraSize = null;
    private ArrayList<RectangleBox> boxlist = new ArrayList<>();
    private int mDisplayRotation = 0;
    private final Rect mTargetRect = new Rect();
    private float fps;
    private long inferTime = 0;
    private long preprocessTime = 0;
    private long postprocessTime = 0;
    private Matrix mTransform = new Matrix();
    private final Paint mBorderColor = new Paint();
    private final Paint mTextColor = new Paint();
    private Paint mFramePaint = new Paint();
    private Paint mTextPaint = new Paint();
    private Paint mLabelFramePaint = new Paint();

    private String mSelectedCountry = "";
    private Paint mRestrictionPaint = new Paint();

    public static @ColorInt int labelColor(int label, int alpha) {
        // Generic colors that do not correspond to a dataset
        final int[] baseColors = new int[]{
                0xFFF44336, // Red
                0xFFE91E63, // Pink
                0xFF9C27B0, // Purple
                0xFF673AB7, // Deep Purple
                0xFF3F51B5, // Indigo
                0xFF2196F3, // Blue
                0xFF03A9F4, // Light Blue
                0xFF00BCD4, // Cyan
                0xFF009688, // Teal
                0xFF4CAF50, // Green
                0xFF8BC34A, // Light Green
                0xFFCDDC39, // Lime
                0xFFFFEB3B, // Yellow
                0xFFFFC107, // Amber
                0xFFFF9800, // Orange
                0xFFFF5722, // Deep Orange
                0xFF795548, // Brown
                0xFF9E9E9E, // Gray
                0xFF607D8B  // Blue Gray
        };

        int index = Math.abs(label % baseColors.length);
        int color = baseColors[index];
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public FragmentRender(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mBorderColor.setColor(Color.MAGENTA);
        mBorderColor.setStyle(Paint.Style.STROKE);
        mBorderColor.setStrokeWidth(6);

        mTextColor.setColor(Color.WHITE);
        mTextColor.setTypeface(Typeface.DEFAULT_BOLD);
        mTextColor.setStyle(Paint.Style.FILL);
        mTextColor.setTextSize(50);

        mRestrictionPaint.setColor(Color.WHITE);
        mRestrictionPaint.setTypeface(Typeface.DEFAULT);
        mRestrictionPaint.setStyle(Paint.Style.FILL);
        mRestrictionPaint.setTextSize(28);
    }

    public void setCoordsList(ArrayList<RectangleBox> t_boxlist) {
        mLock.lock();
        postInvalidate();

        if (boxlist==null)
        {
            mLock.unlock();
            return;
        }
        boxlist.clear();
        for(int j=0;j<t_boxlist.size();j++) {
            boxlist.add(t_boxlist.get(j));
        }
        mLock.unlock();
        postInvalidate();
    }

    public void render(Bitmap image, Size cameraSize, float fps, long inferTime, long preprocessTime, long postprocessTime, int displayRotation, String country)
    {
        this.mBitmap = image;
        this.mCameraSize = cameraSize;
        this.fps = fps;
        this.inferTime = inferTime;
        this.preprocessTime = preprocessTime;
        this.postprocessTime = postprocessTime;
        this.mDisplayRotation = displayRotation;
        this.mSelectedCountry = country;
        postInvalidate();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        mLock.lock();

        if (mBitmap != null && mCameraSize != null) {
            int insetHeight, insetWidth;

            float canvasRatio = (float) getWidth() / (float) getHeight();
            float bitmapRatio = (float) mBitmap.getWidth() / mBitmap.getHeight();

            if (canvasRatio > bitmapRatio) {
                insetHeight = getHeight();
                insetWidth = (int) ((float) getHeight() * bitmapRatio);
            } else {
                insetWidth = getWidth();
                insetHeight = (int) ((float) getWidth() / bitmapRatio);
            }

            int offsetWidth = (getWidth() - insetWidth) / 2;
            int offsetHeight = (getHeight() - insetHeight) / 2;

            float scaleX;
            float scaleY;
            if (mDisplayRotation == 0 || mDisplayRotation == 2) {
                scaleX = (float)mCameraSize.getHeight() / (float)getWidth();
                scaleY = (float)mCameraSize.getWidth() / (float)getHeight();
            } else {
                scaleX = (float)mCameraSize.getWidth() / (float)getWidth();
                scaleY = (float)mCameraSize.getHeight() / (float)getHeight();
            }

            if (scaleX < scaleY) {
                scaleX /= scaleY;
                scaleY = 1.0f;
            } else {
                scaleY /= scaleX;
                scaleX = 1.0f;
            }

            float tx = (float)getWidth() / 2.0f;
            float ty = (float)getHeight() / 2.0f;

            mTransform.reset();
            switch (mDisplayRotation) {
                case 0:
                    mTransform.preTranslate(tx, ty);
                    mTransform.preScale(scaleX, scaleY);
                    mTransform.preTranslate(-tx, -ty);
                    break;
                case 1:
                    mTransform.preRotate(-90, tx, ty);
                    mTransform.preTranslate(tx, ty);
                    mTransform.preScale(
                            scaleY * ty / tx,
                            scaleX * tx / ty);
                    mTransform.preTranslate(-tx, -ty);
                    break;
                case 3:
                    mTransform.preRotate(90, tx, ty);
                    mTransform.preTranslate(tx, ty);
                    mTransform.preScale(
                            scaleY * ty / tx,
                            scaleX * tx / ty);
                    mTransform.preTranslate(-tx, -ty);
                    break;
                default:
                    break;
            }

            mTargetRect.left = offsetWidth;
            mTargetRect.top = offsetHeight;
            mTargetRect.right = offsetWidth + insetWidth;
            mTargetRect.bottom = offsetHeight + insetHeight;

            canvas.save();
            // We use the transform only for the bitmap to handle screen rotation/fit
            canvas.concat(mTransform);
            canvas.drawBitmap(mBitmap, null, mTargetRect, null);
            canvas.restore();

            // Draw header with selected country
            String header = "Destination: " + mSelectedCountry;
            mTextPaint.setTextSize(45);
            mTextPaint.setColor(Color.YELLOW);
            canvas.drawText(header, 50, 100, mTextPaint);

            // Draw boxes in screen space (not transformed) to avoid double-scaling
            for(int j=0;j<boxlist.size();j++) {
                RectangleBox rbox = boxlist.get(j);

                // Map from bitmap pixels [0, 640] to screen pixels using the target rect
                float left = mTargetRect.left + (rbox.left * mTargetRect.width() / mBitmap.getWidth());
                float top = mTargetRect.top + (rbox.top * mTargetRect.height() / mBitmap.getHeight());
                float right = mTargetRect.left + (rbox.right * mTargetRect.width() / mBitmap.getWidth());
                float bottom = mTargetRect.top + (rbox.bottom * mTargetRect.height() / mBitmap.getHeight());

                int alpha = (int)(255 * rbox.confidence);
                int color = labelColor(rbox.classIdx, alpha);

                mFramePaint.setColor(color);
                mFramePaint.setStyle(Paint.Style.STROKE);
                mFramePaint.setStrokeWidth(8);

                canvas.drawRect(left, top, right, bottom, mFramePaint);

                int white = Color.argb(255, 255, 255, 255);
                mTextPaint.setColor(white);
                mTextPaint.setTextSize(35);

                float buf = 8.0f;
                String mainLabel = rbox.label.toUpperCase();
                float textWidth = mTextPaint.measureText(mainLabel);
                
                mLabelFramePaint.setColor(color);
                mLabelFramePaint.setStyle(Paint.Style.FILL);

                // Draw label background
                canvas.drawRect(left, top - 45f, left + textWidth + 2*buf, top, mLabelFramePaint);
                // Draw label text
                canvas.drawText(mainLabel, left + buf, top - 10f, mTextPaint);

                // Draw restriction text if available
                if (rbox.travelInfo != null) {
                    mRestrictionPaint.setColor(Color.WHITE);
                    mRestrictionPaint.setShadowLayer(4, 0, 0, Color.BLACK);
                    
                    String rest = rbox.travelInfo.message;
                    
                    // Choose color based on level
                    int bgColor = 0xCC4CAF50; // Green
                    if (rbox.travelInfo.level == RestrictionManager.Level.CAUTION) bgColor = 0xCCFF9800; // Orange
                    else if (rbox.travelInfo.level == RestrictionManager.Level.DANGER) bgColor = 0xCCF44336; // Red

                    // Intelligent Line Wrapping - Optimized to reduce allocations
                    float maxWidth = Math.max(300, getWidth() * 0.45f);
                    String[] words = rest.split(" ");
                    StringBuilder lineBuilder = new StringBuilder();
                    float currentY = bottom + 35f;
                    float maxLineWidth = 0;
                    int lineCount = 0;

                    // First pass: calculate dimensions
                    for (String word : words) {
                        if (mRestrictionPaint.measureText(lineBuilder + word) < maxWidth) {
                            lineBuilder.append(word).append(" ");
                        } else {
                            maxLineWidth = Math.max(maxLineWidth, mRestrictionPaint.measureText(lineBuilder.toString()));
                            lineBuilder = new StringBuilder(word).append(" ");
                            lineCount++;
                        }
                    }
                    lineCount++;
                    maxLineWidth = Math.max(maxLineWidth, mRestrictionPaint.measureText(lineBuilder.toString()));

                    mLabelFramePaint.setColor(bgColor);
                    canvas.drawRect(left, bottom, left + maxLineWidth + 16f, bottom + (lineCount * 38f) + 10f, mLabelFramePaint);
                    
                    // Second pass: draw
                    lineBuilder = new StringBuilder();
                    for (String word : words) {
                        if (mRestrictionPaint.measureText(lineBuilder + word) < maxWidth) {
                            lineBuilder.append(word).append(" ");
                        } else {
                            canvas.drawText(lineBuilder.toString(), left + 8f, currentY, mRestrictionPaint);
                            lineBuilder = new StringBuilder(word).append(" ");
                            currentY += 38f;
                        }
                    }
                    canvas.drawText(lineBuilder.toString(), left + 8f, currentY, mRestrictionPaint);
                }
            }
        }
        mLock.unlock();
    }
}
