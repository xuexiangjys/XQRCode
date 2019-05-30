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
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.xuexiang.xqrcode.R;
import com.xuexiang.xqrcode.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;

/**
 * 自定义组件实现,扫描功能
 *
 * @author xuexiang
 * @since 2019/5/17 17:54
 */
public final class ViewfinderView extends View {

    private static final int DEFAULT_ANIMATION_INTERVAL = 25;
    private static final int OPAQUE = 0xFF;

    private final Paint mPaint;
    private Bitmap mResultBitmap;
    private int mMaskColor;
    private int mResultColor;
    private int mResultPointColor;
    private Collection<ResultPoint> mPossibleResultPoints;
    private Collection<ResultPoint> mLastPossibleResultPoints;

    public ViewfinderView(Context context) {
        this(context, null);
    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ViewfinderViewStyle);
    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPossibleResultPoints = new HashSet<>(5);
        initInnerRect(context, attrs, defStyleAttr);
    }

    /**
     * 初始化内部框的大小
     *
     * @param context
     * @param attrs
     */
    private void initInnerRect(Context context, AttributeSet attrs, int defStyleAttr) {
        Resources resources = getResources();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView, defStyleAttr, 0);

        // 扫描框距离顶部
        int innerMarginTop = ta.getDimensionPixelSize(R.styleable.ViewfinderView_inner_marginTop, -1);
        if (innerMarginTop != -1) {
            CameraManager.FRAME_MARGIN_TOP = innerMarginTop;
        }

        // 扫描框的宽度
        CameraManager.FRAME_WIDTH = ta.getDimensionPixelSize(R.styleable.ViewfinderView_inner_width, getDefaultScanSize(getContext()));
        // 扫描框的高度
        CameraManager.FRAME_HEIGHT = ta.getDimensionPixelSize(R.styleable.ViewfinderView_inner_height, getDefaultScanSize(getContext()));

        // 扫描框边角颜色
        cornerColor = ta.getColor(R.styleable.ViewfinderView_inner_corner_color, resources.getColor(R.color.default_inner_corner_color));
        // 扫描框边角长度
        cornerLength = ta.getDimensionPixelSize(R.styleable.ViewfinderView_inner_corner_length, resources.getDimensionPixelSize(R.dimen.default_inner_corner_length));
        // 扫描框边角宽度
        cornerWidth = ta.getDimensionPixelSize(R.styleable.ViewfinderView_inner_corner_width, resources.getDimensionPixelSize(R.dimen.default_inner_corner_width));
        // 扫描控件
        scanLight = BitmapFactory.decodeResource(getResources(), ta.getResourceId(R.styleable.ViewfinderView_inner_scan_bitmap, R.drawable.xqrcode_ic_scan_light));
        int tintColor = ta.getColor(R.styleable.ViewfinderView_inner_scan_bitmap_tint, -1);
        if (tintColor != -1) {
            //改变颜色
            Bitmap tmp = scanLight.copy(Bitmap.Config.ARGB_8888, true);
            scanLight.recycle();
            Canvas canvas = new Canvas(tmp);
            canvas.drawColor(tintColor, PorterDuff.Mode.SRC_IN);
            scanLight = tmp;
        }

        // 扫描速度
        scanVelocity = ta.getDimensionPixelSize(R.styleable.ViewfinderView_inner_scan_speed, resources.getDimensionPixelSize(R.dimen.default_inner_scan_speed));
        // 扫描动画间隔
        scanAnimationInterval = ta.getInt(R.styleable.ViewfinderView_inner_scan_animation_interval, DEFAULT_ANIMATION_INTERVAL);
        isCircle = ta.getBoolean(R.styleable.ViewfinderView_inner_scan_isCircle, true);

        mMaskColor = ta.getColor(R.styleable.ViewfinderView_inner_mask_color, resources.getColor(R.color.default_mask_color));
        mResultColor = ta.getColor(R.styleable.ViewfinderView_inner_result_color, resources.getColor(R.color.default_result_color));
        mResultPointColor = ta.getColor(R.styleable.ViewfinderView_inner_result_point_color, resources.getColor(R.color.default_result_point_color));
        ta.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        //扫描的区域
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        mPaint.setColor(mResultBitmap != null ? mResultColor : mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, mPaint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, mPaint);
        canvas.drawRect(0, frame.bottom, width, height, mPaint);

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
                mPossibleResultPoints = new HashSet<>(5);
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
            postInvalidateDelayed(scanAnimationInterval, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    /**
     * 扫描线移动的y
     */
    private int scanLineTop;
    /**
     * 扫描线移动速度
     */
    private int scanVelocity;
    /**
     * 扫描线
     */
    private Bitmap scanLight;
    /**
     * 扫描动画间隔
     */
    private long scanAnimationInterval;
    /**
     * 是否展示小圆点
     */
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
            scanLineTop += scanVelocity;
        }
        Rect scanRect = new Rect(frame.left, scanLineTop, frame.right, scanLineTop + 30);
        canvas.drawBitmap(scanLight, null, scanRect, mPaint);
    }


    /**
     * 扫描框边角颜色
     */
    private int cornerColor;
    /**
     * 扫描框边角长度
     */
    private int cornerLength;
    /**
     * 扫描框边角宽度
     */
    private int cornerWidth;

    /**
     * 绘制取景框边框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {
        mPaint.setColor(cornerColor);
        mPaint.setStyle(Paint.Style.FILL);
        int corWidth = cornerWidth;
        int corLength = cornerLength;

        // 左上角
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth, frame.top + corLength, mPaint);
        canvas.drawRect(frame.left, frame.top, frame.left + corLength, frame.top + corWidth, mPaint);
        // 右上角
        canvas.drawRect(frame.right - corWidth, frame.top, frame.right, frame.top + corLength, mPaint);
        canvas.drawRect(frame.right - corLength, frame.top, frame.right, frame.top + corWidth, mPaint);
        // 左下角
        canvas.drawRect(frame.left, frame.bottom - corLength, frame.left + corWidth, frame.bottom, mPaint);
        canvas.drawRect(frame.left, frame.bottom - corWidth, frame.left + corLength, frame.bottom, mPaint);
        // 右下角
        canvas.drawRect(frame.right - corWidth, frame.bottom - corLength, frame.right, frame.bottom, mPaint);
        canvas.drawRect(frame.right - corLength, frame.bottom - corWidth, frame.right, frame.bottom, mPaint);
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
