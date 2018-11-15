package com.xuexiang.xqrcodetest;

import android.app.Activity;
import android.os.Bundle;

/**
 *
 *
 * @author xuexiang
 * @since 2018/11/15 上午10:03
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
