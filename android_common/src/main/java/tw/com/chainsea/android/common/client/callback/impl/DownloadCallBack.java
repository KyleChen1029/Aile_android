package tw.com.chainsea.android.common.client.callback.impl;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import tw.com.chainsea.android.common.client.exception.ClientRequestException;
import tw.com.chainsea.android.common.client.type.FileMedia;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

/**
 * current by evan on 2020-08-25
 *
 * @author Evan Wang
 * @date 2020-08-25
 */
public abstract class DownloadCallBack extends ACallBack {
    private static final String TAG = DownloadCallBack.class.getSimpleName();

    private String destFileDir;
    private String destFileName;

    public DownloadCallBack(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
    }

    public DownloadCallBack(String destFileDir, String destFileName, boolean mainThreadEnable) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
        this.mainThreadEnable = mainThreadEnable;
    }

    /**
     * Custom response success function
     *
     * @param file
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    abstract public void onSuccess(String resp, File file);

    /**
     * Custom response failure function
     *
     * @param e
     * @param errorMsg
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    abstract public void onFailure(Exception e, String errorMsg);

    /**
     * Progress Rate
     *
     * @param progress
     * @param total
     */
    public void progress(float progress, long total) {

    }

    @Override
    public void onSuccess(ResponseBody body, boolean mainThreadEnable) {
        String type = body.contentType().type();
        String subtype = body.contentType().subtype();
        Log.w(TAG, type + ":::" + subtype);

        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            // If it is not a file
            if (!FileMedia.isDownload(subtype)) {
                String respSrt = body.string();
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(new ClientRequestException("response data contentType is not file"), respSrt));
                return;
            }

            // Is the file
            is = body.byteStream();
            final long total = body.contentLength();
            long sum = 0;
            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> progress(finalSum * 1.0f / total, total));
            }
            fos.flush();
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onSuccess("", file));
        } catch (Exception e) {
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(new ClientRequestException("response data == null"), "response data == null"));
        } finally {
            try {
                body.close();
                if (is != null) is.close();
            } catch (IOException e) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(new ClientRequestException("response data == null"), "response data == null"));
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(new ClientRequestException("response data == null"), "response data == null"));
            }
        }
    }

}
