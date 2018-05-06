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

package com.xuexiang.xqrcode.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.xuexiang.xqrcode.camera.BitmapLuminanceSource;
import com.xuexiang.xqrcode.decoding.DecodeFormatManager;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import static com.xuexiang.xqrcode.util.QRCodeProduceUtils.QRCODE_BITMAP_MAX_SIZE;

/**
 * <pre>
 *     desc   : 二维码解析工具类
 *     author : xuexiang
 *     time   : 2018/5/4 上午12:09
 * </pre>
 */
public final class QRCodeAnalyzeUtils {

    /**
     * Don't let anyone instantiate this class.
     */
    private QRCodeAnalyzeUtils() {
        throw new UnsupportedOperationException("Do not need instantiate!");
    }

    /**
     * 解析二维码（接口回调返回结果）
     *
     * @param QRCodePicPath   二维码图片的路径
     * @param analyzeCallback 解析的回调
     */
    public static void analyze(String QRCodePicPath, AnalyzeCallback analyzeCallback) {
        Bitmap bitmap = getQRCodeBitmap(QRCodePicPath);
        Result rawResult = analyze(bitmap);
        if (rawResult != null) {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeSuccess(bitmap, rawResult.getText());
            }
        } else {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeFailed();
            }
        }
    }

    /**
     * 解析二维码（简单返回结果，扫描失败返回空）
     *
     * @param QRCodePicPath 二维码图片的路径
     */
    public static String analyze(String QRCodePicPath) {
        Result rawResult = getAnalyzeQRCodeResult(QRCodePicPath);
        if (rawResult != null) {
            return rawResult.getText();
        } else {
            return "";
        }
    }

    /**
     * 获取解析二维码的结果
     *
     * @param QRCodePicPath 二维码图片的路径
     */
    public static Result getAnalyzeQRCodeResult(String QRCodePicPath) {
        Bitmap bitmap = getQRCodeBitmap(QRCodePicPath);
        return analyze(bitmap);
    }

    //==================================//
    /**
     * 获取二维码图片
     *
     * @param QRCodePicPath 二维码图片的路径
     * @return
     */
    private static Bitmap getQRCodeBitmap(String QRCodePicPath) {
        return getBitmap(QRCodePicPath, QRCODE_BITMAP_MAX_SIZE, QRCODE_BITMAP_MAX_SIZE);
    }

    /**
     * 解析二维码
     *
     * @param bitmap 二维码图片
     * @return 解析结果
     */
    @Nullable
    private static Result analyze(Bitmap bitmap) {
        MultiFormatReader multiFormatReader = new MultiFormatReader();

        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        if (decodeFormats.isEmpty()) {
            decodeFormats = new Vector<BarcodeFormat>();

            // 这里设置可扫描的类型，我这里选择了都支持
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        // 设置继续的字符编码格式为UTF8
        // hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        // 设置解析配置参数
        multiFormatReader.setHints(hints);

        // 开始对图像资源解码
        Result rawResult = null;
        try {
            rawResult = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(bitmap))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rawResult;
    }


    /**
     * 根据路径获取图片
     *
     * @param filePath  文件路径
     * @param maxWidth  图片最大宽度
     * @param maxHeight 图片最大高度
     * @return bitmap
     */
    public static Bitmap getBitmap(final String filePath, final int maxWidth, final int maxHeight) {
        if (!isFileExists(filePath)) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * Return the sample size.
     *
     * @param options   The options.
     * @param maxWidth  The maximum width.
     * @param maxHeight The maximum height.
     * @return the sample size
     */
    private static int calculateInSampleSize(final BitmapFactory.Options options,
                                             final int maxWidth,
                                             final int maxHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while ((width >>= 1) >= maxWidth && (height >>= 1) >= maxHeight) {
            inSampleSize <<= 1;
        }
        return inSampleSize;
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    private static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    private static boolean isFileExists(final String filePath) {
        return isFileExists(getFileByPath(filePath));
    }

    /**
     * 判断文件是否存在
     *
     * @param file 文件
     * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    private static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解析二维码结果
     */
    public interface AnalyzeCallback {

        /**
         * 解析成功
         *
         * @param bitmap 二维码图片
         * @param result 解析结果
         */
        void onAnalyzeSuccess(Bitmap bitmap, String result);

        /**
         * 解析失败
         */
        void onAnalyzeFailed();
    }


}
