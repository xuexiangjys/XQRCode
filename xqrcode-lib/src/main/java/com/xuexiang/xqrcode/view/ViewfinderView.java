/*
 * Copyright (C) 2018 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xuexiang.xqrcode.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.xuexiang.xqrcode.R;
import com.xuexiang.xqrcode.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;

/**
 * <pre>
 *     desc   : 自定义组件实现,扫描功能
 *     author : xuexiang
 *     time   : 2018/5/5 上午12:48
 * </pre>
 */
public final class ViewfinderView extends View {

    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;

    private final Paint mPaint;
    private Bitmap mResultBitmap;
    private final int mMaskColor;
    private final int mResultColor;
    private final int mResultPointColor;
    private Collection<ResultPoint> mPossibleResultPoints;
    private Collection<ResultPoint> mLastPossibleResultPoints;

    public ViewfinderView(Context context) {
        this(context, null);
    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);

    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        Resources resources = getResources();
        mMaskColor = resources.getColor(R.color.viewfinder_mask);
        mResultColor = resources.getColor(R.color.result_view);
        mResultPointColor = resources.getColor(R.color.possible_result_points);
        mPossibleResultPoints = new HashSet<>(5);

        scanLight = BitmapFactory.decodeResource(resources,
                R.drawable.xqrcode_ic_scan_light);

        initInnerRect(context, attrs);
    }

    /**
     * 初始化内部框的大小
     *
     * @param context
     * @param attrs
     */
    private void initInnerRect(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView);

        // 扫描框距离顶部
        float innerMarginTop = ta.getDimension(R.styleable.ViewfinderView_inner_marginTop, -1);
        if (innerMarginTop != -1) {
            CameraManager.FRAME_MARGIN_TOP = (int) innerMarginTop;
        }

        // 扫描框的宽度
        CameraManager.FRAME_WIDTH = (int) ta.getDimension(R.styleable.ViewfinderView_inner_width, getDefaultScanSize(getContext()));

        // 扫描框的高度
        CameraManager.FRAME_HEIGHT = (int) ta.getDimension(R.styleable.ViewfinderView_inner_height, getDefaultScanSize(getContext()));

        // 扫描框边角颜色
        innercornercolor = ta.getColor(R.styleable.ViewfinderView_inner_corner_color, Color.parseColor("#45DDDD"));
        // 扫描框边角长度
        innercornerlength = (int) ta.getDimension(R.styleable.ViewfinderView_inner_corner_length, 65);
        // 扫描框边角宽度
        innercornerwidth = (int) ta.getDimension(R.styleable.ViewfinderView_inner_corner_width, 15);

        // 扫描控件
        scanLight = BitmapFactory.decodeResource(getResources(), ta.getResourceId(R.styleable.ViewfinderView_inner_scan_bitmap, R.drawable.xqrcode_ic_scan_light));
        // 扫描速度
        SCAN_VELOCITY = ta.getInt(R.styleable.ViewfinderView_inner_scan_speed, 5);

        isCircle = ta.getBoolean(R.styleable.ViewfinderView_inner_scan_isCircle, true);

        ta.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        mPaint.setColor(mResultBitmap != null ? mResultColor : mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        if (mResultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            mPaint.setAlpha(OPAQUE);
            canvas.drawBitmap(mResultBitmap, frame.left, frame.top, mPaint);
        } else {

            drawFrameBounds(canvas, frame);

            drawScanLight(canvas, frame);

            Collection<ResultPoint> currentPossible = mPossibleResultPoints;
            Collection<ResultPoint> currentLast = mLastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                mLastPossibleResultPoints = null;
            } else {
                mPossibleResultPoints = new HashSet<ResultPoint>(5);
                mLastPossibleResultPoints = currentPossible;
                mPaint.setAlpha(OPAQUE);
                mPaint.setColor(mResultPointColor);

                if (isCircle) {
                    for (ResultPoint point : currentPossible) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, mPaint);
                    }
                }
            }
            if (currentLast != null) {
                mPaint.setAlpha(OPAQUE / 2);
                mPaint.setColor(mResultPointColor);

                if (isCircle) {
                    for (ResultPoint point : currentLast) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, mPaint);
                    }
                }
            }

            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    // 扫描线移动的y
    private int scanLineTop;
    // 扫描线移动速度
    private int SCAN_VELOCITY;
    // 扫描线
    private Bitmap scanLight;
    // 是否展示小圆点
    private boolean isCircle;

    /**
     * 绘制移动扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawScanLight(Canvas canvas, Rect frame) {

        if (scanLineTop == 0) {
            scanLineTop = frame.top;
        }

        if (scanLineTop >= frame.bottom - 30) {
            scanLineTop = frame.top;
        } else {
            scanLineTop += SCAN_VELOCITY;
        }
        Rect scanRect = new Rect(frame.left, scanLineTop, frame.right,
                scanLineTop + 30);
        canvas.drawBitmap(scanLight, null, scanRect, mPaint);
    }


    // 扫描框边角颜色
    private int innercornercolor;
    // 扫描框边角长度
    private int innercornerlength;
    // 扫描框边角宽度
    private int innercornerwidth;

    /**
     * 绘制取景框边框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {

        mPaint.setColor(innercornercolor);
        mPaint.setStyle(Paint.Style.FILL);

        int corWidth = innercornerwidth;
        int corLength = innercornerlength;

        // 左上角
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth, frame.top
                + corLength, mPaint);
        canvas.drawRect(frame.left, frame.top, frame.left
                + corLength, frame.top + corWidth, mPaint);
        // 右上角
        canvas.drawRect(frame.right - corWidth, frame.top, frame.right,
                frame.top + corLength, mPaint);
        canvas.drawRect(frame.right - corLength, frame.top,
                frame.right, frame.top + corWidth, mPaint);
        // 左下角
        canvas.drawRect(frame.left, frame.bottom - corLength,
                frame.left + corWidth, frame.bottom, mPaint);
        canvas.drawRect(frame.left, frame.bottom - corWidth, frame.left
                + corLength, frame.bottom, mPaint);
        // 右下角
        canvas.drawRect(frame.right - corWidth, frame.bottom - corLength,
                frame.right, frame.bottom, mPaint);
        canvas.drawRect(frame.right - corLength, frame.bottom - corWidth,
                frame.right, frame.bottom, mPaint);
    }


    public void drawViewfinder() {
        mResultBitmap = null;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        mPossibleResultPoints.add(point);
    }


    /**
     * 获取默认扫描的尺寸
     * @return
     */
    public int getDefaultScanSize(Context context) {
        return Math.min(getScreenWidth(context), getScreenHeight(context)) * 3 / 4;
    }

    /**
     * 得到设备屏幕的宽度
     */
    private static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 得到设备屏幕的高度
     */
    private static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

}
