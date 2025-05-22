package tw.com.chainsea.chat.presenter;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.ce.sdk.http.cp.respone.TenantRelationListResponse;
import tw.com.chainsea.chat.ui.ife.IScannerView;

public class ScannerPresenter {
    private final IScannerView iView;

    public ScannerPresenter(IScannerView view) {
        iView = view;
    }

    public void sendInvitationCodeToJoinTenant(Context ctx, String code) {
        iView.showLoading();
        CpApiManager.getInstance().joinTenantByInvitationCode(ctx, code, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                iView.dismissLoading();
//                JSONObject result;
//                String decode;
                try {
//                    decode = AESHelper.decryptBase64(s);
//                    result = new JSONObject(decode);

                    JSONObject header = JsonHelper.getInstance().toJsonObject(s);//result.getJSONObject("_header_");
                    JSONObject _header = header.getJSONObject("_header_");
                    if (_header.getBoolean("success"))
                        if ("0000".equals(header.getString("status")))
                            getTenantRelationList(ctx); //iView.onJoinSuccess();
                        else
                            iView.onJoinFailure("");

                } catch (Exception e) {
                    iView.onJoinFailure("");
                }
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                iView.dismissLoading();
                CELog.e("getInvitationCode onFailed=" + errorCode + ", " + errorMessage);
                iView.onJoinFailure(errorMessage);
            }
        });
    }

    private void getTenantRelationList(Context context) {
        iView.showLoading();
        CpApiManager.getInstance().getTenantRelationList(context, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                iView.dismissLoading();
                TenantRelationListResponse response = JsonHelper.getInstance().from(s, TenantRelationListResponse.class);
                if ("0000".equals(response.getStatus())) {
                    List<RelationTenant> newRelationTenants = response.getRelationTenantArray();
                    List<RelationTenant> oldRelationTenants = TokenPref.getInstance(context).getCpRelationTenantList();
                    int oldTenantsSize = 0;
                    if (oldRelationTenants != null)
                        oldTenantsSize = oldRelationTenants.size();
                    if (newRelationTenants.size() > oldTenantsSize) {
                        if (oldRelationTenants != null)
                            newRelationTenants.removeAll(oldRelationTenants);
                        List<RelationTenant> difference = Lists.newArrayList(newRelationTenants);
                        iView.onJoinSuccess(difference.size() == 1 ? difference.get(0) : difference.get(difference.size() - 1));
                    } else
                        iView.onJoinFailure("");
                } else
                    iView.onJoinFailure("");
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                iView.dismissLoading();
                Log.d("TAG", errorCode + errorMessage);
                iView.onJoinFailure(errorMessage);
            }
        });
    }
}
