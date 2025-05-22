package tw.com.chainsea.ce.sdk.service;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.video.VideoHelper;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;

/**
 * current by evan on 12/30/20
 *
 * @author Evan Wang
 * @date 12/30/20
 */
public class VideoService {


    public static void previewImage(String url, ImageView imageView, @DrawableRes int defResId, @Nullable ServiceCallBack<Bitmap, RefreshSource> callBack) {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            try {
                Bitmap bitmap = VideoHelper.findFrameAtTime(url, 1000);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    if (bitmap != null && !bitmap.isRecycled()) {
                        if (callBack != null) {
                            callBack.complete(bitmap, RefreshSource.LOCAL);
                        } else {
                            imageView.setImageBitmap(bitmap);
                        }
                        return;
                    }

                    if (callBack != null) {
                        callBack.error("");
                    } else {
                        imageView.setImageResource(defResId);
                    }
                });
            } catch (Exception e){

            }
        });
    }
}
