package tw.com.chainsea.ce.sdk.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import tw.com.chainsea.android.common.image.BitmapHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.AvatarViewRequest;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;

/**
 * current by evan on 2020-06-12
 *
 * @author Evan Wang
 * date 2020-06-12
 */
public class AvatarService {

    public static String getAvatarUrl(Context context, String id, PicSize size) {
        if (id == null) {
            return null;
        }
        return TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.avatarView
            + "?"
            + "id="
            + id
            + "&size="
            + size.getValue();
    }

    static LoadingCache<String, Bitmap> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build(new CacheLoader<String, Bitmap>() {
            @Override
            @NonNull
            public Bitmap load(@NonNull String key) {
                String path = Environment.getDataDirectory() + "/user/0/tw.com.chainsea.chat/cache/temp/avatar/";
                File composeFile = new File(path + key);
                if (composeFile.exists()) {
                    return BitmapFactory.decodeFile(composeFile.getPath());
                }
                return null;
            }
        });


    public static Bitmap getCache(String path) {
        try {
            Bitmap bitmap = cache.get(path);
            if ((bitmap != null) && !bitmap.isRecycled()) {
                cache.invalidate(path);
                return null;
            } else {
                return bitmap;
            }
        } catch (Exception e) {
            CELog.w(e.getMessage());
            return null;
        }
    }

    public static void clearCache(String path) {
        cache.invalidate(path);
    }

    public static void clearAllCache() {
        cache.cleanUp();
    }

    public static void putCache(String path, Bitmap bitmap) {
        if (!Strings.isNullOrEmpty(path) && bitmap != null && !bitmap.isRecycled()) {
            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
            cache.put(path, newBitmap);
        }
    }

    public static void execute(Context context, boolean isCallRemote, Queue<String> avatarQueue) {
        if (avatarQueue.isEmpty()) {
            if (isCallRemote) { // Called Api
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_UPDATE_AVATARS_ALL));
            }
            return;
        }

        String avatarId = avatarQueue.remove();
        String tempPath = context.getCacheDir() + "/temp/" + "avatar" + "/";
        File temp = new File(tempPath);
        if (!temp.exists()) {
            temp.mkdir();
        }

        File avatarFile = new File(tempPath + avatarId + "_" + PicSize.SMALL.getValue());
        if (avatarFile.exists()) {
            execute(context, isCallRemote, avatarQueue);
        } else {
            new AvatarViewRequest(context, new ApiListener<Bitmap>() {
                @Override
                public void onSuccess(Bitmap bitmap) {
//                    putCache(avatarId + "_" + PicSize.SMALL.getValue() , bitmap);
                }

                @Override
                public void onFailed(String errorMessage) {

                }
            }).postAvatar(false, tempPath, avatarId, PicSize.SMALL);
            execute(context, true, avatarQueue);
        }
    }

    public static String getCachePath(Context context, List<String> avatarIds, PicSize size) {
        String tempPath = context.getCacheDir() + "/temp/" + "avatar" + "/";

        File temp = new File(tempPath);
        if (!temp.exists()) {
            temp.mkdir();
        }

        StringBuilder builder = new StringBuilder();
        for (String id : avatarIds) {
            builder.append(id);
            builder.append("_");
        }
        builder.append(size.getValue());
        return builder.toString();
    }

    public static void postByAccountId(Context context, String userId, PicSize size, ImageView imageView, @DrawableRes int defResId) {
        String avatarId = UserProfileReference.findAccountAvatarId(null, userId);
        post(context, avatarId, size, imageView, defResId);
    }

    public static void post(Context context, String avatarId, PicSize size, ImageView imageView, @DrawableRes int defResId) {
        if (Strings.isNullOrEmpty(avatarId)) {
            imageView.setImageResource(defResId);
        } else {
            post(context, Lists.newArrayList(avatarId), size, imageView, defResId);
        }
    }

    public static void post(Context context, List<String> avatarIds, PicSize size, ImageView imageView, @DrawableRes int defResId) {
        if (avatarIds == null || avatarIds.isEmpty()) {
            CELog.e("avatarIds is null");
            imageView.setImageResource(defResId);
            return;
        }
        String tempPath = SdkLib.getAppContext().getCacheDir() + "/temp/" + "avatar" + "/";

        StringBuilder builder = new StringBuilder();
        for (String id : avatarIds) {
            builder.append(id);
            builder.append("_");
        }
        builder.append(size.getValue());

        Bitmap cache = getCache(builder.toString());
        if (cache != null && !cache.isRecycled()) {
            imageView.setImageBitmap(cache);
            return;
        }

        File composeFile = new File(tempPath + builder);
        if (composeFile.exists()) {
            Bitmap composeCache = BitmapHelper.getBitmapFromLocal(composeFile.getPath());
            if (composeCache != null) {
                putCache(builder.toString(), composeCache);
                imageView.setImageBitmap(composeCache);
                return;
            }
        }

        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            if (avatarIds.size() > 1) {
                Map<String, Bitmap> data = Maps.newLinkedHashMap();
                execute(SdkLib.getAppContext(), avatarIds.size(), Queues.newLinkedBlockingDeque(avatarIds), data, tempPath, builder.toString(), size, imageView, defResId);
            } else {
                execute(SdkLib.getAppContext(), avatarIds.get(0), tempPath, builder.toString(), size, imageView, defResId);
            }
        });
    }

    private static void execute(Context context, String avatarId, String tempPath, String fileName, PicSize size, ImageView imageView, @DrawableRes int defResId) {
        new AvatarViewRequest(context, new ApiListener<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                try {
                    BitmapHelper.bitmapToFile(bitmap, tempPath, fileName);
                } catch (Exception e) {
                    CELog.e(e.getMessage());
                }
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }

            @Override
            public void onFailed(String errorMessage) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    if (imageView != null) {
                        imageView.setImageResource(defResId);
                    }
                });
            }
        }).postAvatar(false, tempPath, avatarId, size);
    }

    private static void execute(Context context, int amount, Queue<String> avatarQueues, Map<String, Bitmap> data, String tempPath, String fileName, PicSize size, ImageView imageView, @DrawableRes int defResId) {
        if (avatarQueues.isEmpty()) {
            composeAvatar(context, amount, size, tempPath, fileName, data, RefreshSource.LOCAL, imageView, defResId);
            return;
        }

        String avatarId = avatarQueues.remove();

        Bitmap cache = getCache(avatarId + "_" + size.getValue());
        if (cache != null && !cache.isRecycled()) {
            data.put(avatarId, cache);
            execute(context, amount, avatarQueues, data, tempPath, fileName, size, imageView, defResId);
            return;
        }

        File avatarFile = new File(tempPath + avatarId + "_" + size.getValue());
        if (avatarFile.exists()) {
            Bitmap composeCache = BitmapHelper.getBitmapFromLocal(avatarFile.getPath());
            if (composeCache == null) {
                CELog.e("cache file is null avatarId:: " + avatarId + ", path:: " + avatarFile.getPath());
            } else {
                data.put(avatarId, composeCache);
                putCache(avatarId + "_" + size.getValue(), composeCache);
            }
            execute(context, amount, avatarQueues, data, tempPath, fileName, size, imageView, defResId);
        } else {
            new AvatarViewRequest(context, new ApiListener<Bitmap>() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    data.put(avatarId, bitmap);
                    execute(context, amount, avatarQueues, data, tempPath, fileName, size, imageView, defResId);
                }

                @Override
                public void onFailed(String errorMessage) {
                    execute(context, amount, avatarQueues, data, tempPath, fileName, size, imageView, defResId);
                }
            }).postAvatar(false, tempPath, avatarId, size);
        }
    }

    private static void composeAvatar(Context context, int amount, PicSize size, String tempPath, String fileName, Map<String, Bitmap> data, RefreshSource source, ImageView imageView, @DrawableRes int defResId) {
        LinkedList<Bitmap> bitmaps = Lists.newLinkedList();
        boolean hasError = false;
        for (Map.Entry<String, Bitmap> entry : data.entrySet()) {
            Bitmap bitmap = entry.getValue();
            if (bitmap == null) {
                hasError = true;
                bitmap = BitmapHelper.decodeResourceScaledBitmap(context, defResId, size.getSize(), size.getSize(), false);
            } else {
                if (bitmap.getWidth() != size.getSize() || bitmap.getHeight() != size.getSize()) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, size.getSize(), size.getSize(), false);
                }
            }
            bitmaps.add(bitmap);
        }

        if (bitmaps.size() != amount) {
            hasError = true;
            int index = amount - bitmaps.size();
            for (int i = 0; i < index; i++) {
                BitmapHelper.drawableToBitmap(context, defResId);
                Bitmap defBitmap = BitmapHelper.decodeResourceScaledBitmap(context, defResId, size.getSize(), size.getSize(), false);
                bitmaps.add(defBitmap);
            }
        }

        Bitmap compose = BitmapHelper.addBitmap(bitmaps, size.getSize() * 2);

        if (!hasError) {
            try {
                BitmapHelper.bitmapToFile(compose, tempPath, fileName);
                putCache(fileName, compose);
            } catch (Exception e) {
                CELog.e(e.getMessage());
            }
        } else {
            CELog.e(String.format("Compose Avatars hasError amount :: %s, fileName :: %s", amount, fileName));
        }
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> imageView.setImageBitmap(compose));
    }
}
