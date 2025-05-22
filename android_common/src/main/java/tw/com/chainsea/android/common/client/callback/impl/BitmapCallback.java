package tw.com.chainsea.android.common.client.callback.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import tw.com.chainsea.android.common.client.exception.ClientRequestException;

import okhttp3.ResponseBody;

public abstract class BitmapCallback extends ACallBack {

    /**
     * Custom response success function
     *
     * @param bitmap
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    abstract public void onSuccess(Bitmap bitmap);

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

    @Override
    public void onSuccess(ResponseBody body, boolean mainThreadEnable) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
            if (bitmap == null) {
                onFailure(new ClientRequestException("response data == null"), "response data == null");
                return;
            }
            onSuccess(bitmap);
        } catch (Exception e) {
            onFailure(e, e.getMessage());
        }
    }
}
