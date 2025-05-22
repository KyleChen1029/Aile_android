package tw.com.chainsea.ce.sdk.http.cp.base;

import static tw.com.chainsea.ce.sdk.http.cp.CpApiPath.FORMAL_SERVER;

import android.content.Context;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import tw.com.chainsea.android.common.client.callback.impl.NativeCallback;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.hash.AESHelper;
import tw.com.chainsea.android.common.hash.HMacHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.network.NetworkHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventMessage;
import tw.com.chainsea.ce.sdk.http.common.model.DeviceData;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public abstract class CpNewRequestBase {
    protected static final String TAG = CpNewRequestBase.class.getSimpleName();
    public static String BASE_URL = FORMAL_SERVER; //正式區
    protected String URL;
    protected JSONObject originJsonObject;
    protected Context ctx;
    protected boolean mainThreadEnable = true;
    protected boolean isAsync = true;

    public CpNewRequestBase(Context ctx, String path) {
        this.ctx = ctx;
        this.URL = BASE_URL + path;
    }

    public CpNewRequestBase setMainThreadEnable(boolean mainThreadEnable) {
        this.mainThreadEnable = mainThreadEnable;
        return this;
    }

    public CpNewRequestBase setAsync(boolean isAsync) {
        this.isAsync = isAsync;
        return this;
    }

    public void request(JSONObject jsonObject) {
        requestFlow(jsonObject, null);
    }

    public void requestWithFile(JSONObject jsonObject, String filePath) {
        requestFlow(jsonObject, filePath);
    }

    private void requestFlow(JSONObject jsonObject, String filePath) {
        originJsonObject = jsonObject;
        if (!NetworkHelper.hasNetWork(ctx)) {
            String errMsg = ctx.getString(R.string.api_there_no_internet_connection);
            if (this.mainThreadEnable) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), errMsg));
            } else {
                failed(ErrCode.of("-1"), errMsg);
            }
            return;
        }

        String token = TokenPref.getInstance(ctx).getCpTokenId();
        try {
            if (token != null) {
                jsonObject.put("_header_", new JSONObject()
                    .put("tokenId", token)
                    .put("language", AppConfig.LANGUAGE));
            } else {
                jsonObject.put("_header_", new JSONObject()
                    .put("language", AppConfig.LANGUAGE));
            }
        } catch (JSONException e) {
            CELog.e("construct header failed");
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("deviceData", JsonHelper.getInstance().toJson(new DeviceData()));

        JSONObject data = new JSONObject();

        try {
            String encryptData = AESHelper.encryptBase64(jsonObject.toString());
//            if(DEBUG) Log.d("API AES+BASE 64", encryptData);
            data.put("data", encryptData);
            headers.put("x-aile-siguare", HMacHelper.encryptHmac256Base64(encryptData));
//            if(DEBUG) Log.d("HmacSHA256+Base64", HMacHelper.encryptHmac256Base64(encryptData));
        } catch (JSONException ignored) {
        }
        CELog.d("[CP API] request URL: " + URL);
        CELog.d("[CP API] request Heads :" + headers);
        CELog.d("[CP API] request Body :" + jsonObject);

        if (filePath == null) { //直接Post
            ClientsHelper.post(this.isAsync).execute(this.URL, headers, data.toString(), nativeCallback);
        } else { //Post時也上傳檔案
            Map<String, String> formDataPart = Maps.newHashMap(ImmutableMap.of("args", data.toString()));
            File files = new File(filePath);
            ClientsHelper.post(this.isAsync).execute(this.URL, Media.findByFileType(filePath).get(), headers, formDataPart, "file", files, nativeCallback);
        }
    }

    private final NativeCallback nativeCallback = new NativeCallback(mainThreadEnable) {
        @Override
        public void onSuccess(String s) {
            try {
                JSONObject result;
                String decode;
                try {
                    decode = AESHelper.decryptBase64(s);
                    CELog.d("[CP API] return URL: " + URL);
                    CELog.d("[CP API] return Body : " + decode);

                    result = new JSONObject(decode);
                } catch (Exception e) {
                    success(null, s);
                    return;
                }
                JSONObject header = result.getJSONObject("_header_");
                if (header.getBoolean("success")) {
                    if ("0000".equals(result.getString("status"))) {
                        success(result, decode);
                    } else {
                        String errorCode = result.getString("errorCode");
                        String errorMessage = result.getString("errorMessage");
                        //Token 過期刷新Token
                        if (ErrCode.TOKEN_INVALID.getValue().equals(errorCode)) {
                            String refreshTokenId = TokenPref.getInstance(ctx).getCpRefreshTokenId();
                            CpApiManager.getInstance().tokenRefresh(ctx, refreshTokenId, new CpApiListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    try {
                                        JSONObject result = new JSONObject(s);
                                        TokenPref.getInstance(ctx).setCpTokenId(result.getString("tokenId"));
                                        request(originJsonObject);
                                    } catch (JSONException e) {
                                        failed(ErrCode.JSON_PARSE_FAILED, e.getMessage());
                                    }
                                }

                                @Override
                                public void onFailed(String tokenErrorCode, String tokenErrorMessage) {
                                    CELog.d("[CP API] Token refresh Error : " + tokenErrorMessage);
                                    failed(ErrCode.of(errorCode), errorMessage);
                                }
                            });
                        } else if (ErrCode.CP_REFRESH_TOKEN_NOT_EXIST.getValue().equals(errorCode)
                            || ErrCode.CP_REFRESH_TOKEN_EXPIRED.getValue().equals(errorCode)) {
                            EventBus.getDefault().post(new EventMessage(errorCode));
                            failed(ErrCode.of(errorCode), errorMessage);
                        } else if (ErrCode.CP_SQUEEZED_OUT.getValue().equals(errorCode)) {
                            EventBus.getDefault().post(new EventMessage(errorCode));
                            failed(ErrCode.of(errorCode), errorMessage);
                        } else {
                            failed(ErrCode.of(errorCode), errorMessage);
                        }
                    }
                } else {
                    CELog.d("[CP API] Error : " + s);
                    String errorCode = header.getString("errorCode");
                    String errorMessage = header.getString("errorMessage");
                    ErrCode code = ErrCode.of(errorCode);
                    if (this.mainThreadEnable) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(code, errorMessage));
                    } else {
                        failed(code, errorMessage);
                    }
                }
            } catch (JSONException e) {
                CELog.d("[CP API] Error : " + e);
                if (this.mainThreadEnable) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.JSON_PARSE_FAILED, s));
                } else {
                    failed(ErrCode.JSON_PARSE_FAILED, s);
                }
            }
        }

        @Override
        public void onFailure(Exception e, String errorMsg) {
            if (e.getMessage() != null && (e.getMessage().contains("Only the original thread") || e.getMessage().contains("thread that has not called Looper.prepare()"))) {
                CELog.d("[CP API] Error : " + String.format("%s::: %s::: \n %s", "wrong Main Thread！！！！！", URL, e.getMessage()));
                if (this.mainThreadEnable) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), e.getMessage()));
                } else {
                    failed(ErrCode.of("-1"), e.getMessage());
                }
                throw new RuntimeException();
            }

            String error = e.toString();
            CELog.d("[CP API] Error : " + error);
            String errMsg;
            if (error.contains("TimeoutError")) {
                errMsg = ctx.getString(R.string.api_request_timed_out);
            } else if (error.contains("java.net.ConnectException: Failed to connect to")) {
                errMsg = ctx.getString(R.string.api_connection_timed_out);
            } else {
                CELog.e("[CP API] Error : http failed: There is no Internet connection");
                errMsg = ctx.getString(R.string.api_there_no_internet_connection);
                failed(ErrCode.of("-1"), errorMsg);
            }

            if (!Strings.isNullOrEmpty(errMsg)) {
                if (this.mainThreadEnable) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), errMsg));
                } else {
                    failed(ErrCode.of("-1"), errMsg);
                }
            }
        }
    };

    protected abstract void success(JSONObject jsonObject, String s) throws JSONException;

    protected abstract void failed(ErrCode code, String errorMessage);
}
