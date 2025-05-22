package tw.com.chainsea.android.common.client;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * current by evan on 2019-10-29
 */
public class CountingRequestBody extends RequestBody {

    protected RequestBody delegate;
    protected CountingRequestBody.Listener listener;
    protected CountingRequestBody.CountingSink countingSink;

    public CountingRequestBody(RequestBody delegate, CountingRequestBody.Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    public MediaType contentType() {
        return this.delegate.contentType();
    }

    public long contentLength() {
        try {
            return this.delegate.contentLength();
        } catch (IOException ignored) {
            return -1L;
        }
    }

    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        this.countingSink = new CountingRequestBody.CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(this.countingSink);
        this.delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    public interface Listener {
        void onRequestProgress(long var1, long var3);
    }

    protected final class CountingSink extends ForwardingSink {
        private long bytesWritten = 0L;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        public void write(@NonNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            this.bytesWritten += byteCount;
            CountingRequestBody.this.listener.onRequestProgress(this.bytesWritten, CountingRequestBody.this.contentLength());
        }
    }
}
