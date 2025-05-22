package tw.com.chainsea.chat.ui.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.ui.fragment.ImageConfirmFragment;

/**
 * ImageSendActivity
 * Created by 90Chris on 2014/11/16.
 */
public class ImageConfirmActivity extends FragmentActivity {
    public final String TAG = "ImageConfirmActivity";
    private ImageConfirmFragment fragment = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    protected Fragment createFragment() {
        int way = getIntent().getIntExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
        int type = getIntent().getIntExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_NO_CROP);
        fragment = ImageConfirmFragment.newInstance(way, type);
        return fragment;
    }

    @Override
    protected View rightView() {
        return Tools.createRightView(this, R.string.alert_confirm);
    }

    @Override
    protected String getTitleText() {
        return null;
    }

    @Override
    public void rightAction() {
        fragment.confirmImage();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }
}
