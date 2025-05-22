package tw.com.chainsea.chat.view.group;


import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_ID;
import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_INFO;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ActivityWaitConfirmBinding;
import tw.com.chainsea.chat.view.BaseActivity;

public class GroupWaitConfirmActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWaitConfirmBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_wait_confirm);
        try {
            Glide.with(this)
                .asGif()
                .load(R.raw.loading)
                .into(binding.imgLoading);
        } catch (Exception ignored) {
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketEvent socketEvent) {
        super.onEvent(socketEvent);
        switch (socketEvent.getType()) {
            case TransTenantJoinReject:
                Toast.makeText(this, "被拒絕加入", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case TransTenantMemberAdd:
            case TransTenantJoinAgree:
                TransTenantInfo info = (TransTenantInfo) socketEvent.getData();
                TokenPref.getInstance(this).setCpTransTenantInfo(info);
                agreeJoin();
                break;
            case TransTenantDismiss:
                dismissTenantTrans();
                break;
        }
    }

    private void agreeJoin() {
        Intent intent = new Intent(this, GroupWaitActivity.class);
        startActivity(intent);
        finish();
    }


    private void dismissTenantTrans() {
        TokenPref.getInstance(this).clearByKey(CP_TRANS_TENANT_INFO);
        TokenPref.getInstance(this).clearByKey(CP_TRANS_TENANT_ID);
        finish();
    }
}
