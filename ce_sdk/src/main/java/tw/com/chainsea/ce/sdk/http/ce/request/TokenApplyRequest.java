package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.client.callback.impl.NativeCallback;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.database.sp.TenantPref;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * current by evan on 2020-10-22
 *
 * @author Evan Wang
 * date 2020-10-22
 */
public class TokenApplyRequest extends NewRequestBase {
    private boolean isRefresh;
    private final Listener listener;
    private String startUser;

    private final String selfUserId = TokenPref.getInstance(ctx).getUserId();

    public TokenApplyRequest(Context ctx, Listener listener) {
        super(ctx, "/" + ApiPath.tokenApply);
        startUser = selfUserId; //workround for logout
        TokenPref.init(ctx);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        AileTokenApply.Resp resp = JsonHelper.getInstance().from(s, AileTokenApply.Resp.class);
        if (!startUser.equals(selfUserId)) {
            CELog.w("startUser " + startUser + " != " + "after user " + selfUserId);
            return; //workround for logout
        }
        if (resp == null) {
            CELog.d("[CE API] Token resp == null :::: " + s);
        } else {
            TokenPref.getInstance(ctx).clearByKey(TokenPref.PreferencesKey.USER_ID);
            TokenPref.getInstance(ctx).clearByKey(TokenPref.PreferencesKey.THEME_ITEM);
            if (!Strings.isNullOrEmpty(resp.getBossServiceNumberId()))
                TokenPref.getInstance(ctx).setBossServiceNumberId(resp.getBossServiceNumberId());

            String tokenId = resp.getTokenId();
            TokenPref.getInstance(ctx)
                .setTokenId(tokenId)
                .setCeTokenRefreshId(resp.getRefreshTokenId())
                .setDeviceId(resp.getDeviceId());

            if (resp.getUser() != null) {
                TokenPref.getInstance(ctx)
                    .setUserId(resp.getUser().getId())
                    .setMute(resp.getUser().isMute());
            }

            // re init user data
            UserPref.newInstance(ctx);
            String accountNumber = TokenPref.getInstance(ctx).getAccountNumber();
            String psw = TokenPref.getInstance(ctx).getPassword();
            String uuid = TokenPref.getInstance(ctx).getCurrentTenantId();
            TenantPref.newInstance(ctx, uuid)
                .setAccountID(uuid)
                .setAccountNumber(accountNumber)
                .setAccountPsw(psw);
            if (resp.getUser() != null) {
                UserPref.getInstance(ctx)
                    .setHasBindEmployee(resp.getUser().isHasBindEmployee())
                    .setHasBusinessSystem(resp.getUser().isHasBusinessSystem());
            }

            if (resp.getConfiguration() != null) {
                TokenPref.getInstance(ctx)
                    .setSocketIoUrl(resp.getConfiguration().getSocketIoUrl())
//                    .setSocketAckEnable(resp.getConfiguration().isEnableAck())
                    .setSocketIoNameSpace(resp.getConfiguration().getSocketIoNamespace())
                    .setConnectType(resp.getConfiguration().getConnectType());
                TokenPref.getInstance(ctx).setSystemUserName(resp.getConfiguration().getSystemUserName())
                    .setSystemUserAvatarId(resp.getConfiguration().getSystemUserAvatarId())
                    .setSystemUserId(resp.getConfiguration().getSystemUserId());

            } else {
                TokenPref.getInstance(ctx)
                    .clearByKey(TokenPref.PreferencesKey.SOCKET_IO_URL)
                    .clearByKey(TokenPref.PreferencesKey.SOCKET_ACK_ENABLE)
                    .clearByKey(TokenPref.PreferencesKey.SOCKET_IO_NAME_SPACE)
                    .clearByKey(TokenPref.PreferencesKey.CONNECT_TYPE);
            }
            if (resp.getTenantInfo() != null) {
                TokenPref.getInstance(ctx)
                    .setEnableCall(resp.getTenantInfo().isEnableCall())
                    .setTokenValidSecond(resp.getTenantInfo().getTokenValidSeconds() * 1000L);

                TokenPref.getInstance(ctx).setRetractValidMinute(
                    resp.getTenantInfo().getRetractValidMinute()
                );
                TokenPref.getInstance(ctx).setThemeItemInfo(
                    resp.getTenantInfo().getThemeItem()
                );
            }

            if (this.listener != null) {
                this.listener.onSuccess(isRefresh, resp);
                this.listener.allCallBack(isRefresh, true);
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (!startUser.equals(selfUserId)) {
            CELog.w("startUser " + startUser + " != " + "after user " + selfUserId);
            errorMessage += "startUser " + startUser + " != " + "after user " + selfUserId;
        }
        if (this.listener != null) {
            this.listener.onFailed(code, errorMessage);
            this.listener.allCallBack(isRefresh, false);
        }
    }

    @Override
    public TokenApplyRequest setMainThreadEnable(boolean mainThreadEnable) {
        super.setMainThreadEnable(mainThreadEnable);
        return this;
    }

    public void request(boolean isRefresh) {
        this.isRefresh = isRefresh;
        startUser = selfUserId; //workround for logout
        SharedPreferences sp = ctx.getSharedPreferences("aile_token", Context.MODE_PRIVATE);

        String reqJson;
        try {
            if ("AuthTokenLogin".equals(sp.getString(TokenPref.PreferencesKey.LOGIN_MODE.key, ""))) {
                reqJson = new JSONObject().put("_header_", new JSONObject().put("language", AppConfig.LANGUAGE))
                    .put("loginMode", sp.getString(TokenPref.PreferencesKey.LOGIN_MODE.key, ""))
                    .put("tenantCode", sp.getString(TokenPref.PreferencesKey.TENANT_CODE.key, ""))
                    .put("deviceType", sp.getString(TokenPref.PreferencesKey.DEVICE_TYPE.key, ""))
                    .put("deviceName", sp.getString(TokenPref.PreferencesKey.DEVICE_NAME.key, ""))
                    .put("osType", sp.getString(TokenPref.PreferencesKey.OS_TYPE.key, ""))
                    .put("uniqueID", sp.getString(TokenPref.PreferencesKey.UNIQUE_ID.key, ""))
                    .put("authToken", sp.getString(TokenPref.PreferencesKey.AUTH_TOKEN.key, ""))
                    .toString();
            } else {
                reqJson = new JSONObject().put("_header_", new JSONObject().put("language", AppConfig.LANGUAGE))
                    .put("countryCode", sp.getString(TokenPref.PreferencesKey.COUNTRY_CODE.key, ""))
                    .put("deviceName", sp.getString(TokenPref.PreferencesKey.DEVICE_NAME.key, ""))
                    .put("deviceType", sp.getString(TokenPref.PreferencesKey.DEVICE_TYPE.key, ""))
                    .put("loginMode", sp.getString(TokenPref.PreferencesKey.LOGIN_MODE.key, ""))
                    .put("loginName", sp.getString(TokenPref.PreferencesKey.ACCOUNT_NUMBER.key, ""))
                    .put("osType", sp.getString(TokenPref.PreferencesKey.OS_TYPE.key, ""))
                    .put("password", sp.getString(TokenPref.PreferencesKey.PASSWORD.key, ""))
                    .put("tenantCode", sp.getString(TokenPref.PreferencesKey.CURRENT_TENANT_CODE.key, ""))
                    .put("uniqueID", sp.getString(TokenPref.PreferencesKey.UNIQUE_ID.key, ""))
                    .toString();
            }
        } catch (JSONException ignored) {
            reqJson = "";
        }

        CELog.d("token apply current environment ::: " + TokenPref.getInstance(ctx.getApplicationContext()).getCurrentEnvironment().toString());
        CELog.d("[CE API] Token URL: " + URL);
        CELog.d("[CE API] Token request : " + reqJson);
        ClientsHelper.post(true).execute(URL, Media.JSON_UTF8.get(), reqJson, new NativeCallback(mainThreadEnable) {
            @Override
            public void onSuccess(String s) {
                CELog.d("[CE API] Token response : " + s);
                try {
                    JSONObject result;
                    try {
                        result = new JSONObject(s);
                    } catch (Exception e) {
                        success(null, s);
                        return;
                    }
                    JSONObject header = result.getJSONObject("_header_");
                    if (header.getBoolean("success")) {
                        success(result, s);
                    } else {
                        CELog.d("[CE API] Token error " + s);
                        String errorCode = header.getString("errorCode");
                        ErrCode code = ErrCode.of(errorCode);
                        if (ErrCode.USER_SQUEEZED_OUT.equals(code)) {
                            userSqueezedOut();
                        } else {
                            String errorMessage = header.getString("errorMessage");
                            if (mainThreadEnable) {
                                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(code, errorMessage));
                            } else {
                                failed(code, errorMessage);
                            }
                        }
                    }
                } catch (JSONException e) {
                    CELog.e("[CE API] Token response parse failed" + e);
                    if (mainThreadEnable) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.JSON_PARSE_FAILED, s));
                    } else {
                        failed(ErrCode.JSON_PARSE_FAILED, s);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                CELog.d("[CE API] Token errorMsg : " + errorMsg);
                if (errorMsg == null) {
                    return;
                }
                if (e.getMessage().contains("Only the original thread") || e.getMessage().contains("thread that has not called Looper.prepare()")) {
                    CELog.e(e.getMessage());
                    CELog.e(String.format("%s::: %s::: \n %s", "用錯主線程！！！！！", URL, e.getMessage()));
                    if (mainThreadEnable) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), e.getMessage()));
                    } else {
                        failed(ErrCode.of("-1"), e.getMessage());
                    }
                    throw new RuntimeException();
                }

                String error = e.toString();
                String errMsg = "";
                if (error.contains("TimeoutError")) {
                    errMsg = ctx.getString(R.string.api_request_timed_out);
                } else if (error.contains("java.net.ConnectException: Failed to connect to")) {
                    errMsg = ctx.getString(R.string.api_connection_timed_out);
                } else {
                    CELog.e(error);
                    errMsg = error;
                }

                if (!Strings.isNullOrEmpty(errMsg)) {
                    if (mainThreadEnable) {
                        String finalErrMsg = errMsg;
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), finalErrMsg));
                    } else {
                        failed(ErrCode.of("-1"), errMsg);
                    }
                }
            }
        });
    }

    public interface Listener {
        void allCallBack(boolean isRefresh, boolean status);

        void onSuccess(boolean isRefresh, AileTokenApply.Resp resp);

        void onFailed(ErrCode errorCode, String errorMessage);

        void onCallData(String roomId, String meetingId, String callKey);
    }
}
