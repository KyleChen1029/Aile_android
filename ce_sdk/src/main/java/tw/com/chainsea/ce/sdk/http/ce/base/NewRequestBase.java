package tw.com.chainsea.ce.sdk.http.ce.base;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tw.com.chainsea.android.common.client.callback.impl.NativeCallback;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.network.NetworkHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.request.TokenApplyRequest;
import tw.com.chainsea.ce.sdk.http.common.model.DeviceData;
import tw.com.chainsea.ce.sdk.http.cp.respone.RefreshTokenResponse;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * current by evan on 2020-03-13
 *
 * 1、If it becomes an unnecessary main thread, the UI main thread is not used to avoid IO blockage
 * 2、Decoupling and using a third-party package okhttp package leads to the problem that the library cannot be packaged twice.
 *
 * @author Evan Wang
 * @version 1.10.0
 * date 2020-03-13
 */
public abstract class NewRequestBase {
    protected static final String TAG = NewRequestBase.class.getSimpleName();
    protected String URL;
    protected String sToken;
    protected JSONObject mJSONObject = null;
    protected String filePath = null;
    protected Context ctx;
    protected boolean mainThreadEnable = true;
    protected boolean isAsync = true;
    protected String userId;

    public NewRequestBase(Context ctx, String path) {
        if (sToken == null) {
            sToken = TokenPref.getInstance(ctx).getTokenId();
        }
        AppConfig.tokenForNewAPI = sToken;
        this.ctx = ctx;
        this.URL = TokenPref.getInstance(this.ctx).getCurrentTenantUrl() + ApiPath.ROUTE + path;
        userId = TokenPref.getInstance(ctx).getUserId();
    }

//    protected void setPath(String path) {
//        this.URL = TokenPref.getInstance(this.ctx).getCurrentTenantUrl() + ApiPath.ROUTE + path;
//    }
//
//    protected void replaceUrl(String url) {
//        this.URL = url;
//    }

    public NewRequestBase setMainThreadEnable(boolean mainThreadEnable) {
        this.mainThreadEnable = mainThreadEnable;
        return this;
    }

    public NewRequestBase setAsync(boolean isAsync) {
        this.isAsync = isAsync;
        return this;
    }

    public void request() {
        request(new JSONObject());
    }

//    public void emptyRequest() {
//        String startUser = TokenPref.getInstance(ctx).getUserId(); //workround for logout
//        String taskName = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
//        String taskValue = Integer.toHexString(hashCode());
//        ApiManager.addRequestTask(taskName, taskValue);
//        CELog.d("[CE API] request URL: " + URL);
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("deviceData", JsonHelper.getInstance().toJson(new DeviceData()));
//
//        ClientsHelper.post(this.isAsync).execute(this.URL, Media.JSON_UTF8.get(), headers, "", new NativeCallback(this.mainThreadEnable) {
//            @Override
//            public void onSuccess(String s) {
//                if(!startUser.equals(TokenPref.getInstance(ctx).getUserId())){
//                    CELog.w("startUser " + startUser + " != " + "after user " + TokenPref.getInstance(ctx).getUserId());
//                    return; //workround for logout
//                }
//                try {
//                    CELog.d("[CE API] + "+ URL + " return Body: " + s);
//                    JSONObject result;
//                    try {
//                        result = new JSONObject(s);
//                    } catch (Exception e) {
//                        success(null, s);
//                        return;
//                    }
//                    JSONObject header = result.getJSONObject("_header_");
//                    if (header.getBoolean("success")) {
//                        success(result, s);
//                    } else {
//                        CELog.d("[CE API] return error " + s);
//                        String errorCode = header.getString("errorCode");
//                        ErrCode code = ErrCode.of(errorCode);
//                        String errorMessage = header.getString("errorMessage");
//                        if (this.mainThreadEnable) {
//                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(code, errorMessage));
//                        } else {
//                            failed(code, errorMessage);
//                        }
//
//                    }
//                } catch (JSONException e) {
//                    CELog.e("[CE API] JSONException + ", e);
//                    if (this.mainThreadEnable) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.JSON_PARSE_FAILED, s));
//                    } else {
//                        failed(ErrCode.JSON_PARSE_FAILED, s);
//                    }
//                } finally {
//                    ApiManager.removeRequestTask(taskName);
//                }
//
//            }
//
//            @Override
//            public void onFailure(Exception e, String errorMsg) {
//                ApiManager.removeRequestTask(taskName);
//                if(!startUser.equals(TokenPref.getInstance(ctx).getUserId())){
//                    CELog.w("startUser " + startUser + " != " + "after user " + TokenPref.getInstance(ctx).getUserId());
//                    return; //workround for logout
//                }
//                if(e == null || e.getMessage() == null) {
//                    failed(ErrCode.of("-1"), "exception is null");
//                    return;
//                }
//                if (e.getMessage().contains("Only the original thread") || e.getMessage().contains("thread that has not called Looper.prepare()")) {
//                    CELog.d("[CE API] " + String.format("%s::: %s::: \n %s", "wrong Main Thread！！！！！", URL, e.getMessage()));
//                    if (this.mainThreadEnable) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), e.getMessage()));
//                    } else {
//                        failed(ErrCode.of("-1"), e.getMessage());
//                    }
//                    throw new RuntimeException();
//                }
//
//                String error = e.toString();
//                CELog.d("[CE API] response failed from " + URL);
//                CELog.d("[CE API] error message : " + error);
//                String errMsg = "ce:"+errorMsg;
//                if (error.contains("TimeoutError")) {
//                    errMsg = ctx.getString(R.string.api_request_timed_out);
//                } else if (error.contains("java.net.ConnectException: Failed to connect to")) {
//                    errMsg = ctx.getString(R.string.api_connection_timed_out);
//                }
//
//                if (!Strings.isNullOrEmpty(errMsg)) {
//                    if (this.mainThreadEnable) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), "ce:"+errorMsg));
//                    } else {
//                        failed(ErrCode.of("-1"), errMsg);
//                    }
//                }
//            }
//        });
//    }

    public void request(JSONObject jsonObject) {
        this.mJSONObject = jsonObject;
        if (!NetworkHelper.hasNetWork(ctx)) {
            String errMsg = ctx.getString(R.string.api_there_no_internet_connection);
            if (this.mainThreadEnable) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), errMsg));
            } else {
                failed(ErrCode.of("-1"), errMsg);
            }
            return;
        }
        if (!Strings.isNullOrEmpty(sToken)) {
            try {
                jsonObject.put("_header_", new JSONObject()
                        .put("tokenId", sToken)
                        .put("language", AppConfig.LANGUAGE));
            } catch (JSONException e) {
                CELog.e("construct header failed");
            }
        } else {
            try {
                jsonObject.put("_header_", new JSONObject()
                        .put("language", AppConfig.LANGUAGE));
            } catch (JSONException e) {
                CELog.e("construct header failed");
            }
        }
        CELog.d("[CE API] " + URL + " request body: " + jsonObject);

        Map<String, String> headers = new HashMap<>();
        headers.put("deviceData", JsonHelper.getInstance().toJson(new DeviceData()));

        String startUser = TokenPref.getInstance(ctx).getUserId(); //workround for logout
        String taskName = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        String taskValue = Integer.toHexString(hashCode());
        ApiManager.addRequestTask(taskName, taskValue);

        ClientsHelper.post(this.isAsync).execute(this.URL, Media.JSON_UTF8.get(), headers, jsonObject.toString(), new NativeCallback(this.mainThreadEnable) {
            @Override
            public void onSuccess(String s) {
                CELog.d("[CE API] "+ URL + " return Body: " + s);

                if(!startUser.equals(TokenPref.getInstance(ctx).getUserId())){
                    CELog.e("startUser " + startUser + " != " + "after user " + TokenPref.getInstance(ctx).getUserId());
                    return; //workround for logout
                }
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
                        String errorCode = header.getString("errorCode");
                        ErrCode code = ErrCode.of(errorCode);
                        if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
                            refreshToken();
                        } else if (ErrCode.USER_SQUEEZED_OUT.equals(code)) {
                            userSqueezedOut();
                        } else {
                            String errorMessage = header.getString("errorMessage");
                            if (this.mainThreadEnable) {
                                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(code, errorMessage));
                            } else {
                                failed(code, errorMessage);
                            }
                        }
                    }
                } catch (JSONException e) {
                    CELog.e("[CE API] JSONException "+ e);
                    if (this.mainThreadEnable) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.JSON_PARSE_FAILED, s));
                    } else {
                        failed(ErrCode.JSON_PARSE_FAILED, s);
                    }
                } finally {
                    ApiManager.removeRequestTask(taskName);
                }
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                ApiManager.removeRequestTask(taskName);
                if(!startUser.equals(TokenPref.getInstance(ctx).getUserId())){
                    CELog.d("[CE API] startUser " + startUser + " != " + "after user " + TokenPref.getInstance(ctx).getUserId());
                    return; //workround for logout
                }
                if(e == null || e.getMessage() == null) {
                    failed(ErrCode.of("-1"), "exception is null");
                    return;
                }
                if (e.getMessage().contains("Only the original thread") || e.getMessage().contains("thread that has not called Looper.prepare()")) {
                    CELog.d("[CE API] " + String.format("%s::: %s::: \n %s", "wrong Main Thread！！！！！", URL, e.getMessage()));
                    if (this.mainThreadEnable) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), e.getMessage()));
                    } else {
                        failed(ErrCode.of("-1"), e.getMessage());
                    }
//                    throw new RuntimeException();
                }

                String error = e.toString();
                CELog.d("[CE API] "+ URL + " failed error message : " + error);
                String errMsg = "ce:"+errorMsg;
                if (error.contains("TimeoutError")) {
                    errMsg = ctx.getString(R.string.api_request_timed_out);
                } else if (error.contains("java.net.ConnectException: Failed to connect to")) {
                    errMsg = ctx.getString(R.string.api_connection_timed_out);
                }

                if (!Strings.isNullOrEmpty(errMsg)) {
                    if (this.mainThreadEnable) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), "ce:"+errorMsg));
                    } else {
                        failed(ErrCode.of("-1"), errMsg);
                    }
                }
            }
        });
    }

//    private JSONObject getDeviceDataObject(){
//        try {
//            return new JSONObject(JsonHelper.getInstance().toJson(new DeviceData()));
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public void postFile(JSONObject jsonObject, String filePath) {
        this.filePath = filePath;
        this.mJSONObject = jsonObject;
        if (!Strings.isNullOrEmpty(sToken)) {
            try {
                jsonObject.put("_header_", new JSONObject()
                        .put("tokenId", sToken)
                        .put("language", AppConfig.LANGUAGE));
            } catch (JSONException e) {
                CELog.e("[CE API] + construct header failed:" + e);
            }
        } else {
            try {
                jsonObject.put("_header_", new JSONObject()
                        .put("language", AppConfig.LANGUAGE));
            } catch (JSONException e) {
                CELog.e("[CE API] + construct header failed:" + e);
            }
        }
        CELog.d("[CE API] " + URL + " request body: " + jsonObject);

        Map<String, String> formDataPart = Maps.newHashMap(ImmutableMap.of("args", jsonObject.toString()));
        File files = new File(filePath);
        String taskName = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        String taskValue = Integer.toHexString(hashCode());
        ApiManager.addRequestTask(taskName, taskValue);

        Map<String, String> headers = new HashMap<>();
        headers.put("deviceData", JsonHelper.getInstance().toJson(new DeviceData()));

        ClientsHelper.post(this.isAsync).execute(this.URL, Media.OCTET_STREAM.get(), headers, formDataPart, "file", files, new NativeCallback(this.mainThreadEnable) {
            @Override
            public void onSuccess(String resp) {
                CELog.d("[CE API] + "+ URL + " return Body: " + resp);
                try {
                    JSONObject result;
                    try {
                        result = new JSONObject(resp);
                    } catch (Exception e) {
                        success(null, resp);
                        return;
                    }
                    JSONObject header = result.getJSONObject("_header_");
                    if (header.getBoolean("success")) {
                        success(result, resp);
                    } else {
                        CELog.d("request error " + resp);
                        String errorCode = header.getString("errorCode");
                        ErrCode code = ErrCode.of(errorCode);
                        if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
                            refreshToken();
                        } else if (ErrCode.USER_SQUEEZED_OUT.equals(code)) {
                            userSqueezedOut();
                        } else {
                            String errorMessage = header.getString("errorMessage");
                            failed(code, errorMessage);
                        }
                    }
                } catch (JSONException e) {
                    CELog.e("[CE API] response parse failed " + e);
                    failed(ErrCode.JSON_PARSE_FAILED, resp);
                } finally {
                    ApiManager.removeRequestTask(taskName);
                }
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                ApiManager.removeRequestTask(taskName);
                String error = e.toString();
                CELog.d("[CE API] "+ URL + " failed error message : " + error);
                if (error.contains("TimeoutError")) {
                    failed(ErrCode.of("-1"), ctx.getString(R.string.api_request_timed_out));
                } else if (error.contains("java.net.ConnectException: Failed to connect to")) {
                    failed(ErrCode.of("-1"), ctx.getString(R.string.api_connection_timed_out));
                } else {
                    CELog.e(errorMsg);
                    failed(ErrCode.of("-1"), "ce:"+errorMsg);
                }
            }
        });
    }

    protected void refreshToken() {
        ApiManager.getInstance().refreshToken(ctx, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                sToken = TokenPref.getInstance(ctx).getTokenId();
                if (!Strings.isNullOrEmpty(filePath)) {
                    postFile(mJSONObject, filePath);
                } else {
                    request(mJSONObject);
                }
            }
        });
    }


//    protected JSONObject getReqData() {
//        return this.mJSONObject;
//    }

    protected abstract void success(JSONObject jsonObject, String s) throws JSONException;
//    protected void success(File file) {
//        CELog.d("");
//    }

    protected void success(File file, PicSize size) {
        CELog.d("");
    }

    protected abstract void failed(ErrCode code, String errorMessage);

    protected void userSqueezedOut() {
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.TOKEN_INVALID_FILTER));
    }
}
