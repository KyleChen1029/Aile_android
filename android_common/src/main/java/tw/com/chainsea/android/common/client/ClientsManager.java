package tw.com.chainsea.android.common.client;

import static tw.com.chainsea.android.common.SystemConfig.enableLogcat;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import tw.com.chainsea.android.common.client.callback.intf.ICallBack;
import tw.com.chainsea.android.common.client.type.FileMedia;
import tw.com.chainsea.android.common.client.utils.JsonUtil;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

/**
 * ClientsManager 連線管理
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public class ClientsManager {
    private static final String TAG = ClientsManager.class.getSimpleName();
    private static OkHttpClient _CLIENT = null;
    private static Builder cacheSettings;
    private static boolean isSingle = false;

    private ClientsManager() {

    }

    public static synchronized OkHttpClient getClient() {
        if (isSingle) {
            if (_CLIENT == null) {
                _CLIENT = new Builder()._default();
            }
            return _CLIENT;
        } else {
            if (cacheSettings == null) {
                return getClient();
            }
            return cacheSettings.build();
        }
    }

    public static void cancelAll() {
        for (Call call : getClient().dispatcher().queuedCalls()) {
            CELog.d("API cancel queued thread : " + call.request());
            call.cancel();
        }
        for (Call call : getClient().dispatcher().runningCalls()) {
            CELog.d("API cancel running thread : " + call.request());
            call.cancel();
        }
        CELog.d("API", "cancel All thread");
    }

    public static void cancelAll(OnCancelListener listener) {
        cancelAll();
        while (true) {
            try {
                if (getClient().dispatcher().queuedCalls().isEmpty() &&
                    (getClient().dispatcher().runningCalls().size() == 1 ||
                        getClient().dispatcher().runningCalls().isEmpty())
                ) break;
                Thread.sleep(200);
            } catch (InterruptedException ignored) {

                break;
            }
        }
        listener.onCancelEnd();
    }

    /**
     * 得到Builder物件，開始初始 Client 相關設定
     */
    public static Builder initClient() {
        return new Builder();
    }

    /**
     * 請求 GET 方法
     *
     * @author Evan Wang
     */
    public static void asyncGet(Builder builder, HttpUrl.Builder takeHttpBuider, Map<String, String> headers, ICallBack callBack) {
        if (takeHttpBuider == null) {
            callBack.response(null);
        }
        Request.Builder rb = new Request.Builder()
            .url(takeHttpBuider.build())
            .get();

        if (headers != null) {
            for (Map.Entry<String, String> hs : headers.entrySet()) {
                rb.addHeader(hs.getKey(), hs.getValue());
            }
        }

        Request request = rb.build();
        if (builder != null) {
            complete(builder, request, callBack);
        } else {
            complete(request, callBack);
        }
    }

    /**
     * Request POST method
     *
     * @author Evan Wang
     */
    public static Call asyncPost(Builder builder, String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack) {
        if (url == null || url.isEmpty()) {
            callBack.response(null);
        }
        if (reqData == null) {
            reqData = "";
        }

//        RequestBody body = RequestBody.create(mediaType, reqData);
        RequestBody body = wrapRequestBody(RequestBody.create(mediaType, reqData), callBack);
        Request.Builder rb = new Request.Builder()
            .url(url)
//                .addHeader("Accept-Encoding", "identity")
            .post(body);
        if (headers != null) {
            for (Map.Entry<String, String> hs : headers.entrySet()) {
                rb.addHeader(hs.getKey(), hs.getValue());
            }
        }
        Request request = rb.build();

        if (builder != null) {
            return complete(builder, request, callBack);
        } else {
            complete(request, callBack);
        }
        return null;
    }

    /**
     * 請求 POST 方法，帶檔案(File)
     *
     * @author Evan Wang
     */
    public static void asyncPostFile(Builder builder, String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, ICallBack callBack) {
        if (file == null || reqKey == null || reqKey.isEmpty()) {
            callBack.response(null);
        }

        MultipartBody.Builder builders = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (file != null) {
            RequestBody fileBody = wrapRequestBody(RequestBody.create(mediaType, file), callBack);
            builders.addFormDataPart(reqKey, file.getName(), fileBody);
        }

        if (formDataPart != null) {
            for (Map.Entry<String, String> fdp : formDataPart.entrySet()) {
                builders.addFormDataPart(fdp.getKey(), fdp.getValue());
            }
        }
        Request.Builder rb = new Request.Builder()
            .url(url)
            .post(builders.build());

        if (headers != null) {
            for (Map.Entry<String, String> hs : headers.entrySet()) {
                rb.addHeader(hs.getKey(), hs.getValue());
            }
        }

        Request request = rb.build();
        if (builder != null) {
            complete(builder, request, callBack);
        } else {
            complete(request, callBack);
        }
    }

    public static void asyncPostFile(Builder builder, String url, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, String fileName, ICallBack callBack) {
        if (file == null || reqKey == null || reqKey.isEmpty()) {
            callBack.response(null);
        }

        MultipartBody.Builder builders = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (fileName.isEmpty() && file != null) {
            fileName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
        }
        String fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        MediaType mediaType = FileMedia.of(fileType);

        if (file != null) {

//            RequestBody fileBody = RequestBody.create(mediaType, file);
            RequestBody fileBody = wrapRequestBody(RequestBody.create(mediaType, file), callBack);
            builders.addFormDataPart(reqKey, fileName, fileBody);
        }

        if (formDataPart != null) {
            for (Map.Entry<String, String> fdp : formDataPart.entrySet()) {
                builders.addFormDataPart(fdp.getKey(), fdp.getValue());
            }
        }

        Request.Builder rb = new Request.Builder()
            .url(url)
            .post(builders.build());

        if (headers != null) {
            for (Map.Entry<String, String> hs : headers.entrySet()) {
                rb.addHeader(hs.getKey(), hs.getValue());
            }
        }

        Request request = rb.build();
        if (builder != null) {
            complete(builder, request, callBack);
        } else {
            complete(request, callBack);
        }
    }

    /**
     * 請求 PUT 方法
     *
     * @author Evan Wang
     * version 0.0.1
     * @since 0.0.1
     */
    public static void put(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack) {
        if (url == null || url.isEmpty()) {
            callBack.response(null);
        }
        if (reqData == null) {
            reqData = "";
        }

        RequestBody body = RequestBody.create(mediaType, reqData);
        Request.Builder builder = new Request.Builder()
            .url(url)
            .put(body);

        if (headers != null) {
            for (Map.Entry<String, String> hs : headers.entrySet()) {
                builder.addHeader(hs.getKey(), hs.getValue());
            }
        }

        Request request = builder.build();
        complete(request, callBack);
    }

    /**
     * 請求 PUT 方法，帶檔案(File)
     *
     * @author Evan Wang
     * version 0.0.1
     * @since 0.0.1
     */
    public static void putFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, ICallBack callBack) {
        if (file == null || reqKey == null || reqKey.isEmpty()) {
            callBack.response(null);
        }

        MultipartBody.Builder builders = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (file != null) {
            RequestBody fileBody = RequestBody.create(mediaType, file);
            builders.addFormDataPart(reqKey, file.getName(), fileBody);
        }

        if (formDataPart != null) {
            for (Map.Entry<String, String> fdp : formDataPart.entrySet()) {
                builders.addFormDataPart(fdp.getKey(), fdp.getValue());
            }
        }

        Request.Builder builder = new Request.Builder()
            .url(url)
            .post(builders.build());

        if (headers != null) {
            for (Map.Entry<String, String> hs : headers.entrySet()) {
                builder.addHeader(hs.getKey(), hs.getValue());
            }
        }

        Request request = builder.build();
        complete(request, callBack);
    }

    /**
     * 請求 DELETE 方法
     *
     * @author Evan Wang
     * version 0.0.1
     * @since 0.0.1
     */
    public static void delete(String url, MediaType mediaType, Map<String, String> headers, String reqData, @NonNull ICallBack callBack) {
        if (url == null || url.isEmpty()) {
            callBack.response(null);
        }
        if (reqData == null) {
            reqData = "";
        }

        RequestBody body = RequestBody.create(mediaType, reqData);
        Request.Builder builder = new Request.Builder()
            .url(url)
            .delete(body);

        if (headers != null) {
            for (Map.Entry<String, String> hs : headers.entrySet()) {
                builder.addHeader(hs.getKey(), hs.getValue());
            }
        }

        Request request = builder.build();
        complete(request, callBack);
    }

    /**
     * Http 請求執行完成後
     *
     * @author Evan Wang
     * version 0.0.1
     * @since 0.0.1
     */
    private static void complete(Request request, ICallBack callback) {
//        Response response = null;
        try {
            getClient().newCall(request).enqueue(callback);
//            response = getClient().newCall(request).execute(callback);
//            return response;
        } catch (Exception e) {
//            return null;
            callback.response(null);
        }
    }

    private static Call complete(Builder builder, Request request, ICallBack callback) {
//        Response response = null;
        try {
            Call newCall = builder._default().newCall(request);
            newCall.enqueue(callback);
            return newCall;
//            builder._default().newCall(request).enqueue(callback);
//            getClient().newCall(request).enqueue(callback);
//            response = builder.build().newCall(request).execute();
//            return response;
        } catch (Exception e) {
//            return null;
            callback.response(null);
        }
        return null;
    }

    /**
     * OkHttpClient 建構者，若為設定會給預設值(_default)
     *
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static class Builder {
        private long connectTimeout = 10_000;
        private long readTimeout = 10_000;
        private long writeTimeout = 10_000;
        private int maxIdleConnections = 5;
        private long keepAliveDuration = 5L;
        private boolean giveLog = true;
        private int threadPoolNumber = 20;
        private HttpLoggingInterceptor.Logger logger;
        private ProgressListener listener;

        private Builder() {

        }

        public Builder listener(ProgressListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * 連結超時設定
         *
         * @author Evan Wang
         */
        public Builder connectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * 讀取超時設定
         *
         * @author Evan Wang
         */
        public Builder readTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * 寫入超時設定
         *
         * @author Evan Wang
         */
        public Builder writeTimeout(long writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        /**
         * 是否設定Log
         *
         * @author Evan Wang
         */
        public Builder giveLog(boolean giveLog) {
            this.giveLog = giveLog;
            return this;
        }

        /**
         * 自訂logger
         */
        public Builder logger(HttpLoggingInterceptor.Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * 最大連結數量設定
         *
         * @author Evan Wang
         */
        public Builder maxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
            return this;
        }

        /**
         * 持續時間設定
         *
         * @author Evan Wang
         */
        public Builder keepAliveDuration(long keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }


        public Builder threadPoolNumber(int threadPoolNumber) {
            this.threadPoolNumber = threadPoolNumber;
            return this;
        }

        /**
         * 是否走單一模式
         */
        public Builder single(boolean isSingle) {
            ClientsManager.isSingle = isSingle;
            ClientsManager.cacheSettings = this;
            return this;
        }

        public OkHttpClient newSocketBuild() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .hostnameVerifier((hostname, session) -> true)
                .connectionPool(new ConnectionPool(this.maxIdleConnections, this.keepAliveDuration, TimeUnit.MINUTES));
//            if (this.giveLog) {
//                if (this.logger != null) {
//                    builder.addInterceptor(new HttpLoggingInterceptor(this.logger).setLevel(HttpLoggingInterceptor.Level.BASIC));
//                }
//                else {
//                    builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC));
//                }
//            }

            return builder.build();
        }

        private HttpLoggingInterceptor gsonLogInterceptor() {
            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                private final StringBuilder mMessage = new StringBuilder();

                @Override
                public void log(@NonNull String message) {
                    try {
                        if (message.startsWith("--> POST") || message.startsWith("--> GET")) {
                            mMessage.setLength(0);
                        }
                        if ((message.startsWith("{") && message.endsWith("}"))
                            || (message.startsWith("[") && message.endsWith("]"))) {
                            message = JsonUtil.formatJson(JsonUtil.decodeUnicode(message));
                        }
                        mMessage.append(message.concat("\n"));
                        if (message.startsWith("<-- END HTTP")) {
                            if (enableLogcat) {
                                Log.d("API Response", mMessage.toString());
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            return logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }

        /**
         * 初始化 OkHttpClient
         *
         * @author Evan Wang
         * version 0.0.1
         * @since 0.0.1
         */
        public OkHttpClient build() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(this.connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(this.readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(this.writeTimeout, TimeUnit.MILLISECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .addNetworkInterceptor(gsonLogInterceptor())
                .connectionPool(new ConnectionPool(this.maxIdleConnections, this.keepAliveDuration, TimeUnit.MINUTES));
//            if (this.giveLog) {
//                if (this.logger != null) {
//                    builder.addInterceptor(new HttpLoggingInterceptor(this.logger).setLevel(HttpLoggingInterceptor.Level.BASIC));
//                }
//                else {
//                    builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC));
//                }
//            }


            ClientsManager._CLIENT = builder.build();
            return ClientsManager._CLIENT;
        }

        /**
         * 預設初始化 OkHttpClient
         *
         * @author Evan Wang
         * version 0.0.1
         * @since 0.0.1
         */
        private OkHttpClient _default() {
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(this.connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(this.readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(this.writeTimeout, TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(this.maxIdleConnections, this.keepAliveDuration, TimeUnit.MINUTES))
                .addInterceptor(this.logger != null
                    ? new HttpLoggingInterceptor(this.logger).setLevel(HttpLoggingInterceptor.Level.BASIC)
                    : new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .addNetworkInterceptor(gsonLogInterceptor())
                .addNetworkInterceptor(new NetworkInterceptor.Build().listener(listener).builder())
                .build();

            client.dispatcher().setMaxRequestsPerHost(5);
            client.dispatcher().setMaxRequests(10);
            return client;
        }
    }


    protected static RequestBody wrapRequestBody(RequestBody requestBody, ICallBack callBack) {
        if (callBack == null) {
            return requestBody;
        }
        return new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(final long bytesWritten, final long contentLength) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.progress(bytesWritten * 1.0f / contentLength, contentLength));
//                ClientsManager.getMainThreadExecutor().post(() -> callBack.progress(bytesWritten * 1.0f / contentLength, contentLength));
            }
        });

    }
}



