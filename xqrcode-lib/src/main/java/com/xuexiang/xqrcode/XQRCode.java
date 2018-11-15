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

package com.xuexiang.xqrcode;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;

import com.google.zxing.Result;
import com.xuexiang.xqrcode.camera.CameraManager;
import com.xuexiang.xqrcode.logs.QCLog;
import com.xuexiang.xqrcode.ui.CaptureFragment;
import com.xuexiang.xqrcode.util.QRCodeAnalyzeUtils;
import com.xuexiang.xqrcode.util.QRCodeProduceUtils;
import com.xuexiang.xqrcode.util.QRCodeProduceUtils.Builder;

import static com.xuexiang.xqrcode.util.QRCodeProduceUtils.QRCODE_BITMAP_MAX_SIZE;

/**
 * <pre>
 *     desc   : 二维码 XQRCode API
 *     author : xuexiang
 *     time   : 2018/5/4 上午12:33
 * </pre>
 */
public class XQRCode {

    /**
     * 扫描返回的结果
     */
    public static final String RESULT_TYPE = "result_type";
    /**
     * 扫描到的数据
     */
    public static final String RESULT_DATA = "result_data";
    /**
     * 扫描成功
     */
    public static final int RESULT_SUCCESS = 1;
    /**
     * 扫描失败
     */
    public static final int RESULT_FAILED = 2;

    /**
     * CaptureFragment
     */
    public static final String KEY_LAYOUT_ID = "key_layout_id";

    /**
     * 默认二维码扫描的activity启动
     */
    public static final String ACTION_DEFAULT_CAPTURE = "com.xuexiang.xqrcode.ui.captureactivity";

    /**
     * 设置是否打开调试
     *
     * @param isDebug
     */
    public static void debug(boolean isDebug) {
        QCLog.debug(isDebug);
    }

    /**
     * 设置调试模式
     *
     * @param tag
     */
    public static void debug(String tag) {
        QCLog.debug(tag);
    }

    //==================================CaptureFragment=================================//

    /**
     * 获取CaptureFragment设置layout参
     *
     * @param layoutId
     */
    public static CaptureFragment getCaptureFragment(int layoutId) {
        return CaptureFragment.newInstance(layoutId);
    }

    /**
     * 为CaptureFragment设置layout参数
     *
     * @param captureFragment
     * @param layoutId
     */
    public static void setFragmentArgs(CaptureFragment captureFragment, int layoutId) {
        if (captureFragment == null || layoutId == -1) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_LAYOUT_ID, layoutId);
        captureFragment.setArguments(bundle);
    }

    //================================二维码解析===================================//

    /**
     * 解析二维码（接口回调返回结果）
     *
     * @param QRCodePicPath   二维码图片的路径
     * @param analyzeCallback
     */
    public static void analyzeQRCode(String QRCodePicPath, QRCodeAnalyzeUtils.AnalyzeCallback analyzeCallback) {
        QRCodeAnalyzeUtils.analyze(QRCodePicPath, analyzeCallback);
    }

    /**
     * 解析二维码（简单返回结果，扫描失败返回空）
     *
     * @param QRCodePicPath 二维码图片的路径
     */
    public static String analyzeQRCode(String QRCodePicPath) {
        return QRCodeAnalyzeUtils.analyze(QRCodePicPath);
    }

    /**
     * 获取解析二维码的结果
     *
     * @param QRCodePicPath 二维码图片的路径
     */
    public static Result getAnalyzeQRCodeResult(String QRCodePicPath) {
        return QRCodeAnalyzeUtils.getAnalyzeQRCodeResult(QRCodePicPath);
    }

    //================================二维码生成===================================//

    /**
     * 生成含图标的二维码图片
     *
     * @param contents 二维码写入的数据
     * @param width    二维码的宽
     * @param height   二维码的高
     * @param logo     二维码中央的logo
     * @return 含图标的二维码
     */
    public static Bitmap createQRCodeWithLogo(String contents, int width, int height, Bitmap logo) {
        return QRCodeProduceUtils.create(contents, width, height, logo);
    }

    /**
     * 生成含图标的二维码图片
     *
     * @param contents 二维码写入的数据
     * @param logo     二维码中央的logo
     * @return 含图标的二维码
     */
    public static Bitmap createQRCodeWithLogo(String contents, Bitmap logo) {
        return QRCodeProduceUtils.create(contents, QRCODE_BITMAP_MAX_SIZE, QRCODE_BITMAP_MAX_SIZE, logo);
    }

    /**
     * 获取二维码生成构建者
     *
     * @param contents
     * @return
     */
    public static Builder newQRCodeBuilder(String contents) {
        return QRCodeProduceUtils.newBuilder(contents);
    }

    //================================FlashLight===================================//

    /**
     * 开关闪光灯
     *
     * @param isEnable 是否开启闪光灯
     */
    public static void enableFlashLight(boolean isEnable) throws RuntimeException {
        if (isEnable) {
            Camera camera = CameraManager.get().getCamera();
            if (camera != null) {
                Camera.Parameters parameter = camera.getParameters();
                parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameter);
            }
        } else {
            Camera camera = CameraManager.get().getCamera();
            if (camera != null) {
                Camera.Parameters parameter = camera.getParameters();
                parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameter);
            }
        }
    }
}
