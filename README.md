# XQRCode
[![xq][xqsvg]][xq]  [![api][apisvg]][api]

一个非常方便实用的二维码扫描、解析、生成库

## 关于我

[![github](https://img.shields.io/badge/GitHub-xuexiangjys-blue.svg)](https://github.com/xuexiangjys)   [![csdn](https://img.shields.io/badge/CSDN-xuexiangjys-green.svg)](http://blog.csdn.net/xuexiangjys)

## 特点

* 支持快速集成条形码、二维码扫描功能。
* 支持自定义扫描界面。
* 支持生成带图标的二维码。
* 支持生成带背景图片的复杂二维码。
* 支持二维码解析功能

## 1、演示（请star支持）
![](https://github.com/xuexiangjys/XQRCode/blob/master/img/xqrcode.gif)

### Demo下载

[![downloads](https://img.shields.io/badge/downloads-1.7M-blue.svg)](https://github.com/xuexiangjys/XQRCode/blob/master/apk/xqrcodedemo.apk?raw=true)

![](https://github.com/xuexiangjys/XQRCode/blob/master/img/download.png)


## 2、如何使用
目前支持主流开发工具AndroidStudio的使用，直接配置build.gradle，增加依赖即可.

### 2.1、Android Studio导入方法，添加Gradle依赖

1.先在项目根目录的 build.gradle 的 repositories 添加:
```
allprojects {
     repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

2.然后在dependencies添加:

```
dependencies {
  ...
  implementation 'com.github.xuexiangjys:XQRCode:1.0.1'
}
```

### 2.2、二维码扫描

1.默认二维码扫描界面`CaptureActivity`

二维码的扫描结果通过Intent返回出来：

* `XQRCode.RESULT_TYPE`:扫描结果类型，`XQRCode.RESULT_SUCCESS`代表扫描成功，`XQRCode.RESULT_FAILED`代表扫描失败。
* `XQRCode.RESULT_DATA`:扫描二维码的数据内容。

```
/**
 * 开启二维码扫描
 */
@Permission(CAMERA)
@IOThread(ThreadType.Single)
private void startScan(boolean isCustom) {
    if (isCustom) {
        openPageForResult(CustomCaptureFragment.class, null, REQUEST_CUSTOM_SCAN);
    } else {
        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }
}

/**
 * 处理二维码扫描结果
 * @param data
 */
private void handleScanResult(Intent data) {
    if (data != null) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            if (bundle.getInt(XQRCode.RESULT_TYPE) == XQRCode.RESULT_SUCCESS) {
                String result = bundle.getString(XQRCode.RESULT_DATA);
                ToastUtils.toast("解析结果:" + result, Toast.LENGTH_LONG);
            } else if (bundle.getInt(XQRCode.RESULT_TYPE) == XQRCode.RESULT_FAILED) {
                ToastUtils.toast("解析二维码失败", Toast.LENGTH_LONG);
            }
        }
    }
}
```

2.自定义二维码扫描界面

（1）自定义一个扫码界面布局。自定义的扫码界面需要定义一个`SurfaceView`和一个`ViewfinderView`，且id必须是`preview_view`和`viewfinder_view`。详情见如下布局代码：

```
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <SurfaceView
        android:id="@+id/preview_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

    <com.xuexiang.xqrcode.view.ViewfinderView
        android:id="@+id/viewfinder_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:inner_corner_color="@color/scan_corner_color"
        app:inner_corner_length="30dp"
        app:inner_corner_width="5dp"
        app:inner_marginTop="120dp"
        app:inner_scan_bitmap="@mipmap/ic_scan_image"
        app:inner_scan_isCircle="false"
        app:inner_scan_speed="10"
        app:inner_height="300dp"
        app:inner_width="300dp" />

</FrameLayout>
```

ViewfinderView属性表

属性名 | 类型 | 默认值 | 备注
:-|:-:|:-:|:-
inner_width | dimension | 屏幕宽度的3／4 | 扫描框的宽度
inner_height | dimension | 屏幕宽度的3／4 | 扫描框的高度
inner_marginTop | dimension | 居中效果 | 扫描框距离顶部的距离
inner_corner_color | color | #45DDDD | 扫描框四角的颜色
inner_corner_length | dimension | 65px | 扫描框四角的长度
inner_corner_width | dimension | 15px | 扫描框四角的宽度
inner_scan_bitmap | reference | R.drawable.xqrcode_ic_scan_light | 扫描控件图资源
inner_scan_speed | integer | 5px | 扫描速度
inner_scan_isCircle | boolean | true | 小圆点是否展示


（2）调用`XQRCode.getCaptureFragment`的方法，传入自定义扫描界面的布局ID，可以获得带扫描功能的Fragment-`CaptureFragment`，将其填充到页面中。

```
// 为二维码扫描界面设置定制化界面
CaptureFragment captureFragment = XQRCode.getCaptureFragment(R.layout.layout_custom_camera);
captureFragment.setAnalyzeCallback(analyzeCallback);
getChildFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
```

（3）最后为CaptureFragment设置二维码解析回调接口`AnalyzeCallback`即可。

### 2.3、二维码生成

1.简单的二维码生成

调用`XQRCode.createQRCodeWithLogo`,传入二维码携带的内容、尺寸、图标即可生成二维码Bitmap。

2.复杂的二维码生成

调用`XQRCode.newQRCodeBuilder`可以获得二维码生成构建者。可设置的参数如下:

* setContents: 设置二维码携带的内容
* setSize: 设置二维码的尺寸
* setMargin: 设置二维码的边缘宽度
* setDataDotScale: 设置二维码的数据点缩放比例
* setColorDark: 设置深色点（true-dots）色值
* setColorLight: 设置浅色点（false-dots）色值
* setBackgroundImage: 设置背景图案
* setWhiteMargin: 设置是否是白色的边缘
* setAutoColor: 设置是否自动从背景图案中选取色值
* setBinarize: 设置是否（二值化）灰度化背景图案
* setBinarizeThreshold: 设置二值化中值

最后调用`build`方法即可生成二维码。

### 2.4、二维码解析

1.只需要解析二维码携带的数据内容，可直接调用`XQRCode.analyzeQRCode`方法，传入二维码的绝对路径即可。

2.如果需要完整的二维码解析结果，可调用`XQRCode.getAnalyzeQRCodeResult`方法，传入二维码的绝对路径即可。

## 混淆配置

```
-dontwarn com.google.zxing.**
-keep class com.google.zxing.**{*;}
```

## 特别感谢
https://github.com/yipianfengye/android-zxingLibrary

## 联系方式

[![](https://img.shields.io/badge/点击一键加入QQ交流群-602082750-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=9922861ef85c19f1575aecea0e8680f60d9386080a97ed310c971ae074998887)

![](https://github.com/xuexiangjys/XPage/blob/master/img/qq_group.jpg)

[xqsvg]: https://img.shields.io/badge/XQRCode-v1.0.1-brightgreen.svg
[xq]: https://github.com/xuexiangjys/XQRCode
[apisvg]: https://img.shields.io/badge/API-14+-brightgreen.svg
[api]: https://android-arsenal.com/api?level=14