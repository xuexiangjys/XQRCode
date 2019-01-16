package com.xuexiang.xqrcode.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;

/**
 * 二维码扫描界面实现接口
 *
 * @author xuexiang
 * @since 2019/1/16 下午11:57
 */
public interface ICaptureView {

    /**
     * 响应处理二维码扫描结果
     * @param result
     * @param barcode
     */
    void handleDecode(Result result, Bitmap barcode);

    /**
     * @return 获取所在窗口
     */
    Activity getActivity();

    /**
     * 开始画扫描控件
     */
    void drawViewfinder();

    /**
     * @return 获取扫码处理者
     */
    Handler getCaptureHandler();

}
