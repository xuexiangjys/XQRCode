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

package com.xuexiang.xqrcodedemo;

import static com.xuexiang.xaop.consts.PermissionConsts.CAMERA;

import android.app.Application;

import com.xuexiang.xaop.XAOP;
import com.xuexiang.xaop.annotation.Permission;
import com.xuexiang.xpage.PageConfig;
import com.xuexiang.xqrcode.XQRCode;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.common.StringUtils;
import com.xuexiang.xutil.tip.ToastUtils;

/**
 * <pre>
 *     desc   :
 *     author : xuexiang
 *     time   : 2018/5/4 下午11:27
 * </pre>
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        XUtil.init(this);
        XUtil.debug(true);

        PageConfig.getInstance()
                .debug("PageLog")
                .init(this);

        XAOP.init(this); //初始化插件
        XAOP.debug(true); //日志打印切片开启
        //设置动态申请权限切片 申请权限被拒绝的事件响应监听
        XAOP.setOnPermissionDeniedListener(permissionsDenied -> ToastUtils.toast("权限申请被拒绝:" + StringUtils.listToString(permissionsDenied, ",")));

        initPermission();
    }

    @Permission(CAMERA)
    private void initPermission() {
        //设置相机的自动聚焦间隔
        XQRCode.setAutoFocusInterval(1500L);
        ToastUtils.toast("相机权限已获取！");
    }
}
