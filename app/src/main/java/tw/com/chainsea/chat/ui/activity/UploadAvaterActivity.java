package tw.com.chainsea.chat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.ui.fragment.UploadAvaterFragment;

public class UploadAvaterActivity extends FragmentActivity {
    public final String TAG = "UploadAvaterActivity";
    private UploadAvaterFragment mUploadAvaterFragment;
    private Bundle mBundle;
    private String mNickName;

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    protected Fragment createFragment() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            mBundle = intent.getExtras();
            mNickName = intent.getStringExtra(Constant.NICKNAME);
        }
        mUploadAvaterFragment = new UploadAvaterFragment(mBundle, mNickName);
        return mUploadAvaterFragment;
    }

    @Override
    protected View rightView() {
        return Tools.createRightView(this, R.string.alert_complete);
    }

    @Override
    protected void rightAction() {
        super.rightAction();
        mUploadAvaterFragment.upload();
    }

    @Override
    protected String getTitleText() {
        return "請完善個人資訊";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

}
