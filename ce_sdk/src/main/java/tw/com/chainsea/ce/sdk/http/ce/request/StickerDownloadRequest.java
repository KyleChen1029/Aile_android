package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

import tw.com.chainsea.android.common.client.callback.impl.FileCallBack;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 11/23/20
 *
 * @author Evan Wang
 * date 11/23/20
 */
public class StickerDownloadRequest extends NewRequestBase {
    private final ApiListener<Drawable> listener;

    public enum Type {
        PICTURE("picture", "/items/"),
        THUMBNAIL_PICTURE("thumbnailPicture", "/thumbnails/");

        private String type;
        private String path;

        Type(String type, String path) {
            this.type = type;
            this.path = path;
        }

        public String getType() {
            return type;
        }

        public String getPath() {
            return path;
        }
    }

    public StickerDownloadRequest(Context ctx, ApiListener<Drawable> listener) {
        super(ctx, ApiPath.baseStickerDownload);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {

    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {

    }

    public void postSticker(String packageId, String stickerId, Type type, boolean mainThreadEnable) {
        try {
            JSONObject jsonObject = new JSONObject()
                    .put("stickerId", stickerId)
                    .put("type", type.getType());
            this.mJSONObject = jsonObject;
            if ((sToken != null) && (!sToken.isEmpty())) {
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
            String tempPath = ctx.getCacheDir() + "/sticker/" + packageId + type.getPath();
            ClientsHelper.post(true).execute(URL, Media.JSON_UTF8.get(), jsonObject.toString(), new StickerFileCallBack(tempPath, stickerId, mainThreadEnable) {
                @Override
                public void onSuccess(File file) {
                    try {
                        Drawable drawable = null;
                        if (Type.PICTURE.equals(type)) {
                            drawable = new pl.droidsonroids.gif.GifDrawable(file);
                        } else {
                            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file));
                            b.setDensity(Bitmap.DENSITY_NONE);
                            drawable = new BitmapDrawable(b);
                        }
                        if (listener != null) {
                            listener.onSuccess(drawable);
                        }
                    } catch (Exception e) {
                        failed(ErrCode.JSON_PARSE_FAILED, e.getMessage());
                    }
                }

                @Override
                protected void onFailure(String errorMessage, File file) {
                    CELog.e(errorMessage);
                    if (file != null) {
                        file.delete();
                    }
                    String error = errorMessage;
                    CELog.e("response failed", error);
                    if (error.contains("TimeoutError")) {
                        failed(ErrCode.of("-1"), ctx.getString(R.string.api_request_timed_out));
                    } else if (error.contains("java.net.ConnectException: Failed to connect to")) {
                        failed(ErrCode.of("-1"), ctx.getString(R.string.api_connection_timed_out));
                    } else {
                        CELog.e("http failed: There is no Internet connection");
                        failed(ErrCode.of("-1"), ctx.getString(R.string.api_there_no_internet_connection));
                    }
                }
            });
        } catch (Exception e) {
            failed(ErrCode.of("-1"), e.getMessage());
            CELog.e("construct header failed");
        }
    }

    public abstract static class StickerFileCallBack extends FileCallBack {

        public StickerFileCallBack(String destFileDir, String destFileName, boolean mainThreadEnable) {
            super(destFileDir, destFileName, mainThreadEnable);
        }

        public abstract void onSuccess(File file);

        protected abstract void onFailure(String errorMessage, File file);

        @Override
        public void progress(float progress, long total) {
            super.progress(progress, total);
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
