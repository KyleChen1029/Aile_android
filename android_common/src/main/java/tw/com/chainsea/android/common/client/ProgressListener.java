package tw.com.chainsea.android.common.client;

/**
 * current by evan on 2020-01-14
 */
public interface ProgressListener {
    /**
     * @param bytesRead     Number of bytes read
     * @param contentLength Total response length
     * @param done          whether to read
     */
    void update(long bytesRead, long contentLength, boolean done);
}
