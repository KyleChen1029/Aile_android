package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.request.CpRepairRequest;
import tw.com.chainsea.ce.sdk.http.cp.respone.RefreshTokenResponse;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * uploading manager
 * Created by 90Chris on 2015/12/1.
 */
public class UploadManager {
    private static UploadManager INSTANCE = new UploadManager();
    private String TAG = UploadManager.class.getSimpleName();
    private Handler mainHandler;
    private final String FILE = "file";

    public static UploadManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UploadManager();
        }
        return INSTANCE;
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient();
    }

    private JSONObject getUploadArgs(String token) {
        JSONObject requestJson = new JSONObject();
        if ((token != null) && (!token.isEmpty())) {
            JSONObject header = new JSONObject();
            try {
                header.put("tokenId", token);
                requestJson.put("_header_", header);
            } catch (JSONException e) {
                CELog.e("construct header failed");
            }
        }
        return requestJson;
    }

    private MultipartBody.Builder getBuilder(String token) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"args\""),
            RequestBody.create(null, getUploadArgs(token).toString()));
        return builder;
    }

    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;

        try {
            contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, "utf-8"));
        } catch (UnsupportedEncodingException ignored) {
        }

        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }

        return contentTypeFor;
    }

    private Handler getMainHandler(Context context) {
        mainHandler = new Handler(context.getMainLooper());
        return mainHandler;
    }

    private FileEntity requestBodyParser(Response response) throws IOException {
        FileEntity entity = JsonHelper.getInstance().from(response.body().string(), FileEntity.class);
        return entity;
    }

    public void uploadVoice(Context context, final String messageId, String token, String path, OnVoiceUploadListener listener) {

        File file = new File(path);

        RequestBody requestBody = getBuilder(token)
            .addFormDataPart(FILE, file.getName(),
                RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file))
            .build();

        Request request = new Request.Builder()
            .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.baseFileUpload)
            .post(requestBody)
            .build();

        mainHandler = getMainHandler(context);

        getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                // Handle the error
                if (listener != null) {
                    mainHandler.post(() -> {
                        listener.onUploadFailed(e.toString());
                    });
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    FileEntity entity = requestBodyParser(response);
                    if (entity.isHeightAndWidthError()) {
                        Log.e("isHeightAndWidthError:", "" + response);
                    }
                    if (listener != null) {
                        mainHandler.post(() -> {
                            listener.onUploadSuccess(messageId, entity);
                        });
                    }
                }
            }
        });
    }


    public void uploadFile(Context context, final String messageId, String token, String path, OnFileUploadListener listener) {
        File file = new File(path);
        final long totalSize = file.length();

        if (totalSize < 0) {
            return;
        }

        RequestBody requestBody = getBuilder(token)
            .addFormDataPart(FILE, file.getName(),
                new CountingFileRequest(file, guessMimeType(file.getName()), num -> {
                    if (listener != null) {
                        float progress = (num / (float) totalSize) * 100;
                        Log.i(TAG, "upload = " + progress);
                        mainHandler.post(() -> {
                            listener.onUploadIng(messageId, (int) progress, totalSize);
                        });
                    }
                })).build();

        Request request = new Request.Builder()
            .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.baseFileUpload)
            .post(requestBody)
            .build();

        mainHandler = getMainHandler(context);

        getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                if (listener != null) {
                    mainHandler.post(() -> {
                        listener.onUploadFailed(e.toString(), messageId);
                    });
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    FileEntity entity = requestBodyParser(response);
                    if (entity.isHeightAndWidthError()) {
                        Log.e("isHeightAndWidthError:", "" + response);
                    }
                    if (listener != null) {
                        mainHandler.post(() -> {
                            listener.onUploadSuccess(messageId, entity);
                        });
                    }
                }
            }
        });
    }

    public void uploadPic(Context context, String token, String path, OnPicUploadListener listener) {

        File file = new File(path);

        RequestBody requestBody = getBuilder(token)
            .addFormDataPart(FILE, file.getName(),
                RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file))
            .build();

        Request request = new Request.Builder()
            .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.basePictureUpload)
            .post(requestBody)
            .build();

        mainHandler = getMainHandler(context);

        getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                if (listener != null) {
                    mainHandler.post(() -> listener.onUploadFailed(e.toString()));
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            String picUrl = new JSONObject(response.body().string()).getString("url");
                            mainHandler.post(() -> listener.onUploadSuccess(picUrl));
                        }
                    } catch (Exception e) {
                        Log.e("uploadPic", "error:" + e.getMessage());
                    }
                }
            }
        });

    }

    public void uploadBusinessCard(Context context, String token, String accountId, final String path, final int x, final OnUploadAvatarListener listener) {
        JSONObject object = new JSONObject();
        try {
            JSONObject header = new JSONObject();
            header.put("tokenId", token);
            object.put("x", 0);
            object.put("y", 0);
            object.put("size", x);
            object.put("userId", accountId);
            header.put("language", AppConfig.LANGUAGE);
            object.put("_header_", header);
        } catch (JSONException ignored) {
        }

        File file = new File(path);

        RequestBody requestBody = getBuilder(token)
            .addFormDataPart("args", object.toString())
            .addFormDataPart(FILE, file.getName(),
                RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file))
            .build();

        Request request = new Request.Builder()
            .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.userHomePageUpdateCustomerProfileCustomer)
            .post(requestBody)
            .build();

        mainHandler = getMainHandler(context);

        getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                if (listener != null) {
                    mainHandler.post(() -> {
                        listener.onUploadFailed(e.toString());
                    });
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject header = jsonObject.getJSONObject("_header_");
                        if (header.getBoolean("success")) {
                            String url = jsonObject.getString("customerBusinessCardUrl");
                            mainHandler.post(() -> {
                                listener.onUploadSuccess(url);
                            });
                        } else {
                            String errorCode = header.getString("errorCode");
                            ErrCode code = ErrCode.of(errorCode);
                            if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
                                refreshToken(context, path, x, listener);
                            }
                        }
                    } catch (JSONException ignored) {
                    }
                }
            }
        });
    }

    //    TODO 待測試
    public void uploadAvatar(Context context, String token, final String path, final int x, final OnUploadAvatarListener listener) {
        JSONObject object = new JSONObject();
        try {
            JSONObject header = new JSONObject();
            header.put("tokenId", token);
            object.put("x", 0);
            object.put("y", 0);
            object.put("size", x);
            header.put("language", AppConfig.LANGUAGE);
            object.put("_header_", header);
        } catch (JSONException ignored) {
        }

        File file = new File(path);

        RequestBody requestBody = getBuilder(token)
            .addFormDataPart("args", object.toString())
            .addFormDataPart(FILE, file.getName(),
                RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file))
            .build();

        Request request = new Request.Builder()
            .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.baseAvatarUpload)
            .post(requestBody)
            .build();

        mainHandler = getMainHandler(context);

        getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                if (listener != null) {
                    mainHandler.post(() -> {
                        listener.onUploadFailed(e.toString());
                    });
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject header = jsonObject.getJSONObject("_header_");
                        if (header.getBoolean("success")) {
                            String mId = jsonObject.getString("id");
                            mainHandler.post(() -> {
                                listener.onUploadSuccess(mId);
                            });
                        } else {
                            String errorCode = header.getString("errorCode");
                            ErrCode code = ErrCode.of(errorCode);
                            if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
                                refreshToken(context, path, x, listener);
                            }
                        }
                    } catch (JSONException ignored) {
                    }
                }
            }
        });
    }

    /**
     * 上传群组头像并创建群组
     */
    public void uploadGroupAvatar(Context context, String token, final String path, final int x, final OnUploadAvatarListener listener, final List<String> userIds, final String name) {
        JSONArray arrayUserId = new JSONArray();
        for (String userId : userIds) {
            arrayUserId.put(userId);
        }
        JSONObject object = new JSONObject();
        try {
            JSONObject header = new JSONObject();
            object.put("name", name);
            object.put("x", 0);
            object.put("y", 0);
            object.put("size", x);
            object.put("userIds", arrayUserId);
            object.put("type", "group");
            object.put("isUpgrade", false);
            header.put("tokenId", token);
            header.put("language", AppConfig.LANGUAGE);
            object.put("_header_", header);
        } catch (JSONException ignored) {
        }

        File file = new File(path);

        try {
            RequestBody requestBody = getBuilder(token)
                .addFormDataPart("args", URLEncoder.encode(object.toString(), "UTF-8"))
                .addFormDataPart(FILE, file.getName(),
                    RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file))
                .build();

            Request request = new Request.Builder()
                .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + "/" + ApiPath.chatRoomCreate)
                .post(requestBody)
                .build();

            mainHandler = getMainHandler(context);

            getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                    if (listener != null) {
                        mainHandler.post(() -> {
                            listener.onUploadFailed("建立社團失敗");
                        });
                    }
                }

                @Override
                public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject header = jsonObject.getJSONObject("_header_");
                            if (header.getBoolean("success")) {
                                String mId = jsonObject.getString("id");
                                mainHandler.post(() -> {
                                    listener.onUploadSuccess(mId);
                                });
                            } else {
                                String errorCode = header.getString("errorCode");
                                ErrCode code = ErrCode.of(errorCode);
                                if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
                                    refreshToken(context, path, x, listener);
                                }
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                }
            });
        } catch (UnsupportedEncodingException ignored) {
        }
    }


    /**
     * 升級群組
     */
    public void upgradeGroupWithAvatar(Context context, String token, final String path, final int size, List<String> userIds, final OnUpgradeWithAvatarListener listener, final String roomId, final String groupName) {
        JSONArray arrayUserId = new JSONArray();
        for (String userId : userIds) {
            arrayUserId.put(userId);
        }
        JSONObject object = new JSONObject();
        try {
            JSONObject header = new JSONObject();
            object.put("name", groupName);
            object.put("x", 0);
            object.put("y", 0);
            object.put("size", size);
            object.put("userIds", arrayUserId);
            object.put("roomId", roomId);
            header.put("tokenId", token);
            header.put("language", AppConfig.LANGUAGE);
            object.put("_header_", header);
        } catch (JSONException ignored) {
        }

        File file = new File(path);

        try {
            RequestBody requestBody = getBuilder(token)
                .addFormDataPart("args", URLEncoder.encode(object.toString(), "UTF-8"))
                .addFormDataPart(FILE, file.getName(),
                    RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file))
                .build();

            Request request = new Request.Builder()
                .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + "/" + ApiPath.chatRoomUpgrade)
                .post(requestBody)
                .build();

            mainHandler = getMainHandler(context);

            getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                    Log.d("UpgradeToGroupActivity", "上传升级为社團头像失败 " + e.toString());
                    if (listener != null) {
                        mainHandler.post(() -> {
                            listener.onUploadFailed("升級為社團失敗");
                        });
                    }
                }

                @Override
                public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONObject header = jsonObject.getJSONObject("_header_");
                            if (header.getBoolean("success")) {
                                //升级成功后，
                                String groupOwnerId = jsonObject.getString("ownerId");
                                String groupId = jsonObject.getString("roomId");
//                                String avatarid = jsonObject.getString("avatarId");
                                mainHandler.post(() -> {
                                    listener.onUploadSuccess(groupId, groupOwnerId);
                                });
                                Log.d("UpgradeToGroupActivity", "上传升级为社團头像成功 " + "社團拥有者id" + groupOwnerId + "群组的id" + roomId);
                            } else {
                                String errorCode = header.getString("errorCode");
                                Log.d("UpgradeToGroupActivity", "收到升級為社團api的錯誤碼 " + errorCode);
                                ErrCode code = ErrCode.of(errorCode);
                                if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
                                    Log.d("UpgradeToGroupActivity", code + "token失效 ");
                                }
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                }
            });
        } catch (UnsupportedEncodingException ignored) {
        }
    }


    private void refreshToken(Context context, final String path, final int x, final OnUploadAvatarListener listener) {
        ApiManager.getInstance().refreshToken(context, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tokenId = TokenPref.getInstance(context).getTokenId();
                uploadAvatar(context, tokenId, path, x, listener);
            }
        });
    }


//    private void refreshTokenOfBuildGroup(Context context, final String path, final int x, final OnUploadAvatarListener listener, final List<String> userIds, final String name) {
//        new TokenApplyRequest(context, new TokenApplyRequest.Listener() {
//            @Override
//            public void allCallBack(boolean isRefresh, boolean status) {
//                if (isRefresh && status) {
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_FCM_TOKEN_ID));
//                }
//            }
//
//            @Override
//            public void onSuccess(boolean isRefresh, String tokenId, String avatarId, String nickName, ArrayList<AileTokenApply.Resp.AiffInfo> aiffInfo) {
//                ApiManager.getInstance().closeInvalid();
//                ApiManager.getInstance().closeSocketInvalid();
//                uploadGroupAvatar(context, tokenId, path, x, listener, userIds, name);
//            }
//
//            @Override
//            public void onFailed(ErrCode errorCode, String errorMessage) {
//            }
//
//            @Override
//            public void onCallData(String roomId, String meetingId, String callKey) {
//
//            }
//        }).setMainThreadEnable(false).request(true);
//    }


    /**
     * 更改聊天室或群组的头像
     * TODO 待測試
     */
    public void uploadRoomAvatar(Context context, String roomId, String token, String path, int x, OnUploadAvatarListener listener) {
        JSONObject object = new JSONObject();
        try {
            JSONObject header = new JSONObject();
            header.put("tokenId", token);
            object.put("x", 0);
            object.put("y", 0);
            object.put("size", x);
            object.put("roomId", roomId);
            header.put("language", AppConfig.LANGUAGE);
            object.put("_header_", header);
        } catch (JSONException ignored) {
        }

        File file = new File(path);

        try {
            RequestBody requestBody = getBuilder(token)
                .addFormDataPart("args", URLEncoder.encode(object.toString(), "UTF-8"))
                .addFormDataPart(FILE, file.getName(),
                    RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file))
                .build();

            Request request = new Request.Builder()
                .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.baseRoomAvatarUpload)
                .post(requestBody)
                .build();

            mainHandler = getMainHandler(context);

            getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                    if (listener != null) {
                        mainHandler.post(() -> {
                            listener.onUploadFailed("上傳頭像失敗");
                        });
                    }
                }

                @Override
                public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            String mId = jsonObject.getString("id");
                            mainHandler.post(() -> {
                                listener.onUploadSuccess(mId);
                            });
                        } catch (JSONException ignored) {
                        }
                    }
                }
            });
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    // EVAN_FLAG 2020-03-04 (1.10.0) 上傳多種類型檔案

    public void onUploadFile(Context context, final String name, final String messageId, String token, MessageType type, String path, OnUploadListener listener) {
        File file = new File(path);
        mainHandler = getMainHandler(context);
        final long totalSize = file.length();

        if (totalSize <= 0) {
            if (listener != null) {
                mainHandler.post(() -> {
                    listener.onFailed("Error");
                });
            }
            return;
        }

        RequestBody requestBody = getBuilder(token)
            .addFormDataPart(FILE, file.getName(),
                new CountingFileRequest(file, guessMimeType(file.getName()), num -> {
                    if (listener != null) {
                        float progress = (num / (float) totalSize) * 100;
                        Log.i(TAG, "upload = " + progress);
                        mainHandler.post(() -> {
                            listener.onProgress(messageId, (int) progress, totalSize);
                        });
                    }
                })).build();

        Request request = new Request.Builder()
            .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.baseFileUpload)
            .post(requestBody)
            .build();

        getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (listener != null) {
                    mainHandler.post(() -> {
                        listener.onFailed(e.toString());
                    });
                }
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (listener != null) {
                        String responseBody = response.body().string();
                        if (Strings.isNullOrEmpty(responseBody)) {
                            mainHandler.post(() -> {
                                listener.onFailed("上傳失敗");
                            });
                        } else {
                            mainHandler.post(() -> {
                                listener.onSuccess(messageId, type, responseBody);
                            });
                        }
                    }
                }
            }
        });
    }

    public void requestUploadAttachment(Context context, final String name, final String type,
                                        final String content, final String version,
                                        final ArrayList<String> localMediaPaths,
                                        final List<String> logPath,
                                        final UploadAttachmentRequest.Listener listener) {
        String tempDir = context.getFilesDir() + "/log/";
        mainHandler = getMainHandler(context);
        JSONObject requestJson = new JSONObject();

        JSONObject head = new JSONObject();
        try {
            head.put("tokenId", TokenPref.getInstance(context).getTokenId());
            head.put("language", AppConfig.LANGUAGE);
            requestJson.put("_header_", head);
            requestJson.put("name", name);
            requestJson.put("type", type);
            requestJson.put("content", content);
            requestJson.put("version", version);
            requestJson.put("osType", AppConfig.osType);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (String path : localMediaPaths) {
            File file = new File(path);
            final long totalSize = file.length();
            builder.addFormDataPart(FILE, file.getName(),
                new CountingFileRequest(file, guessMimeType(file.getName()), num -> {
                    if (listener != null) {
                        float progress = (num / (float) totalSize) * 100;
                        Log.i(TAG, "upload = " + progress);
                        mainHandler.post(() -> {
                            listener.onUplodeAttachmentProgress((int) progress);
                        });
                    }
                }));
        }

        List<String> tempLogPath = Lists.newArrayList();
        for (String mLogPath : logPath) { //取Log
            File file = new File(mLogPath);
            if (file.getTotalSpace() > 0) {
                String tempPath = tempDir + "t_" + file.getName();
                try {
                    FileHelper.copy(mLogPath, tempPath, false);
                } catch (Exception ignored) {

                }
                Log.d("logPath", mLogPath);
                tempLogPath.add(tempPath);
            }
        }
        for (String path : tempLogPath) {
            new CpRepairRequest(context, new CpApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    try {
                        JSONObject result = new JSONObject(s);
                        JSONObject header = result.getJSONObject("_header_");
                        if (header.getBoolean("success")) {
                            mainHandler.post(() -> {
                                listener.onUplodeAttachmentSuccess("上報成功");
                            });

                            for (String path : tempLogPath) {
                                File file = new File(path);
                                file.delete();
                            }
                        } else {
                            mainHandler.post(() -> listener.onUplodeAttachmentFailed("上報失敗，api正常", ""));
                        }
                    } catch (JSONException ignored) {
                        mainHandler.post(() -> listener.onUplodeAttachmentFailed("上報失敗，api正常", ""));
                    }
                }

                public void onFailed(String errorCode, String errorMessage) {
                    CELog.e("response parse failed " + errorMessage);
                    mainHandler.post(() -> listener.onUplodeAttachmentFailed("上報失敗", ""));
                }
            })
                .setMainThreadEnable(true)
                .requestWithFile(requestJson, path);
        }
    }

    //SEND_MULTIPLE
    public interface OnUploadListener {
        void onSuccess(String messageId, MessageType type, String response);

        void onProgress(String messageId, int progress, long total);

        void onFailed(String reason);
    }

    public interface OnImageUploadListener {
        void onUploadSuccess(String messageId, FileEntity entity);

        void onUploadIng(String messageId, int progress, long total);

        void onUploadFailed(String reason);
    }

    public interface OnVoiceUploadListener {
        void onUploadSuccess(String messageId, FileEntity entity);

        void onUploadFailed(String reason);
    }

    public interface OnFileUploadListener {
        void onUploadSuccess(String messageId, FileEntity entity);

        void onUploadIng(String messageId, int progress, long total);

        void onUploadFailed(String reason, String messageId);
    }

    public interface OnPicUploadListener {
        void onUploadSuccess(String url);

        void onUploadFailed(String reason);
    }

    public static class FileEntity {
        /**
         * size : 294295
         * _header_ : {"success":true,"timeCost":654}
         * thumbnailSize : 264880
         * name : android_chat_uid_photo_1561086985857.jpeg
         * width : 563
         * thumbnailWidth : 300
         * thumbnailHeight : 400
         * url : https://223.112.26.202:11001/ce/openapi/base/file/api-upload/download/m/2019/06/21/11/8eef68e698e24c15acd7f05e0e12f971.jpeg
         * height : 750
         * thumbnailUrl : https://223.112.26.202:11001/ce/openapi/base/file/api-upload/download/m/2019/06/21/11/8eef68e698e24c15acd7f05e0e12f971-t.jpeg
         */

        private int size;
        private HeaderBean _header_;
        private int thumbnailSize;
        private String name;
        private int width;
        private int thumbnailWidth;
        private int thumbnailHeight;
        private String url;
        private int height;
        private String MD5;
        private String thumbnailUrl;
        private double duration;


        public String getMD5() {
            return MD5;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public HeaderBean get_header_() {
            return _header_;
        }

        public void set_header_(HeaderBean _header_) {
            this._header_ = _header_;
        }

        public int getThumbnailSize() {
            return thumbnailSize;
        }

        public void setThumbnailSize(int thumbnailSize) {
            this.thumbnailSize = thumbnailSize;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getThumbnailWidth() {
            return thumbnailWidth;
        }

        public void setThumbnailWidth(int thumbnailWidth) {
            this.thumbnailWidth = thumbnailWidth;
        }

        public int getThumbnailHeight() {
            return thumbnailHeight;
        }

        public void setThumbnailHeight(int thumbnailHeight) {
            this.thumbnailHeight = thumbnailHeight;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }


        public double getDuration() {
            return duration;
        }

        public void setDuration(double duration) {
            this.duration = duration;
        }

        public boolean isHeightAndWidthError() {
            return width <= 0 || height <= 0;
        }

        public static class HeaderBean {
            /**
             * success : true
             * timeCost : 654
             */

            private boolean success;
            private int timeCost;

            public boolean isSuccess() {
                return success;
            }

            public void setSuccess(boolean success) {
                this.success = success;
            }

            public int getTimeCost() {
                return timeCost;
            }

            public void setTimeCost(int timeCost) {
                this.timeCost = timeCost;
            }
        }
    }

    public interface OnUploadAvatarListener {
        void onUploadSuccess(String url);

        void onUploadFailed(String reason);
    }


    //升級群聊到群組的監聽器
    public interface OnUpgradeWithAvatarListener {
        void onUploadSuccess(String roomId, String ownerId);

        void onUploadFailed(String reason);
    }
}
