package tw.com.chainsea.android.common.client.helper;

import com.google.common.collect.Maps;

import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.logging.HttpLoggingInterceptor;
import tw.com.chainsea.android.common.client.ClientsManager;
import tw.com.chainsea.android.common.client.ProgressListener;
import tw.com.chainsea.android.common.client.abs.APost;
import tw.com.chainsea.android.common.client.callback.intf.ICallBack;
import tw.com.chainsea.android.common.client.method.AsyncPost;
import tw.com.chainsea.android.common.client.method.Post;


/**
 * ClientsHelper Http 請求工具類
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public class ClientsHelper {

//    public static Post POST = Post.getInstance();
//    public static AsyncPost ASYNC_POST = AsyncPost.getInstance();
//    public static AsyncGet ASYNC_GET = AsyncGet.getInstance();
//    public static AsyncPut ASYNC_PUT = AsyncPut.getInstance();
//    public static AsyncDelete ASYNC_DELETE = AsyncDelete.getInstance();

    public static APost post(boolean async) {
        return async ? AsyncPost.getInstance() : Post.getInstance();
    }

//    public static AsyncGet get() {
//        return AsyncGet.newInstance();
//    }

//    public static void buildPost(Builder builder, ICallBack callBack) {
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//            ClientsManager.asyncPost(getOption(builder), builder.url, builder.mediaType, builder.header, builder.request, callBack);
//        });
//    }

//    public static void buildPost(Builder builder, String url, MediaType type, Map<String, String> header, String request, ICallBack callBack) {
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//            ClientsManager.asyncPost(getOption(builder), url, type, header, request, callBack);
//        });
//    }

//    public static void buildGet(Builder builder, ICallBack callBack) {
//        ClientsManager.getIoThreadExecutor().execute(() -> {
//            ClientsManager.asyncPost(getOption(builder), builder.url, builder.mediaType, builder.header, builder.request, callBack);
//        });
//    }

    public static Call buildDownloadGet(String url, ICallBack callBack, ProgressListener listener) {
        Builder builder = new ClientsHelper.Builder()
            .url(url)
            .connectTimeout(10_000)
            .readTimeout(10_000)
            .writeTimeout(10_000)
            .maxIdleConnections(5)
            .keepAliveDuration(5)
            .giveLog(true)
            .giveSSL(true)
            .progressListener(listener);
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
        return ClientsManager.asyncPost(getOption(builder), builder.url, builder.mediaType, builder.header, builder.request, callBack);
//        });
    }

    private static ClientsManager.Builder getOption(Builder builder) {
        return ClientsManager.initClient()
            .connectTimeout(builder.connectTimeout)
            .readTimeout(builder.readTimeout)
            .writeTimeout(builder.writeTimeout)
            .maxIdleConnections(builder.maxIdleConnections)
            .keepAliveDuration(builder.keepAliveDuration)
            .listener(builder.progressListener)
            .giveLog(builder.giveLog)
            .logger(builder.logger);
    }

    public static class Builder {
        private String url = "";
        private String request = "";
        private long connectTimeout = 10_000;
        private long readTimeout = 10_000;
        private long writeTimeout = 10_000;
        private int maxIdleConnections = 5;
        private long keepAliveDuration = 5L;
        private Map<String, String> header = Maps.newHashMap();
        private MediaType mediaType;
        private ProgressListener progressListener;
        private boolean giveLog = false;
        private HttpLoggingInterceptor.Logger logger;
        private boolean giveSSL = false;


        public Builder giveLog(boolean giveLog) {
            this.giveLog = giveLog;
            return this;
        }

        public Builder logger(HttpLoggingInterceptor.Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder giveSSL(boolean giveSSL) {
            this.giveSSL = giveSSL;
            return this;
        }

        public Builder progressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder request(String request) {
            this.request = request;
            return this;
        }

        public Builder connectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(long writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder maxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
            return this;
        }

        public Builder keepAliveDuration(long keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }

        public Builder header(Map<String, String> header) {
            this.header = header;
            return this;
        }

        public Builder addHeader(String key, String value) {
            this.header.put(key, value);
            return this;
        }


        public Builder mediaType(MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

    }

}
