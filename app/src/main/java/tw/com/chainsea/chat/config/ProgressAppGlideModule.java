package tw.com.chainsea.chat.config;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

/**
 * current by evan on 2019-11-15
 */

@GlideModule
public class ProgressAppGlideModule extends AppGlideModule {


    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
//
//        Glide.init(context, builder
//                        .setMemoryCache(new MemoryCacheAdapter())
//                        .setBitmapPool(new BitmapPoolAdapter()));
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), ResourceIds.raw.canonical);
//        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(chain -> {
                Request request = chain.request();
                Response response = chain.proceed(request);
                ResponseProgressListener listener = new DispatchingProgressListener();
                return response.newBuilder()
//                                .addHeader("Accept-Encoding", "identity")
                    .body(new OkHttpProgressResponseBody(request, response, listener))
//                                .addHeader("Accept-Encoding", "identity")
                    .build();
            })
            .build();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }

    public static void forget(String url) {
        ProgressAppGlideModule.DispatchingProgressListener.forget(url);
    }

    public static void expect(String url, ProgressAppGlideModule.UIonProgressListener listener) {
        ProgressAppGlideModule.DispatchingProgressListener.expect(url, listener);
    }

    private interface ResponseProgressListener {
        void update(HttpUrl url, long bytesRead, long contentLength);
    }

    public interface UIonProgressListener {
        void onProgress(long bytesRead, long expectedLength, DispatchingProgressListener listener);

        /**
         * Control how often the listener needs an update. 0% and 100% will always be dispatched.
         *
         * @return in percentage (0.2 = call {@link #onProgress} around every 0.2 percent of progress)
         */
        float getGranualityPercentage();

    }

    public static class DispatchingProgressListener implements ProgressAppGlideModule.ResponseProgressListener {
        private static final Map<String, UIonProgressListener> LISTENERS = new HashMap<>();
        private static final Map<String, Long> PROGRESSES = new HashMap<>();

//        private final Handler handler;

        DispatchingProgressListener() {
//            this.handler = new Handler(Looper.getMainLooper());
        }

        public static void forget(String url) {
            LISTENERS.remove(url);
            PROGRESSES.remove(url);
        }

        static void expect(String url, UIonProgressListener listener) {
            LISTENERS.put(url, listener);
        }

        @Override
        public void update(HttpUrl url, final long bytesRead, final long contentLength) {
//            Log.w("onProgress", String.format("%s: %d/%d = %.2f%%%n", url, bytesRead, contentLength, (100f * bytesRead) / contentLength));
            String key = url.toString();
            final UIonProgressListener listener = LISTENERS.get(key);
            if (listener == null) {
                return;
            }
//            if (contentLength <= bytesRead) {
//                forget(key);
//            }
            if (needsDispatch(key, bytesRead, contentLength, listener.getGranualityPercentage())) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                        Log.w("onProgress", "Handler.postSticker");
                    listener.onProgress(bytesRead, contentLength, DispatchingProgressListener.this);
                });
            }
        }

        private boolean needsDispatch(String key, long current, long total, float granularity) {
            if (granularity == 0 || current == 0 || total == current) {
                return true;
            }
            float percent = 100f * current / total;
            long currentProgress = (long) (percent / granularity);
            Long lastProgress = PROGRESSES.get(key);

//            Log.i("onProgress", String.format("%s, %s, %s", key, currentProgress, lastProgress));
            if (lastProgress == null || currentProgress != lastProgress) {
                PROGRESSES.put(key, currentProgress);
                return true;
            } else {
                return false;
            }
        }
    }

    private static class OkHttpProgressResponseBody extends ResponseBody {
        private final HttpUrl url;
        //        private final Req request;
//        private final Response response;
        private final ResponseBody responseBody;
        private final ResponseProgressListener progressListener;
        private BufferedSource bufferedSource;

        OkHttpProgressResponseBody(Request request, Response response, ResponseProgressListener progressListener) {
            this.url = request.url();
//            this.request = request;
//            this.response = response;
            this.responseBody = response.body();
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        @NonNull
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    long fullLength = responseBody.contentLength();
                    if (bytesRead == -1) { // this source is exhausted
                        totalBytesRead = fullLength;
                    } else {
                        totalBytesRead += bytesRead;
                    }
//                    ResponseBody rb = response.peekBody(responseBody.contentLength() > 0 ? responseBody.contentLength() : Integer.MAX_VALUE);
//                    long contentLength = rb.contentLength();
//                    Log.i("onProgress", String.format("%s, %s", contentLength, fullLength));
                    progressListener.update(url, totalBytesRead, fullLength);
                    return bytesRead;
                }
            };
        }
    }
}
