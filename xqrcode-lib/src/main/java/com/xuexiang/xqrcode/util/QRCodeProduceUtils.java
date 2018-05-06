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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import com.xuexiang.xqrcode.logs.QCLog;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * <pre>
 *     desc   : 二维码生成工具类
 *     author : xuexiang
 *     time   : 2018/5/4 上午12:05
 * </pre>
 */
public final class QRCodeProduceUtils {

    //============================带背景图片的二维码==================================//
    /**
     * For more information about QR code, refer to: https://en.wikipedia.org/wiki/QR_code
     * BYTE_EPT: Empty block
     * BYTE_DTA: Data block
     * BYTE_POS: Position block
     * BYTE_AGN: Align block
     * BYTE_TMG: Timing block
     * BYTE_PTC: Protector block, translucent layer (custom block, this is not included in QR code's standards)
     */
    private static final int BYTE_EPT = 0x0;
    private static final int BYTE_DTA = 0x1;
    private static final int BYTE_POS = 0x2;
    private static final int BYTE_AGN = 0x3;
    private static final int BYTE_TMG = 0x4;
    private static final int BYTE_PTC = 0x5;

    /**
     * 二维码最大尺寸
     */
    public static final int QRCODE_BITMAP_MAX_SIZE = 400;
    /**
     * 默认边缘宽度
     */
    private static int DEFAULT_MARGIN = 20;
    /**
     * 默认数据点缩放比例
     */
    private static float DEFAULT_DATA_DOT_SCALE = 0.3F;

    /**
     * 默认二值化中值
     */
    private static int DEFAULT_BINARIZING_THRESHOLD = 128;

    /**
     * Don't let anyone instantiate this class.
     */
    private QRCodeProduceUtils() {
        throw new UnsupportedOperationException("Do not need instantiate!");
    }

    /**
     * 获取二维码生成构建者
     * @param contents
     * @return
     */
    public static Builder newBuilder(String contents) {
        return new Builder(contents);
    }

    /**
     * 二维码生成构建者
     */
    public static class Builder {
        /**
         * 内容
         */
        String contents;
        /**
         * 尺寸
         */
        int size;
        /**
         * 边缘宽度
         */
        int margin;
        /**
         * 数据点的缩放比例
         */
        float dataDotScale;
        /**
         * 是否自动选取色值
         */
        boolean autoColor;
        /**
         * 深色色值
         */
        int colorDark;
        /**
         * 浅色色值
         */
        int colorLight;
        /**
         * 背景图案
         */
        Bitmap backgroundImage;
        /**
         * 是否有白色边缘
         */
        boolean whiteMargin;

        /**
         * 是否（二值化）灰度化背景图案
         */
        boolean binarize;
        /**
         * 二值化中值
         */
        int binarizeThreshold;

        public Builder(String contents) {
            this.contents = contents;
            this.size = QRCODE_BITMAP_MAX_SIZE;
            this.margin = DEFAULT_MARGIN;
            this.dataDotScale = DEFAULT_DATA_DOT_SCALE;
            this.autoColor = true;
            this.colorDark = Color.BLACK;
            this.colorLight = Color.WHITE;
            this.whiteMargin = false;
            this.binarize = false;
            this.binarizeThreshold = DEFAULT_BINARIZING_THRESHOLD;
        }

        /**
         * 设置二维码携带的内容
         *
         * @param contents 二维码携带的内容
         * @return
         */
        public Builder setContents(String contents) {
            this.contents = contents;
            return this;
        }

        /**
         * 设置二维码的尺寸
         *
         * @param size 二维码的尺寸
         * @return
         */
        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        /**
         * 设置二维码的边缘宽度
         *
         * @param margin 边缘宽度
         * @return
         */
        public Builder setMargin(int margin) {
            this.margin = margin;
            return this;
        }

        /**
         * 设置二维码的数据点缩放比例
         *
         * @param dataDotScale 数据点缩放比例
         * @return
         */
        public Builder setDataDotScale(float dataDotScale) {
            this.dataDotScale = dataDotScale;
            return this;
        }

        /**
         * 设置深色点（true-dots）色值
         *
         * @param colorDark
         * @return
         */
        public Builder setColorDark(int colorDark) {
            this.colorDark = colorDark;
            return this;
        }

        /**
         * 设置浅色点（false-dots）色值
         *
         * @param colorLight
         * @return
         */
        public Builder setColorLight(int colorLight) {
            this.colorLight = colorLight;
            return this;
        }

        /**
         * 设置背景图案
         *
         * @param backgroundImage
         * @return
         */
        public Builder setBackgroundImage(Bitmap backgroundImage) {
            this.backgroundImage = backgroundImage;
            return this;
        }

        /**
         * 设置是否是白色的边缘
         *
         * @param whiteMargin
         * @return
         */
        public Builder setWhiteMargin(boolean whiteMargin) {
            this.whiteMargin = whiteMargin;
            return this;
        }

        /**
         * 设置是否自动从背景图案中选取色值
         *
         * @param autoColor
         * @return
         */
        public Builder setAutoColor(boolean autoColor) {
            this.autoColor = autoColor;
            return this;
        }

        /**
         * 设置是否（二值化）灰度化背景图案
         *
         * @param binarize
         * @return
         */
        public Builder setBinarize(boolean binarize) {
            this.binarize = binarize;
            return this;
        }

        /**
         * 设置二值化中值
         *
         * @param binarizeThreshold
         * @return
         */
        public Builder setBinarizeThreshold(int binarizeThreshold) {
            this.binarizeThreshold = binarizeThreshold;
            return this;
        }

        /**
         * 生成二维码
         * @return
         */
        public Bitmap build() {
            return QRCodeProduceUtils.create(contents, size, margin, dataDotScale,
                    colorDark, colorLight, backgroundImage, whiteMargin, autoColor,
                    binarize, binarizeThreshold);
        }
    }


    public static Bitmap create(String contents, int size, int margin, int colorDark, int colorLight) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DATA_DOT_SCALE, colorDark, colorLight, null, true, true);
    }

    public static Bitmap create(String contents, int size, int margin, int colorDark, int colorLight, Bitmap backgroundImage) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DATA_DOT_SCALE, colorDark, colorLight, backgroundImage, true, true);
    }

    public static Bitmap create(String contents, int size, int margin, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin) throws IllegalArgumentException {
        return create(contents, size, margin, DEFAULT_DATA_DOT_SCALE, colorDark, colorLight, backgroundImage, whiteMargin, true);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage, whiteMargin, false);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, Bitmap backgroundImage, boolean whiteMargin, boolean binarize) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, Color.BLACK, Color.WHITE, backgroundImage, whiteMargin, true, binarize, DEFAULT_BINARIZING_THRESHOLD);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, Bitmap backgroundImage, boolean whiteMargin, boolean binarize, int binarizeThreshold) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, Color.BLACK, Color.WHITE, backgroundImage, whiteMargin, true, binarize, binarizeThreshold);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage, whiteMargin, autoColor, false, DEFAULT_BINARIZING_THRESHOLD);
    }

    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor, boolean binarize) throws IllegalArgumentException {
        return create(contents, size, margin, dataDotScale, colorDark, colorLight, backgroundImage, whiteMargin, autoColor, binarize, DEFAULT_BINARIZING_THRESHOLD);
    }

    /**
     * Create a QR matrix and render it use given configs.
     *
     * @param contents        Contents to encode.
     * @param size            Width as well as the height of the output QR code, includes margin.
     * @param margin          Margin to add around the QR code.
     * @param dataDotScale    Scale the data blocks and makes them appear smaller.
     * @param colorDark       Color of blocks. Will be OVERRIDE by autoColor. (BYTE_DTA, BYTE_POS, BYTE_AGN, BYTE_TMG)
     * @param colorLight      Color of empty space. Will be OVERRIDE by autoColor. (BYTE_EPT)
     * @param backgroundImage The background image to embed in the QR code. If null, no background image will be embedded.
     * @param whiteMargin     If true, background image will not be drawn on the margin area.
     * @param autoColor       If true, colorDark will be set to the dominant color of backgroundImage.
     * @return Bitmap of QR code
     * @throws IllegalArgumentException Refer to the messages below.
     */
    public static Bitmap create(String contents, int size, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor, boolean binarize, int binarizeThreshold) throws IllegalArgumentException {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Error: contents is empty. (contents.isEmpty())");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Error: a negative size is given. (size < 0)");
        }
        if (margin < 0) {
            throw new IllegalArgumentException("Error: a negative margin is given. (margin < 0)");
        }
        if (size - 2 * margin <= 0) {
            throw new IllegalArgumentException("Error: there is no space left for the QRCode. (size - 2 * margin <= 0)");
        }
        ByteMatrix byteMatrix = getBitMatrix(contents);
        if (size - 2 * margin < byteMatrix.getWidth()) {
            throw new IllegalArgumentException("Error: there is no space left for the QRCode. (size - 2 * margin < " + byteMatrix.getWidth() + ")");
        }
        if (dataDotScale < 0 || dataDotScale > 1) {
            throw new IllegalArgumentException("Error: an illegal data dot scale is given. (dataDotScale < 0 || dataDotScale > 1)");
        }
        return render(byteMatrix, size - 2 * margin, margin, dataDotScale, colorDark, colorLight, backgroundImage, whiteMargin, autoColor, binarize, binarizeThreshold);
    }

    private static Bitmap render(ByteMatrix byteMatrix, int innerRenderedSize, int margin, float dataDotScale, int colorDark, int colorLight, Bitmap backgroundImage, boolean whiteMargin, boolean autoColor, boolean binarize, int binarizeThreshold) {
        int nCount = byteMatrix.getWidth();
        float nWidth = (float) innerRenderedSize / nCount;
        float nHeight = (float) innerRenderedSize / nCount;

        Bitmap backgroundImageScaled = Bitmap.createBitmap(
                innerRenderedSize + (whiteMargin ? 0 : margin * 2),
                innerRenderedSize + (whiteMargin ? 0 : margin * 2),
                Bitmap.Config.ARGB_8888);
        if (backgroundImage != null) {
            scaleBitmap(backgroundImage, backgroundImageScaled);
        }

        Bitmap renderedBitmap = Bitmap.createBitmap(innerRenderedSize + margin * 2, innerRenderedSize + margin * 2, Bitmap.Config.ARGB_8888);

        if (autoColor && backgroundImage != null) {
            colorDark = getDominantColor(backgroundImage);
        }

        if (binarize && backgroundImage != null) {
            int threshold = DEFAULT_BINARIZING_THRESHOLD;
            if (binarizeThreshold > 0 && binarizeThreshold < 255) {
                threshold = binarizeThreshold;
            }
            colorDark = Color.BLACK;
            colorLight = Color.WHITE;
            binarize(backgroundImageScaled, threshold);
        }

        Paint paint = new Paint();
        Paint paintDark = new Paint();
        paintDark.setColor(colorDark);
        paintDark.setAntiAlias(true);
        Paint paintLight = new Paint();
        paintLight.setColor(colorLight);
        paintLight.setAntiAlias(true);
        Paint paintProtector = new Paint();
        paintProtector.setColor(Color.argb(120, 255, 255, 255));
        paintProtector.setAntiAlias(true);

        Canvas canvas = new Canvas(renderedBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(backgroundImageScaled, whiteMargin ? margin : 0, whiteMargin ? margin : 0, paint);


        for (int row = 0; row < byteMatrix.getHeight(); row++) {
            String s = "";
            for (int col = 0; col < byteMatrix.getWidth(); col++) {
                switch (byteMatrix.get(col, row)) {
                    case BYTE_AGN:
                    case BYTE_POS:
                    case BYTE_TMG:
                        canvas.drawRect(
                                margin + col * nWidth,
                                margin + row * nHeight,
                                margin + (col + 1.0f) * nWidth,
                                margin + (row + 1.0f) * nHeight,
                                paintDark
                        );
                        s += "Ｘ";
                        break;
                    case BYTE_DTA:
                        canvas.drawRect(
                                margin + (col + 0.5f * (1 - dataDotScale)) * nWidth,
                                margin + (row + 0.5f * (1 - dataDotScale)) * nHeight,
                                margin + (col + 0.5f * (1 + dataDotScale)) * nWidth,
                                margin + (row + 0.5f * (1 + dataDotScale)) * nHeight,
                                paintDark
                        );
                        s += "〇";
                        break;
                    case BYTE_PTC:
                        canvas.drawRect(
                                margin + col * nWidth,
                                margin + row * nHeight,
                                margin + (col + 1.0f) * nWidth,
                                margin + (row + 1.0f) * nHeight,
                                paintProtector
                        );
                        s += "＋";
                        break;
                    case BYTE_EPT:
                        canvas.drawRect(
                                margin + (col + 0.5f * (1 - dataDotScale)) * nWidth,
                                margin + (row + 0.5f * (1 - dataDotScale)) * nHeight,
                                margin + (col + 0.5f * (1 + dataDotScale)) * nWidth,
                                margin + (row + 0.5f * (1 + dataDotScale)) * nHeight,
                                paintLight
                        );
                        s += "　";
                        break;
                }
            }
            QCLog.dTag("QR_MAPPING", s);
        }

        return renderedBitmap;
    }

    private static ByteMatrix getBitMatrix(String contents) {
        try {
            QRCode qrCode = getProtoQRCode(contents, ErrorCorrectionLevel.H);
            int agnCenter[] = qrCode.getVersion().getAlignmentPatternCenters();
            ByteMatrix byteMatrix = qrCode.getMatrix();
            int matSize = byteMatrix.getWidth();
            for (int row = 0; row < matSize; row++) {
                for (int col = 0; col < matSize; col++) {
                    if (isTypeAGN(col, row, agnCenter, true)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_AGN);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    } else if (isTypePOS(col, row, matSize, true)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_POS);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    } else if (isTypeTMG(col, row, matSize)) {
                        if (byteMatrix.get(col, row) != BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_TMG);
                        } else {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    }

                    if (isTypePOS(col, row, matSize, false)) {
                        if (byteMatrix.get(col, row) == BYTE_EPT) {
                            byteMatrix.set(col, row, BYTE_PTC);
                        }
                    }
                }
            }
            return byteMatrix;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param contents             Contents to encode.
     * @param errorCorrectionLevel ErrorCorrectionLevel
     * @return QR code object.
     * @throws WriterException Refer to the messages below.
     */
    private static QRCode getProtoQRCode(String contents, ErrorCorrectionLevel errorCorrectionLevel) throws WriterException {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        }
        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        return Encoder.encode(contents, errorCorrectionLevel, hintMap);
    }

    private static boolean isTypeAGN(int x, int y, int[] agnCenter, boolean edgeOnly) {
        if (agnCenter.length == 0) return false;
        int edgeCenter = agnCenter[agnCenter.length - 1];
        for (int agnY : agnCenter) {
            for (int agnX : agnCenter) {
                if (edgeOnly && agnX != 6 && agnY != 6 && agnX != edgeCenter && agnY != edgeCenter)
                    continue;
                if ((agnX == 6 && agnY == 6) || (agnX == 6 && agnY == edgeCenter) || (agnY == 6 && agnX == edgeCenter))
                    continue;
                if (x >= agnX - 2 && x <= agnX + 2 && y >= agnY - 2 && y <= agnY + 2)
                    return true;
            }
        }
        return false;
    }

    private static boolean isTypePOS(int x, int y, int size, boolean inner) {
        if (inner) {
            return ((x < 7 && (y < 7 || y >= size - 7)) || (x >= size - 7 && y < 7));
        } else {
            return ((x <= 7 && (y <= 7 || y >= size - 8)) || (x >= size - 8 && y <= 7));
        }
    }

    private static boolean isTypeTMG(int x, int y, int size) {
        return ((y == 6 && (x >= 8 && x < size - 8)) || (x == 6 && (y >= 8 && y < size - 8)));
    }

    private static void scaleBitmap(Bitmap src, Bitmap dst) {
        Paint cPaint = new Paint();
        cPaint.setAntiAlias(true);
        cPaint.setDither(true);
        cPaint.setFilterBitmap(true);

        float ratioX = dst.getWidth() / (float) src.getWidth();
        float ratioY = dst.getHeight() / (float) src.getHeight();
        float middleX = dst.getWidth() * 0.5f;
        float middleY = dst.getHeight() * 0.5f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(dst);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(src, middleX - src.getWidth() / 2,
                middleY - src.getHeight() / 2, cPaint);
    }

    private static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 8, 8, true);
        int red = 0, green = 0, blue = 0, c = 0;
        int r, g, b;
        for (int y = 0; y < newBitmap.getHeight(); y++) {
            for (int x = 0; x < newBitmap.getHeight(); x++) {
                int color = newBitmap.getPixel(x, y);
                r = (color >> 16) & 0xFF;
                g = (color >> 8) & 0xFF;
                b = color & 0xFF;
                if (r > 200 || g > 200 || b > 200) continue;
                red += r;
                green += g;
                blue += b;
                c++;
            }
        }
        newBitmap.recycle();
        red = Math.max(0, Math.min(0xFF, red / c));
        green = Math.max(0, Math.min(0xFF, green / c));
        blue = Math.max(0, Math.min(0xFF, blue / c));
        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    private static void binarize(Bitmap bitmap, int threshold) {
        int r, g, b;
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getHeight(); x++) {
                int color = bitmap.getPixel(x, y);
                r = (color >> 16) & 0xFF;
                g = (color >> 8) & 0xFF;
                b = color & 0xFF;
                float sum = 0.30f * r + 0.59f * g + 0.11f * b;
                bitmap.setPixel(x, y, sum > threshold ? Color.WHITE : Color.BLACK);
            }
        }
    }

    //============================带图标的二维码==================================//

    /**
     * 生成含图标的二维码图片
     *
     * @param contents 二维码写入的数据
     * @param width    二维码的宽
     * @param height   二维码的高
     * @param logo     二维码中央的logo
     * @return
     */
    public static Bitmap create(String contents, int width, int height, Bitmap logo) {
        if (TextUtils.isEmpty(contents)) {
            return null;
        }
        try {
            Bitmap scaleLogo = getScaleLogo(logo, width, height);

            int offsetX = width / 2;
            int offsetY = height / 2;

            int scaleWidth = 0;
            int scaleHeight = 0;
            if (scaleLogo != null) {
                scaleWidth = scaleLogo.getWidth();
                scaleHeight = scaleLogo.getHeight();
                offsetX = (width - scaleWidth) / 2;
                offsetY = (height - scaleHeight) / 2;
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //设置空白边距的宽度
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix bitMatrix = new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (x >= offsetX && x < offsetX + scaleWidth && y >= offsetY && y < offsetY + scaleHeight) {
                        int pixel = scaleLogo.getPixel(x - offsetX, y - offsetY);
                        if (pixel == 0) {
                            if (bitMatrix.get(x, y)) {
                                pixel = 0xff000000;
                            } else {
                                pixel = 0xffffffff;
                            }
                        }
                        pixels[y * width + x] = pixel;
                    } else {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * width + x] = 0xff000000;
                        } else {
                            pixels[y * width + x] = 0xffffffff;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap getScaleLogo(Bitmap logo, int w, int h) {
        if (logo == null) return null;
        Matrix matrix = new Matrix();
        float scaleFactor = Math.min(w * 1.0f / 5 / logo.getWidth(), h * 1.0f / 5 / logo.getHeight());
        matrix.postScale(scaleFactor, scaleFactor);
        return Bitmap.createBitmap(logo, 0, 0, logo.getWidth(), logo.getHeight(), matrix, true);
    }
}
