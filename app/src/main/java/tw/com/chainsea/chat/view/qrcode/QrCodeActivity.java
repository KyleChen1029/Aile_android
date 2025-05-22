package tw.com.chainsea.chat.view.qrcode;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.event.SocketEventEnum;
import tw.com.chainsea.ce.sdk.socket.cp.model.GuarantorJoinContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ActivityQrCodeBinding;
import tw.com.chainsea.chat.view.BaseActivity;

public class QrCodeActivity extends BaseActivity {
    private ActivityQrCodeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_qr_code);
    }


    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketEvent socketEvent){
        super.onEvent(socketEvent);
        if (socketEvent.getType() == SocketEventEnum.GuarantorJoin) {
            GuarantorJoinContent guarantorJoinContent = (GuarantorJoinContent) socketEvent.getData();
            guarantorJoin(guarantorJoinContent);
        }
    }
}
