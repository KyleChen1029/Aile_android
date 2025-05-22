package tw.com.chainsea.android.common.client;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * current by evan on 2020-01-14
 */
public class NetworkInterceptor implements Interceptor {
    ProgressListener listener;

    NetworkInterceptor(ProgressListener listener) {
        this.listener = listener;
    }

    @Override
    @NonNull
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder()
            .body(new ProgressResponseBody(response.body(), listener))
            .build();
    }

    static class Build {
        ProgressListener listener;

        public Build listener(ProgressListener listener) {
            this.listener = listener;
            return this;
        }

        public NetworkInterceptor builder() {
            return new NetworkInterceptor(listener);
        }
    }
}
