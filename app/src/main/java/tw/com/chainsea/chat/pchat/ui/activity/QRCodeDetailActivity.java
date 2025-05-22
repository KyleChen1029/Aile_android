package tw.com.chainsea.chat.pchat.ui.activity;

import androidx.fragment.app.Fragment;
import android.view.View;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.pchat.ui.fragment.QRCodeDetailFragment;
import tw.com.chainsea.chat.ui.activity.FragmentActivity;

public class QRCodeDetailActivity extends FragmentActivity {
    public final String TAG = "QRCodeDetailActivity";
    private String serviceNumberId;
    private String serviceName;

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    protected Fragment createFragment() {
        serviceNumberId = getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
        serviceName = getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_NAME.key());
        return QRCodeDetailFragment.newInstance(serviceNumberId);
    }

    @Override
    protected View rightView() {
        return null;
    }

    @Override
    protected String getTitleText() {
        return "二維碼名片";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }
}
