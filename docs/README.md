# XQRCode
[![](https://jitpack.io/v/xuexiangjys/XQRCode.svg)](https://jitpack.io/#xuexiangjys/XQRCode)
[![api][apisvg]][api]
[![I](https://img.shields.io/github/issues/xuexiangjys/XQRCode.svg)](https://github.com/xuexiangjys/XQRCode/issues)
[![Star](https://img.shields.io/github/stars/xuexiangjys/XQRCode.svg)](https://github.com/xuexiangjys/XQRCode)

一个非常方便实用的二维码扫描、解析、生成库。

## 特点

* 支持快速集成条形码、二维码扫描功能。
* 支持自定义扫描界面。
* 支持二维码多次扫描。
* 支持生成带图标的二维码。
* 支持生成带背景图片的复杂二维码。
* 支持二维码解析功能

## 添加Gradle依赖

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
   // 如果是androidx项目，使用1.1.0版本及以上
  implementation 'com.github.xuexiangjys:XQRCode:1.1.0'
   // 如果是support项目，请使用1.0.6版本
  implementation 'com.github.xuexiangjys:XQRCode:1.0.6'
}
```

## 特别感谢

https://github.com/yipianfengye/android-zxingLibrary

## 联系方式

[![](https://img.shields.io/badge/点击一键加入QQ交流群-602082750-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=9922861ef85c19f1575aecea0e8680f60d9386080a97ed310c971ae074998887)

![gzh_weixin.jpg](https://img.rruu.net/image/5f871cfff3194)

[apisvg]: https://img.shields.io/badge/API-14+-brightgreen.svg
[api]: https://android-arsenal.com/api?level=14