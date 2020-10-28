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

package com.xuexiang.xqrcode.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.xuexiang.xqrcode.R;
import com.xuexiang.xqrcode.XQRCode;
import com.xuexiang.xqrcode.util.QRCodeAnalyzeUtils;

/**
 * 默认的二维码扫描Activity
 *
 * @author xuexiang
 * @since 2019/5/17 18:31
 */
public class CaptureActivity extends AppCompatActivity {

    public final static int REQUEST_CODE_REQUEST_PERMISSIONS = 222;

    public final static String KEY_CAPTURE_THEME = "com.xuexiang.xqrcode.ui.KEY_CAPTURE_THEME";

    /**
     * 开始二维码扫描
     *
     * @param fragment
     * @param requestCode 请求码
     * @param theme       主题
     */
    public static void start(Fragment fragment, int requestCode, int theme) {
        Intent intent = new Intent(fragment.getContext(), CaptureActivity.class);
        intent.putExtra(KEY_CAPTURE_THEME, theme);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 开始二维码扫描
     *
     * @param activity
     * @param requestCode 请求码
     * @param theme       主题
     */
    public static void start(Activity activity, int requestCode, int theme) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        intent.putExtra(KEY_CAPTURE_THEME, theme);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getIntent().getIntExtra(KEY_CAPTURE_THEME, R.style.XQRCodeTheme));
        super.onCreate(savedInstanceState);
        setContentView(getCaptureLayoutId());
        beforeCapture();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_REQUEST_PERMISSIONS);
                return;
            }
        }
        initCaptureFragment();
    }

    protected int getCaptureLayoutId() {
        return R.layout.xqrcode_activity_capture;
    }

    /**
     * 做二维码采集之前需要做的事情
     */
    protected void beforeCapture() {

    }

    /**
     * 初始化采集
     */
    protected void initCaptureFragment() {
        CaptureFragment captureFragment = new CaptureFragment();
        captureFragment.setAnalyzeCallback(analyzeCallback);
        captureFragment.setCameraInitCallBack(cameraInitCallBack);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_zxing_container, captureFragment).commit();
    }

    /**
     * 照相机初始化监听
     */
    CaptureFragment.CameraInitCallBack cameraInitCallBack = new CaptureFragment.CameraInitCallBack() {
        @Override
        public void callBack(@Nullable Exception e) {
            if (e != null) {
                CaptureActivity.showNoPermissionTip(CaptureActivity.this);
                onCameraInitFailed();
            } else {
                //照相机初始化成功
                onCameraInitSuccess();
            }
        }
    };

    /**
     * 相机初始化成功
     */
    protected void onCameraInitSuccess() {

    }

    /**
     * 相机初始化失败
     */
    protected void onCameraInitFailed() {

    }

    /**
     * 显示无照相机权限提示
     *
     * @param activity
     * @param listener 确定点击事件
     * @return
     */
    public static AlertDialog showNoPermissionTip(final Activity activity, DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(activity)
                .setTitle(R.string.xqrcode_pay_attention)
                .setMessage(R.string.xqrcode_not_get_permission)
                .setPositiveButton(R.string.xqrcode_submit, listener)
                .show();
    }

    /**
     * 显示无照相机权限提示
     *
     * @param activity
     * @return
     */
    public static AlertDialog showNoPermissionTip(final Activity activity) {
        return showNoPermissionTip(activity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
    }

    /**
     * 二维码解析回调函数
     */
    QRCodeAnalyzeUtils.AnalyzeCallback analyzeCallback = new QRCodeAnalyzeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap bitmap, String result) {
            handleAnalyzeSuccess(bitmap, result);
        }

        @Override
        public void onAnalyzeFailed() {
            handleAnalyzeFailed();
        }
    };

    /**
     * 处理扫描成功
     *
     * @param bitmap
     * @param result
     */
    protected void handleAnalyzeSuccess(Bitmap bitmap, String result) {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(XQRCode.RESULT_TYPE, XQRCode.RESULT_SUCCESS);
        bundle.putString(XQRCode.RESULT_DATA, result);
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * 处理解析失败
     */
    protected void handleAnalyzeFailed() {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(XQRCode.RESULT_TYPE, XQRCode.RESULT_FAILED);
        bundle.putString(XQRCode.RESULT_DATA, "");
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCaptureFragment();
            } else {
                handleRequestPermissionDeny();
            }
        }
    }

    /**
     * 处理权限申请拒绝
     */
    protected void handleRequestPermissionDeny() {
        CaptureActivity.showNoPermissionTip(CaptureActivity.this);
    }


}