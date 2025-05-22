package tw.com.chainsea.android.common.client.callback.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import tw.com.chainsea.android.common.client.exception.ClientRequestException;
import tw.com.chainsea.android.common.client.type.FileMedia;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;


public abstract class FileCallBack extends ACallBack {

    protected String destFileDir;
    protected String destFileName;

    public FileCallBack(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
    }

    public FileCallBack(String destFileDir, String destFileName, boolean mainThreadEnable) {
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
     * progress
     *
     * @param progress
     * @param total
     */
    public void progress(float progress, long total) {

    }

    @Override
    public void onSuccess(ResponseBody body, boolean mainThreadEnable) {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            // If it is not a file
            String subtype = body.contentType().subtype();
            if (!FileMedia.isFileContentType(subtype)) {
                String respSrt = body.string();
                if (mainThreadEnable) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(new ClientRequestException("response data contentType is not file"), respSrt));
                }else {
                    onFailure(new ClientRequestException("response data contentType is not file"), respSrt);
                }
                return;
            }
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
                if (mainThreadEnable) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> progress(finalSum * 1.0f / total, total));
                } else {
                    progress(finalSum * 1.0f / total, total);
                }
            }
            fos.flush();
            if (mainThreadEnable) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onSuccess("", file));
            } else {
                onSuccess("", file);
            }
        } catch (Exception e) {
            if (mainThreadEnable) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(new ClientRequestException("response data == null"), "response data == null"));
            } else {
                onFailure(new ClientRequestException("response data == null"), "response data == null");
            }
        } finally {
            try {
                body.close();
                if (is != null) is.close();
            } catch (IOException e) {
                if (mainThreadEnable) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(new ClientRequestException("response data == null"), "response data == null"));
                } else {
                    onFailure(new ClientRequestException("response data == null"), "response data == null");
                }
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                if (mainThreadEnable) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(new ClientRequestException("response data == null"), "response data == null"));
                } else {
                    onFailure(new ClientRequestException("response data == null"), "response data == null");
                }
            }
        }
    }

}
