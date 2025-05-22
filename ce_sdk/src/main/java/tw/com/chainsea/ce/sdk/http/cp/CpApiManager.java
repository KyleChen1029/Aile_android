package tw.com.chainsea.ce.sdk.http.cp;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.request.CpAccountCheckCodeSendRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpAccountDeviceRecordRememberAddRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpAccountDeviceRecordRememberRemoveRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpAccountLoginCheckCodeValidateRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpAccountLoginDeviceAgreeRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpAccountLoginDeviceRejectRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpAccountLoginDeviceScanRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpDeleteRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpGetInvitationCodeRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpJoinTenantRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpLoginDeviceCheckRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpLoginRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpLogoutRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpRegisterRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantDictionaryIndustryRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantDictionaryScaleRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantGuarantorAddRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantGuarantorAgreeRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantGuarantorCancelRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantGuarantorRejectRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantItemRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantRelationListRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantSupportFileUploadRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantTransActiveRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantTransCreateRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantTransDismissRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantTransJoinRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantTransMemberExitRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantTransMemberJoinAgreeRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantTransMemberJoinRejectRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantTransMemberRemoveRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantUpdateRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTenantUpgradeRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTokenAnewRequest;
import tw.com.chainsea.ce.sdk.http.cp.request.CpTokenRefreshRequest;

public class CpApiManager {
    private static CpApiManager instance;
    public static synchronized CpApiManager getInstance() {
        if (instance == null) {
            instance = new CpApiManager();
        }
        return instance;
    }
    public void login(Context context, String countryCode, String mobile, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("type", "3") //手機+OTP登入固定為3
                    .put("countryCode", countryCode)
                    .put("mobile", mobile);
            new CpLoginRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void logout(Context context, @Nullable CpApiListener<String> listener) {
        try {
            new CpLogoutRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(new JSONObject());
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void register(Context context, String mobile, String countryCode, String checkCode, String name, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("mobile", mobile)
                    .put("countryCode", countryCode)
                    .put("checkCode", checkCode)
                    .put("from", "APP")
                    .put("name", name);
            new CpRegisterRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void tokenAnew(Context context, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            new CpTokenAnewRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void tokenRefresh(Context context, String tokenId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("refreshTokenId", tokenId);
            new CpTokenRefreshRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void sendCheckCode(Context context, String countryCode, String mobile, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("countryCode", countryCode)
                    .put("mobile", mobile);
            new CpAccountCheckCodeSendRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void loginCheckCodeValidate(Context context, String onceToken, String checkCode, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("onceToken", onceToken)
                    .put("checkCode", checkCode);
            new CpAccountLoginCheckCodeValidateRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void loginDeviceScan(Context context, String onceToken, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("onceToken", onceToken);
            new CpAccountLoginDeviceScanRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void loginDeviceAgree(Context context, String onceToken, boolean isRemember, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("onceToken", onceToken)
                    .put("rememberMe", isRemember);
            new CpAccountLoginDeviceAgreeRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void loginDeviceReject(Context context, String onceToken, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("onceToken", onceToken);
            new CpAccountLoginDeviceRejectRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void loginDeviceCheck(Context context, String onceToken, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("onceToken", onceToken);
            new CpLoginDeviceCheckRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void deviceRecordRememberAdd(Context context, String deviceId) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("id", deviceId);
            new CpAccountDeviceRecordRememberAddRequest(context, null)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void deviceRecordRememberRemove(Context context, String deviceId) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("id", deviceId);
            new CpAccountDeviceRecordRememberRemoveRequest(context, null)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void createTenantTrans(Context context, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            new CpTenantTransCreateRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void dismissTenantTrans(Context context, String tenantId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("tenantId", tenantId);
            new CpTenantTransDismissRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void joinTenantTrans(Context context, String tenantId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("tenantId", tenantId);
            new CpTenantTransJoinRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void activeTenantTrans(Context context, String tenantId, String name, String description, String industry, String avatarPath, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("tenantId", tenantId)
                    .put("name", name)
                    .put("description", description)
                    .put("industry", industry);
            new CpTenantTransActiveRequest(context, listener)
                    .setMainThreadEnable(true)
                    .requestWithFile(jsonObject, avatarPath);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void agreeTenantTransMemberJoin(Context context, String tenantId, String memberId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("tenantId", tenantId)
                    .put("memberId", memberId);
            new CpTenantTransMemberJoinAgreeRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void rejectTenantTransMemberJoin(Context context, String tenantId, String memberId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("tenantId", tenantId)
                    .put("memberId", memberId);
            new CpTenantTransMemberJoinRejectRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void removeTenantTransMember(Context context, String tenantId, String memberId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("tenantId", tenantId)
                    .put("memberId", memberId);
            new CpTenantTransMemberRemoveRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void exitTenantTransMember(Context context, String tenantId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("tenantId", tenantId);
            new CpTenantTransMemberExitRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }
    public void getTenantRelationList(Context context, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            new CpTenantRelationListRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantGuarantorAdd(Context context, String tenantCode, String accountId,  @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tenantCode", tenantCode);
            jsonObject.put("accountId", accountId);
            new CpTenantGuarantorAddRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantGuarantorAgree(Context context, String onceToken, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("onceToken", onceToken);
            new CpTenantGuarantorAgreeRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantGuarantorReject(Context context, String onceToken, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("onceToken", onceToken);
            new CpTenantGuarantorRejectRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantGuarantorCancel(Context context, String tenantCode, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tenantCode", tenantCode);
            new CpTenantGuarantorCancelRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantDictionaryIndustry(Context context, String tokenId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tokenId", tokenId);
            new CpTenantDictionaryIndustryRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantDictionaryScale(Context context, String tokenId, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tokenId", tokenId);
            new CpTenantDictionaryScaleRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantItem(Context context, String tenantCode, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tenantCode", tenantCode);
            new CpTenantItemRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantUpdate(Context context, String tenantId, String tenantName, String description, String path,
                              @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tenantId", tenantId);
            jsonObject.put("tenantName", tenantName);
            jsonObject.put("description", description);
            new CpTenantUpdateRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantSupportFileUpload(Context context, String tenantId, String path,
                             @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tenantId", tenantId);
            new CpTenantSupportFileUploadRequest(context, listener)
                    .setMainThreadEnable(true)
                    .requestWithFile(jsonObject, path);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void tenantUpgrade(Context context, String tenantId, String tenantName,
                              String description, String industry, String scale,
                              String supportFileId, String path,
                              @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tenantId", tenantId);
            jsonObject.put("tenantName", tenantName);
            jsonObject.put("description", description);
            jsonObject.put("industry", industry);
            jsonObject.put("scale", scale);
            jsonObject.put("supportFileId", supportFileId);
            new CpTenantUpgradeRequest(context, listener)
                    .setMainThreadEnable(true)
                    .requestWithFile(jsonObject, path);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void delete(Context context, String reason, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("reason", reason);
            new CpDeleteRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void getInvitationCode(Context context, String tenantCode, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tenantCode", tenantCode);
            new CpGetInvitationCodeRequest(context, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch(Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void joinTenantByInvitationCode(Context ctx, String invitationCode, @Nullable CpApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("invitationCode", invitationCode);
            new CpJoinTenantRequest(ctx, listener)
                    .setMainThreadEnable(true)
                    .request(jsonObject);
        } catch(Exception e) {
            CELog.e(e.getMessage());
        }
    }

}