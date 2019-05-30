package com.xuexiang.xqrcodedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;

import com.xuexiang.xqrcode.ui.CaptureActivity;
import com.xuexiang.xqrcodedemo.R;

/**
 * 自定义二维码扫描界面
 *
 * @author xuexiang
 * @since 2019/5/30 10:43
 */
public class CustomCaptureActivity extends CaptureActivity {
    /**
     * 开始二维码扫描
     *
     * @param fragment
     * @param requestCode 请求码
     * @param theme       主题
     */
    public static void start(Fragment fragment, int requestCode, int theme) {
        Intent intent = new Intent(fragment.getContext(), CustomCaptureActivity.class);
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
        Intent intent = new Intent(activity, CustomCaptureActivity.class);
        intent.putExtra(KEY_CAPTURE_THEME, theme);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getCaptureLayoutId() {
        return R.layout.activity_custom_capture;
    }

    @Override
    protected void beforeCapture() {
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
