package com.xuexiang.xqrcode;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;

import com.google.zxing.Result;
import com.xuexiang.xqrcode.camera.CameraManager;
import com.xuexiang.xqrcode.ui.CaptureFragment;
import com.xuexiang.xqrcode.util.QRCodeAnalyzeUtils;
import com.xuexiang.xqrcode.util.QRCodeProduceUtils;

/**
 * <pre>
 *     desc   : XQRCode API
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
     * @param QRCodePicPath   二维码图片的路径
     */
    public static String analyzeQRCode(String QRCodePicPath) {
        return QRCodeAnalyzeUtils.analyze(QRCodePicPath);
    }

    /**
     * 获取解析二维码的结果
     *
     * @param QRCodePicPath   二维码图片的路径
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



    //================================FlashLight===================================//
    /**
     * 开关闪光灯
     *
     * @param isEnable 是否开启闪光灯
     */
    public static void enableFlashLight(boolean isEnable) {
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
