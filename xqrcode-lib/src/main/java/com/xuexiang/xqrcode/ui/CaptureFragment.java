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

package com.xuexiang.xqrcode.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.xuexiang.xqrcode.R;
import com.xuexiang.xqrcode.XQRCode;
import com.xuexiang.xqrcode.camera.CameraManager;
import com.xuexiang.xqrcode.decoding.CaptureViewHandler;
import com.xuexiang.xqrcode.decoding.InactivityTimer;
import com.xuexiang.xqrcode.util.QRCodeAnalyzeUtils;
import com.xuexiang.xqrcode.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

import static android.Manifest.permission_group.CAMERA;

/**
 * 自定义实现的扫描Fragment
 *
 * @author xuexiang
 * @since 2019/1/16 下午10:51
 */
public class CaptureFragment extends Fragment implements ICaptureView, SurfaceHolder.Callback {

    private CaptureViewHandler mHandler;
    private ViewfinderView mViewfinderView;
    private boolean mHasSurface;
    private Vector<BarcodeFormat> mDecodeFormats;
    private String mCharacterSet;
    private InactivityTimer mInactivityTimer;
    private MediaPlayer mMediaPlayer;
    private boolean mPlayBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean mVibrate;
    private SurfaceHolder mSurfaceHolder;
    @Nullable
    private CameraInitCallBack mCameraInitCallBack;
    private QRCodeAnalyzeUtils.AnalyzeCallback mAnalyzeCallback;
    private Camera mCamera;
    private boolean mIsRepeated;
    private long mScanInterval;

    /**
     * 构建扫描Fragment
     *
     * @param layoutId     布局id
     * @return
     */
    public static CaptureFragment newInstance(int layoutId) {
        return CaptureFragment.newInstance(layoutId, false, 0);
    }

    /**
     * 构建扫描Fragment
     *
     * @param layoutId     布局id
     * @param isRepeated   是否重复扫码
     * @param scanInterval 扫码间隙
     * @return
     */
    public static CaptureFragment newInstance(int layoutId, boolean isRepeated, long scanInterval) {
        if (layoutId == -1) {
            return null;
        }
        CaptureFragment captureFragment = new CaptureFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(XQRCode.KEY_LAYOUT_ID, layoutId);
        bundle.putBoolean(XQRCode.KEY_IS_REPEATED, isRepeated);
        bundle.putLong(XQRCode.KEY_SCAN_INTERVAL, scanInterval);
        captureFragment.setArguments(bundle);
        return captureFragment;
    }

    /**
     * 处理Activity【防止锁屏和fragment里面放surfaceView，第一次黑屏的问题】
     *
     * @param activity
     */
    public static void onCreate(Activity activity) {
        if (activity != null) {
            // 防止锁屏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //为了解决fragment里面放SurfaceView，第一次黑屏的问题
            activity.getWindow().setFormat(PixelFormat.TRANSLUCENT);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate(getActivity());

        CameraManager.init(requireNonNull(getActivity()).getApplicationContext());

        mHasSurface = false;
        mInactivityTimer = new InactivityTimer(this.getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        View view = null;
        if (bundle != null) {
            int layoutId = bundle.getInt(XQRCode.KEY_LAYOUT_ID);
            if (layoutId != -1) {
                view = inflater.inflate(layoutId, null);
            }
            mIsRepeated = bundle.getBoolean(XQRCode.KEY_IS_REPEATED);
            mScanInterval = bundle.getLong(XQRCode.KEY_SCAN_INTERVAL);
        }

        if (view == null) {
            view = inflater.inflate(R.layout.xqrcode_fragment_capture, null);
        }

        mViewfinderView = view.findViewById(R.id.viewfinder_view);
        SurfaceView surfaceView = view.findViewById(R.id.preview_view);
        mSurfaceHolder = surfaceView.getHolder();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHasSurface) {
            initCamera(mSurfaceHolder);
        } else {
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        mDecodeFormats = null;
        mCharacterSet = null;

        mPlayBeep = true;
        AudioManager audioService = (AudioManager) requireNonNull(getActivity()).getSystemService(Context.AUDIO_SERVICE);
        if (audioService != null && audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            mPlayBeep = false;
        }
        initBeepSound();
        mVibrate = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void onDestroyView() {
        if (mInactivityTimer != null) {
            mInactivityTimer.shutdown();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            //关键语句
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroyView();
    }


    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    @Override
    public void handleDecode(Result result, Bitmap barcode) {
        mInactivityTimer.onActivity();
        playBeepSoundAndVibrate();

        if (result == null || TextUtils.isEmpty(result.getText())) {
            if (mAnalyzeCallback != null) {
                mAnalyzeCallback.onAnalyzeFailed();
            }
        } else {
            if (mAnalyzeCallback != null) {
                mAnalyzeCallback.onAnalyzeSuccess(barcode, result.getText());
            }
        }

        if (mIsRepeated) {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(R.id.restart_preview, mScanInterval);
            }
        }

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            mCamera = CameraManager.get().getCamera();
        } catch (Exception e) {
            if (mCameraInitCallBack != null) {
                mCameraInitCallBack.callBack(e);
            }
            return;
        }
        if (mCameraInitCallBack != null) {
            //打开成功
            mCameraInitCallBack.callBack(null);
        }
        if (mHandler == null) {
            mHandler = new CaptureViewHandler(this, mDecodeFormats, mCharacterSet, mViewfinderView);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
        if (mCamera != null) {
            if (CameraManager.get().isPreviewing()) {
                if (!CameraManager.get().isUseOneShotPreviewCallback()) {
                    mCamera.setPreviewCallback(null);
                }
                mCamera.stopPreview();
                CameraManager.get().getPreviewCallback().setHandler(null, 0);
                CameraManager.get().getAutoFocusCallback().setHandler(null, 0);
                CameraManager.get().setPreviewing(false);
            }
        }
    }

    @Override
    public Handler getCaptureHandler() {
        return mHandler;
    }

    @Override
    public void drawViewfinder() {
        mViewfinderView.drawViewfinder();
    }

    private void initBeepSound() {
        if (mPlayBeep && mMediaPlayer == null) {
            requireNonNull(getActivity()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(mBeepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mMediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mMediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                mMediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (mPlayBeep && mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        if (mVibrate) {
            Vibrator vibrator = (Vibrator) requireNonNull(getActivity()).getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener mBeepListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    public QRCodeAnalyzeUtils.AnalyzeCallback getAnalyzeCallback() {
        return mAnalyzeCallback;
    }

    @RequiresPermission(CAMERA)
    public void setAnalyzeCallback(QRCodeAnalyzeUtils.AnalyzeCallback analyzeCallback) {
        mAnalyzeCallback = analyzeCallback;
    }

    /**
     * Set callback for Camera check whether Camera init success or not.
     */
    public void setCameraInitCallBack(CameraInitCallBack callBack) {
        mCameraInitCallBack = callBack;
    }

    public interface CameraInitCallBack {
        /**
         * Callback for Camera init result.
         *
         * @param e If is's null,means success.otherwise Camera init failed with the Exception.
         */
        void callBack(@Nullable Exception e);
    }

    public static <T> T requireNonNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }


}
