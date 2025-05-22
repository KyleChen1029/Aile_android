package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.common.base.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import tw.com.chainsea.android.common.client.callback.impl.FileCallBack;
import tw.com.chainsea.android.common.client.exception.ClientRequestException;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.image.BitmapHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.network.NetworkHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.AvatarRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * current by evan on 2020-06-12
 *
 * @author Evan Wang
 * date 2020-06-12
 */
public class AvatarViewRequest extends AvatarRequestBase {
    private final ApiListener<Bitmap> listener;

    public AvatarViewRequest(Context ctx, ApiListener<Bitmap> listener) {
        super(ctx, ApiPath.avatarView);
        this.listener = listener;
    }

    @Override
    protected void success(File file, PicSize size) {
        String path = file.getPath();
        String name = file.getName();

        int pixel;
        switch (size) {
            case LARGE:
                pixel = 320;
                break;
            case MED:
                pixel = 200;
                break;
            case SMALL:
            default:
                pixel = 80;
                break;
        }

        Bitmap bitmap = BitmapHelper.getBitmapFromLocal(path);
        if (bitmap == null) {
            if (listener != null) {
                listener.onFailed("Bitmap is null ");
            }
            return;
        }

        if (bitmap.getWidth() != pixel || bitmap.getHeight() != pixel) {
            bitmap = Bitmap.createScaledBitmap(bitmap, pixel, pixel, false);
            try {
                BitmapHelper.bitmapToFile(bitmap, path, name);
            } catch (Exception ignored) {

            }
        }
        if (listener != null) {
            listener.onSuccess(bitmap);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (listener != null) {
            listener.onFailed(errorMessage);
        }
    }

    public void postAvatar(boolean async, String filePath, String avatarId, PicSize size) {
        setMainThreadEnable(false);

        if (!NetworkHelper.hasNetWork(this.ctx)) {
            String errMsg = ctx.getString(R.string.api_there_no_internet_connection);
            if (mainThreadEnable) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> failed(ErrCode.of("-1"), errMsg));
            } else {
                failed(ErrCode.of("-1"), errMsg);
            }
            return;
        }

        String taskName = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        try {
            JSONObject jsonObject = new JSONObject()
                .put("id", avatarId)
                .put("size", size.getValue());
            this.mJSONObject = jsonObject;
            if (!Strings.isNullOrEmpty(sToken)) {
                JSONObject header = new JSONObject();
                try {
                    header.put("tokenId", sToken);
                    header.put("language", AppConfig.LANGUAGE);
                    jsonObject.put("_header_", header);
                } catch (JSONException e) {
                    CELog.e("construct header failed");
                }
            } else {
                JSONObject header = new JSONObject();
                header.put("language", AppConfig.LANGUAGE);
                jsonObject.put("_header_", header);
            }

            String taskValue = Integer.toHexString(hashCode());
            ApiManager.addRequestTask(taskName, taskValue);
            ClientsHelper.post(async).execute(URL, Media.JSON_UTF8.get(), jsonObject.toString(), new AvatarCallBack(filePath, avatarId + "_" + size.getValue()) {
                @Override
                public void onSuccess(File file) {
                    ApiManager.removeRequestTask(taskName);
                    success(file, size);
                }

                @Override
                protected void onFailure(String errorMessage, File file) {
                    CELog.e(errorMessage);
                    ApiManager.removeRequestTask(taskName);
                    if (file != null) {
                        file.delete();
                    }
                    CELog.e("response failed", errorMessage);
                    if (errorMessage.contains("TimeoutError")) {
                        failed(ErrCode.of("-1"), ctx.getString(R.string.api_request_timed_out));
                    } else if (errorMessage.contains("java.net.ConnectException: Failed to connect to")) {
                        failed(ErrCode.of("-1"), ctx.getString(R.string.api_connection_timed_out));
                    } else {
                        CELog.e("http failed: There is no Internet connection");
                        failed(ErrCode.of("-1"), ctx.getString(R.string.api_there_no_internet_connection));
                    }
                }
            });

        } catch (Exception e) {
            ApiManager.removeRequestTask(taskName);
            failed(ErrCode.of("-1"), e.getMessage());
            CELog.e("construct header failed");
        }
    }

    public abstract static class AvatarCallBack extends FileCallBack {
        public AvatarCallBack(String destFileDir, String destFileName) {
            super(destFileDir, destFileName, true);
        }

        public abstract void onSuccess(File file);

        protected abstract void onFailure(String errorMessage, File file);

        @Override
        public void progress(float progress, long total) {
            super.progress(progress, total);
        }

        @Override
        public void onSuccess(ResponseBody body, boolean mainThreadEnable) {
            //super.onSuccess(body, mainThreadEnable);
            InputStream is = null;
            byte[] buf = new byte[2048];
            int len;
            FileOutputStream fos = null;
            try {
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

        @Override
        public void onSuccess(String resp, File file) {
            try {
                ResponseBean baseResponse = JsonHelper.getInstance().from(resp, ResponseBean.class);
                if (baseResponse != null && baseResponse.get_header_() != null && !baseResponse.get_header_().isSuccess()) {
                    CELog.e(baseResponse.get_header_().getErrorMessage());
                    CELog.e(baseResponse.get_header_().getStackTrace());
                    onFailure(baseResponse.get_header_().getErrorMessage(), file);
                    return;
                }

            } catch (Exception e) {
                CELog.e(e.getMessage());
            }
            onSuccess(file);
        }

        @Override
        public void onFailure(Exception e, String errorMsg) {
            onFailure(errorMsg, null);
        }
    }
}
