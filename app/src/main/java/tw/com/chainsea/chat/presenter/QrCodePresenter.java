package tw.com.chainsea.chat.presenter;

import android.content.Context;

import org.json.JSONObject;

import java.util.Objects;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.chat.ui.ife.IQrCodeView;

public class QrCodePresenter {

    private final IQrCodeView iView;

    public QrCodePresenter(IQrCodeView iView) {
        this.iView = iView;
    }

    public void getInvitationCode(Context ctx) {

        CpApiManager.getInstance().getInvitationCode(ctx, TokenPref.getInstance(ctx).getTenantCode(), new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                try {
                    JSONObject jsonObject = JsonHelper.getInstance().toJsonObject(s);
                    String text = jsonObject.getString("text");
                    iView.onInvitationCodeComplete(text);
                } catch(Exception e) {
                    CELog.e("getInvitationCode onSuccess error=" +e.getMessage());
                    iView.onError(Objects.requireNonNull(e.getMessage()));
                }
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                CELog.e("getInvitationCode onFailed=" +errorCode +", "+ errorMessage);
                iView.onError(errorMessage);
            }
        });

    }
}
