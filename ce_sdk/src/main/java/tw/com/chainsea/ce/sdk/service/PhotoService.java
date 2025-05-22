package tw.com.chainsea.ce.sdk.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.google.common.base.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import pl.droidsonroids.gif.GifDrawable;
import tw.com.chainsea.android.common.client.callback.impl.DownloadCallBack;
import tw.com.chainsea.android.common.client.callback.impl.FileCallBack;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.image.BitmapHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.service.listener.ProgressServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-08-17
 *
 * @author Evan Wang
 * @date 2020-08-17
 */
public class PhotoService {

    public static synchronized void post(Context context, String url, ImageView imageView, @DrawableRes int defResId, @Nullable ProgressServiceCallBack<Drawable, RefreshSource> callBack) {

        if (Strings.isNullOrEmpty(url)) {
            if (callBack != null) {
                callBack.error("ERROR");
            } else {
                imageView.setImageResource(defResId);
            }
            return;
        }
        if (url.startsWith("http")) {
            String tempPath = context.getCacheDir() + "/temp/" + "photo" + "/";
            String name = getLastString(url);
            File photoFile = new File(tempPath + name);
            if (photoFile.exists()) {
                try {
                    String localFileName = photoFile.getName();
                    Drawable drawable = null;
                    if (localFileName.toLowerCase().endsWith(".gif")) {
                        drawable = new GifDrawable(photoFile);
                    } else {
                        drawable = Drawable.createFromStream(new FileInputStream(photoFile), null);
                    }

                    if (callBack != null && drawable != null) {
                        callBack.complete(drawable, RefreshSource.LOCAL);
                    } else if (callBack == null && drawable != null) {
                        imageView.setImageDrawable(drawable);
                    } else {
                        photoFile.delete();
                        CELog.e("");
                        imageView.setImageResource(defResId);
                    }
                } catch (Exception e) {
                    if (callBack != null) {
                        callBack.error(e.getMessage());
                    } else {
                        imageView.setImageResource(defResId);
                    }
                }
                return;
            }

            try {
                ClientsHelper.post(true).execute(url, new PhotoFileCallBack(tempPath, name) {
                    @Override
                    public void progress(float progress, long total) {
//                        if (callBack != null) {
//                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                                callBack.progress(progress,total);
//                            });
//                        }
                        CELog.e("progress: " + progress + ", total: " + total);
                    }

                    @Override
                    public void onSuccess(File file) {
                        try {
                            String fileName = file.getName();
                            long fileSize = file.length();
                            Drawable drawable = null;
                            if (fileName.toLowerCase().endsWith(".gif")) {
                                drawable = new GifDrawable(file);
                            } else {
                                drawable = Drawable.createFromStream(new FileInputStream(file), null);
                            }
                            resultProcessing(drawable, imageView, defResId, null, callBack);
                        } catch (Exception e) {
                            resultProcessing(null, imageView, defResId, e.getMessage(), callBack);
                        }
                    }

                    @Override
                    protected void onFailure(String errorMessage, File file) {
                        resultProcessing(null, imageView, defResId, errorMessage, callBack);
                        if (file != null) {
                            file.delete();
                        }
                    }
                });
            } catch (Exception e) {
                if (callBack != null) {
                    callBack.error(e.getMessage());
                } else {
                    imageView.setImageResource(defResId);
                }
            }
        } else {
            try {
                Drawable drawable = null;
                if (url.toLowerCase().endsWith(".gif")) {
                    drawable = new GifDrawable(url);
                } else {
                    drawable = Drawable.createFromStream(new FileInputStream(url), null);
                }
                if (callBack != null) {
                    callBack.complete(drawable, RefreshSource.LOCAL);
                } else {
                    imageView.setImageDrawable(drawable);
                }
            } catch (Exception e) {
                if (callBack != null) {
                    callBack.error(e.getMessage());
                } else {
                    imageView.setImageResource(defResId);
                }
            }
        }
    }


    private static void resultProcessing(Drawable drawable, ImageView imageView, @DrawableRes int defResId, String errorMessage, @Nullable ServiceCallBack<Drawable, RefreshSource> callBack) {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            if (drawable == null) {
                if (callBack != null) {
                    callBack.error(errorMessage);
                } else {
                    imageView.setImageResource(defResId);
                }
                return;
            }

            if (callBack != null) {
                callBack.complete(drawable, RefreshSource.REMOTE);
            } else {
                imageView.setImageDrawable(drawable);
            }
        });
    }


    public static String getLastString(String str) {
        URL url;
        try {
            url = new URL(str);
        } catch (MalformedURLException e) {
            return null;
        }

        String file = url.getFile();
        String[] splitStr = file.split("/");
        int len = splitStr.length;
        String result = splitStr[len - 1];
        return result;
    }


    abstract static class VideoFileCallBack extends DownloadCallBack {
        public VideoFileCallBack(String destFileDir, String destFileName) {
            super(destFileDir, destFileName, false);
        }

        abstract void onSuccess(File file);

        abstract void onFailure(String errorMessage, File file);

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

    public abstract static class PhotoFileCallBack extends FileCallBack {

        public PhotoFileCallBack(String destFileDir, String destFileName) {
            super(destFileDir, destFileName, false);
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


    public static void loadVideoThumbnail(Context context, String url, @DrawableRes int defResId, @Nullable ServiceCallBack<Bitmap, RefreshSource> callBack) {
        if (Strings.isNullOrEmpty(url)) {
            if (callBack != null) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), defResId);
                callBack.complete(bitmap, RefreshSource.LOCAL);

            }
            return;
        }
        if (!url.startsWith("http")) {
            File localFile = new File(url);
            if (callBack != null && localFile.exists()) {
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(localFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    callBack.complete(bitmap, RefreshSource.LOCAL);
                });
            }
            return;
        }

        String name = getLastString(url);
        String tempPath = context.getCacheDir() + "/temp/" + "video/";
        String thumbnailTempPath = context.getCacheDir() + "/temp/" + "video/thumbnail" + "/";
        File temp = new File(thumbnailTempPath);
        if (!temp.exists()) {
            temp.mkdir();
        }
        File localFile = new File(thumbnailTempPath + name + "_t.jpg");
        if (localFile.exists()) {
            Bitmap bitmap = BitmapHelper.getBitmapFromLocal(localFile.getPath());
            if (callBack != null) {
                callBack.complete(bitmap, RefreshSource.LOCAL);
            }
            if (bitmap != null) {
                return;
            } else {
                localFile.delete();
            }
        }

        ClientsHelper.post(true).execute(url, new VideoFileCallBack(tempPath, name) {
            @Override
            void onSuccess(File file) {
                if (file != null) {
                    try {
                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                        if (callBack != null && bitmap != null) {
                            BitmapHelper.bitmapToFile(bitmap, thumbnailTempPath, name + "_t.jpg");
                            callBack.complete(bitmap, RefreshSource.REMOTE);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            void onFailure(String errorMessage, File file) {
                if (callBack != null) {
                    callBack.error(errorMessage);
                }
            }
        });

    }
}
