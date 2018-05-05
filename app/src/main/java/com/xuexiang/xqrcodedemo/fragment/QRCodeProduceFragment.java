package com.xuexiang.xqrcodedemo.fragment;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.xuexiang.xaop.annotation.IOThread;
import com.xuexiang.xaop.annotation.MainThread;
import com.xuexiang.xaop.annotation.Permission;
import com.xuexiang.xaop.enums.ThreadType;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.BaseFragment;
import com.xuexiang.xqrcode.XQRCode;
import com.xuexiang.xqrcodedemo.R;
import com.xuexiang.xqrcodedemo.util.PathUtils;
import com.xuexiang.xutil.common.StringUtils;
import com.xuexiang.xutil.data.DateUtils;
import com.xuexiang.xutil.display.ImageUtils;
import com.xuexiang.xutil.file.FileUtils;
import com.xuexiang.xutil.tip.ToastUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

import static com.xuexiang.xaop.consts.PermissionConsts.STORAGE;

/**
 * <pre>
 *     desc   :
 *     author : xuexiang
 *     time   : 2018/5/5 下午11:06
 * </pre>
 */
@Page(name = "二维码生成器")
public class QRCodeProduceFragment extends BaseFragment {

    @BindView(R.id.et_input)
    EditText mEtInput;
    @BindView(R.id.iv_qrcode)
    ImageView mIvQrcode;

    private boolean isQRCodeCreated = false;
    /**
     * 布局的资源id
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_qrcode_produce;
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {

    }

    /**
     * 初始化监听
     */
    @Override
    protected void initListeners() {

    }

    @OnClick({R.id.btn_save, R.id.btn_create_no_logo, R.id.btn_create_with_logo})
    void OnClick(View v) {
        switch(v.getId()) {
            case R.id.btn_save:
                saveQRCode();
                break;
            case R.id.btn_create_no_logo:
                if (StringUtils.isSpace(mEtInput.getEditableText().toString())) {
                    ToastUtils.toast("请输入二维码内容!");
                    return;
                }

                createQRCodeWithLogo(ImageUtils.getBitmap(R.mipmap.ic_launcher));

                break;
            case R.id.btn_create_with_logo:
                if (StringUtils.isSpace(mEtInput.getEditableText().toString())) {
                    ToastUtils.toast("请输入二维码内容!");
                    return;
                }

                createQRCodeWithLogo(null);

                break;
            default:
                break;
        }
    }

    @IOThread(ThreadType.Disk)
    @Permission(STORAGE)
    private void saveQRCode() {
        if (isQRCodeCreated) {
            boolean result = ImageUtils.save(ImageUtils.view2Bitmap(mIvQrcode), FileUtils.getDiskCacheDir() + File.separator + "XQRCode_" + DateUtils.getNowMills() + ".png", Bitmap.CompressFormat.PNG);
            ToastUtils.toast("二维码保存" + (result ? "成功" : "失败") + "!");
        } else {
            ToastUtils.toast("请先生成二维码!");
        }
    }

    @IOThread(ThreadType.Single)
    private void createQRCodeWithLogo(Bitmap logo) {
        showQRCode(XQRCode.createQRCodeWithLogo(mEtInput.getEditableText().toString(), 400, 400, logo));
        isQRCodeCreated = true;
    }

    @MainThread
    private void showQRCode(Bitmap QRCode) {
        mIvQrcode.setImageBitmap(QRCode);
    }

}
