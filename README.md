# XQRCode
[![](https://jitpack.io/v/xuexiangjys/XQRCode.svg)](https://jitpack.io/#xuexiangjys/XQRCode)
[![api][apisvg]][api]
[![I](https://img.shields.io/github/issues/xuexiangjys/XQRCode.svg)](https://github.com/xuexiangjys/XQRCode/issues)
[![Star](https://img.shields.io/github/stars/xuexiangjys/XQRCode.svg)](https://github.com/xuexiangjys/XQRCode)

一个非常方便实用的二维码扫描、解析、生成库

## 关于我

| 公众号   | 掘金     |  知乎    |  CSDN   |   简书   |   思否  |   哔哩哔哩  |   今日头条
|---------|---------|--------- |---------|---------|---------|---------|---------|
| [我的Android开源之旅](https://ss.im5i.com/2021/06/14/6tqAU.png)  |  [点我](https://juejin.im/user/598feef55188257d592e56ed/posts)    |   [点我](https://www.zhihu.com/people/xuexiangjys/posts)       |   [点我](https://xuexiangjys.blog.csdn.net/)  |   [点我](https://www.jianshu.com/u/6bf605575337)  |   [点我](https://segmentfault.com/u/xuexiangjys)  |   [点我](https://space.bilibili.com/483850585)  |   [点我](https://img.rruu.net/image/5ff34ff7b02dd)

## 特点

* 支持快速集成条形码、二维码扫描功能。
* 支持自定义扫描界面。
* 支持二维码多次扫描。
* 支持生成带图标的二维码。
* 支持生成带背景图片的复杂二维码。
* 支持二维码解析功能

## 1、演示（请star支持）

![xqrcode.gif](https://ss.im5i.com/2021/07/09/gSwU2.gif)

### Demo下载

[![downloads](https://img.shields.io/badge/downloads-1.7M-blue.svg)](https://github.com/xuexiangjys/XQRCode/blob/master/apk/xqrcodedemo.apk?raw=true)

![xqrcode_download.png](https://ss.im5i.com/2021/07/09/gShv7.png)

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

2.然后在应用项目(一般是app)的 `build.gradle` 的 dependencies 添加:

```
dependencies {
  ...
   // 如果是androidx项目，使用1.1.0版本及以上
  implementation 'com.github.xuexiangjys:XQRCode:1.1.1'
   // 如果是support项目，请使用1.0.6版本
  implementation 'com.github.xuexiangjys:XQRCode:1.0.6'
}
```

### 2.2、二维码扫描

#### 默认二维码扫描

1.默认二维码扫描界面`CaptureActivity`

使用`XQRCode.startScan`直接调取默认二维码扫描。

```
XQRCode.startScan(this, REQUEST_CODE);
```

2.二维码的扫描结果通过Intent返回出来：

* `XQRCode.RESULT_TYPE`:扫描结果类型，`XQRCode.RESULT_SUCCESS`代表扫描成功，`XQRCode.RESULT_FAILED`代表扫描失败。
* `XQRCode.RESULT_DATA`:扫描二维码的数据内容。

3.自定义默认二维码扫描界面的主题样式：

```
<!-- 自定义默认二维码扫描界面的主题. -->
<style name="XQRCodeTheme.Custom">
    <item name="ViewfinderViewStyle">@style/ViewfinderView.Custom</item>
</style>

<style name="ViewfinderView.Custom">
    <item name="inner_corner_color">#123456</item>
    <item name="inner_corner_length">50dp</item>
    <item name="inner_corner_width">5dp</item>
    <item name="inner_scan_speed">20dp</item>
    <item name="inner_scan_isCircle">false</item>
</style>
```

4.自定义默认二维码扫描界面的界面样式，重写相关方法：

详细内容可参见[CustomCaptureActivity](https://github.com/xuexiangjys/XQRCode/blob/master/app/src/main/java/com/xuexiang/xqrcodedemo/activity/CustomCaptureActivity.java)

```
@Override
protected void beforeCapture() {
//做二维码采集之前需要做的事情
}

@Override
protected void onCameraInitSuccess() {
//相机初始化成功
}

@Override
protected void onCameraInitFailed() {
//相机初始化失败
}
```


下面的二维码扫描代码仅供参考：

```
/**
 * 开启二维码扫描
 */
@Permission(CAMERA)
private void startScan(ScanType scanType) {
    switch (scanType) {
        case DEFAULT:
            XQRCode.startScan(this, REQUEST_CODE);
            break;
        case DEFAULT_Custom:
            XQRCode.startScan(this, REQUEST_CODE, R.style.XQRCodeTheme_Custom);
            break;
        case REMOTE:
            Intent intent = new Intent(XQRCode.ACTION_DEFAULT_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE);
            break;
        default:
            break;
    }
}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    //处理二维码扫描结果
    if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
        //处理扫描结果（在界面上显示）
        handleScanResult(data);
    }

    //选择系统图片并解析
    else if (requestCode == REQUEST_IMAGE) {
        if (data != null) {
            Uri uri = data.getData();
            getAnalyzeQRCodeResult(uri);
        }
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

/**
 * 进行二维码解析
 *
 * @param uri
 */
private void getAnalyzeQRCodeResult(Uri uri) {
    XQRCode.analyzeQRCode(PathUtils.getFilePathByUri(getContext(), uri), new QRCodeAnalyzeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            ToastUtils.toast("解析结果:" + result, Toast.LENGTH_LONG);
        }

        @Override
        public void onAnalyzeFailed() {
            ToastUtils.toast("解析二维码失败", Toast.LENGTH_LONG);
        }
    });
}
```

#### 自定义二维码扫描

1. 自定义一个扫码界面布局。自定义的扫码界面需要定义一个`SurfaceView`和一个`ViewfinderView`，且id必须是`preview_view`和`viewfinder_view`。详情见如下布局代码：

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
        app:inner_scan_speed="10dp"
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
inner_corner_color | color | #0DC2FE | 扫描框四角的颜色
inner_corner_length | dimension | 32dp | 扫描框四角的长度
inner_corner_width | dimension | 6dp | 扫描框四角的宽度
inner_scan_bitmap | reference | R.drawable.xqrcode_ic_scan_light | 扫描控件图资源
inner_scan_bitmap_tint | color | / | 扫描控件图资源的色调
inner_scan_speed | dimension | 5dp | 扫描速度
inner_scan_animation_interval | integer | 25 | 扫描动画绘制的间期，单位是ms
inner_scan_isCircle | boolean | true | 小圆点是否展示


2. 调用`XQRCode.getCaptureFragment`的方法，传入自定义扫描界面的布局ID，可以获得带扫描功能的Fragment-`CaptureFragment`，将其填充到页面中。

```
// 为二维码扫描界面设置定制化界面
CaptureFragment captureFragment = XQRCode.getCaptureFragment(R.layout.layout_custom_camera);
captureFragment.setAnalyzeCallback(analyzeCallback);
getChildFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
```

3. 最后为CaptureFragment设置二维码解析回调接口`AnalyzeCallback`即可。

#### 设置相机聚焦的间隔

```
//设置相机的自动聚焦间隔
XQRCode.setAutoFocusInterval(1500L);
```

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

## 如果觉得项目还不错，可以考虑打赏一波

> 你的打赏是我维护的动力，我将会列出所有打赏人员的清单在下方作为凭证，打赏前请留下打赏项目的备注！

![pay.png](https://ss.im5i.com/2021/06/14/6twG6.png)

## 公众号

> 更多资讯内容，欢迎扫描关注我的个人微信公众号:【我的Android开源之旅】

![](https://ss.im5i.com/2021/06/14/65yoL.jpg)

[apisvg]: https://img.shields.io/badge/API-14+-brightgreen.svg
[api]: https://android-arsenal.com/api?level=14