package tw.com.chainsea.ce.sdk.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import pl.droidsonroids.gif.GifDrawable;
import tw.com.chainsea.android.common.client.callback.impl.FileCallBack;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.image.DefType;
import tw.com.chainsea.android.common.image.ImagesHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.bean.common.EnableType;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.bean.sticker.EmoticonType;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerPackageEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerSelectAction;
import tw.com.chainsea.ce.sdk.reference.StickerReference;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.android.common.log.CELog;


/**
 * current by evan on 2020-09-30
 *
 * @author Evan Wang
 * @date 2020-09-30
 */
public class StickerService {

    public static void initEmoji(Context context, @Nullable ServiceCallBack<String, RefreshSource> callBack) {
        boolean hasEmoji = StickerReference.hasEmojiData(null);
        if (hasEmoji) {
            return;
        }

        String packageId = UUID.randomUUID().toString();
        StickerPackageEntity entity = StickerPackageEntity.Build()
            .id(packageId)
            .action(StickerSelectAction.INPUT)
            .emoticonType(EmoticonType.EMOJI)
            .count(emojiData.length)
            .name("aile_emoji")
            .columns(8)
            .row(emojiData.length / 8)
            .joinTime(1)
            .enable(EnableType.Y)
            .iconId("icon_emoji")
            .iconUrl("drawable://icon_emoji")
            .build();

        List<StickerItemEntity> items = Lists.newArrayList();


        for (int i = 0; i < emojiData.length; i++) {
            String sourceName = "drawable://" + emojiData[i][0];
            String tag = emojiData[i][1];
            items.add(StickerItemEntity.Build()
                .index(emojiData.length - i)
                .id(UUID.randomUUID().toString())
                .pictureUrl(sourceName)
                .thumbnailPictureUrl(sourceName)
                .stickerPackageId(packageId)
                .name(tag)
                .displayName(tag)
                .keywords(tag)
//                    .enable(EnableType.Y)
                .build());
        }

        StickerReference.emojiSave(null, entity);
        StickerReference.itemSave(null, items);
    }

    public static void getStickerPackageEntities(Context context, RefreshSource source, @Nullable ServiceCallBack<List<StickerPackageEntity>, RefreshSource> callBack) {
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {

            List<StickerPackageEntity> localPackageEntities = StickerReference.packageFindAll(null, context);
            if (callBack != null) {
                Collections.sort(localPackageEntities);
                callBack.complete(localPackageEntities, RefreshSource.LOCAL);
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doStickerPackageList(context, new ApiListener<List<StickerPackageEntity>>() {
                int count = 0;

                @Override
                public void onSuccess(List<StickerPackageEntity> packageEntities) {
                    Set<String> localIds = StickerReference.packageFindAllIds(null);
                    StickerReference.packageSave(null, packageEntities);
                    Collections.sort(packageEntities);
                    count = packageEntities.size();
                    Set<String> packageIdSet = Sets.newHashSet();

                    for (StickerPackageEntity packageEntity : packageEntities) {
                        String packageId = packageEntity.getId();
                        packageIdSet.add(packageId);
                        String itemTempPath = context.getCacheDir() + "/sticker/" + packageEntity.getId() + "/items/";
                        String packageTempPath = context.getCacheDir() + "/sticker/" + packageEntity.getId() + "/";
                        FileHelper.mkdirs(packageTempPath, itemTempPath);
                        localIds.remove(packageEntity.getId());
                        getStickerEntities(context, packageEntity.getId(), source, new ServiceCallBack<List<StickerItemEntity>, RefreshSource>() {
                            @Override
                            public void complete(List<StickerItemEntity> entities, RefreshSource source) {
                                count--;
                                packageEntity.setCount(entities.size());
                                packageEntity.setColumns(4);
                                packageEntity.setRow(entities.size() / 4);
                                packageEntity.setStickerItems(entities);
                                boolean status = StickerReference.packageSave(null, packageEntities);
                                if (callBack != null && count <= 0) {
                                    callBack.complete(packageEntities, RefreshSource.REMOTE);
                                }
                            }

                            @Override
                            public void error(String message) {
                                count--;
                                if (callBack != null && count <= 0) {
                                    callBack.error(message);
                                }
                            }
                        });
                    }

                    postPackageIcons(context, Queues.newLinkedBlockingQueue(packageEntities), Maps.newLinkedHashMap(), null);

                    if (callBack != null) {
                        List<StickerPackageEntity> localPackageEntities = StickerReference.packageFindByIds(null, context, packageIdSet);
                        Collections.sort(localPackageEntities);
                        callBack.complete(localPackageEntities, RefreshSource.REMOTE);
                    }
                    if (!localIds.isEmpty()) {
                        // update Package Disable
                        boolean disableStatus = StickerReference.updateDisablePackageByIds(null, localIds);
                        CELog.d("disable:: " + localIds + ", status:: " + disableStatus);
                    }


                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                }
            });
        }
    }

    public static void getStickerEntities(Context context, String stickerPackageId, RefreshSource source, @Nullable ServiceCallBack<List<StickerItemEntity>, RefreshSource> callBack) {
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            List<StickerItemEntity> localItemEntities = StickerReference.itemFindAll(null, stickerPackageId, context);
            if (callBack != null) {
                callBack.complete(localItemEntities, RefreshSource.LOCAL);
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doStickerList(context, stickerPackageId, new ApiListener<List<StickerItemEntity>>() {
                @Override
                public void onSuccess(List<StickerItemEntity> entities) {
                    StickerReference.itemSave(null, entities);
                    if (callBack != null) {
                        callBack.complete(entities, RefreshSource.REMOTE);
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                    if (callBack != null) {
                        callBack.error(errorMessage);
                    }
                }
            });
        }
    }

    public static int getStickerThumbnailsSize(Context context, String stickerPackageName) {
        if (context == null) {
            return 0;
        }
        String tempPath = context.getCacheDir() + "/sticker/" + stickerPackageName + "/thumbnails/";
        File stickerFile = new File(tempPath);
        if (stickerFile.exists()) {
            File[] files = stickerFile.listFiles();
            if (files == null) return -1;
            else return files.length;
        } else {
            stickerFile.mkdirs();
            return 0;
        }
    }

    public static synchronized void postSticker(Context context, String packageId, String stickerId, StickerDownloadRequest.Type type, @Nullable ServiceCallBack<Drawable, RefreshSource> callBack) {
        // old sticker find local assets by id
        if (!Strings.isNullOrEmpty(stickerId) && !StringHelper.isValidUUID(stickerId)) {
            try {
                InputStream stream = context.getAssets().open("emoticons/qbi/" + stickerId);
                GifDrawable gifFromAssets = new GifDrawable(stream);
                if (callBack != null) {
                    callBack.complete(gifFromAssets, RefreshSource.LOCAL);
                }
            } catch (Exception e) {
                CELog.e(e.getMessage());
                if (callBack != null) {
                    callBack.error(e.getMessage());
                }
            }
            return;
        }

        if (!StringHelper.isValidUUID(packageId) || Strings.isNullOrEmpty(stickerId)) {
            if (callBack != null) {
                callBack.error("package Id is null ");
            }
            return;
        }

        String tempPath = context.getCacheDir() + "/sticker/" + packageId + type.getPath();
        File stickerFile = new File(tempPath + stickerId);
        if (stickerFile.exists()) {
            try {
                Drawable drawable;
                if (StickerDownloadRequest.Type.PICTURE.equals(type)) {
                    drawable = new GifDrawable(stickerFile);
                } else {
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(stickerFile));
                    b.setDensity(Bitmap.DENSITY_NONE);
                    drawable = new BitmapDrawable(b);
                }
                if (callBack != null) {
                    callBack.complete(drawable, RefreshSource.LOCAL);
                } else {
                    stickerFile.delete();
                }
            } catch (Exception e) {
                if (callBack != null) {
                    callBack.error(e.getMessage());
                }
            }
        } else {
            ApiManager.doStickerDownload(context, true, packageId, stickerId, type, new ApiListener<Drawable>() {
                @Override
                public void onSuccess(Drawable drawable) {
                    if (callBack != null) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(drawable, RefreshSource.REMOTE));
                        callBack.complete(drawable, RefreshSource.REMOTE);
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (callBack != null) {
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.error(errorMessage));
                        callBack.error(errorMessage);
                    }
                }
            });
        }
    }

    public static synchronized void postDownloads(Context context, Queue<StickerItemEntity> entityQueues, StickerDownloadRequest.Type type, @Nullable ServiceCallBack<String, RefreshSource> callBack) {
        if (entityQueues.isEmpty()) {
            if (callBack != null) {
                callBack.complete("", RefreshSource.REMOTE);
            }
            return;
        }
        StickerItemEntity entity = entityQueues.remove();
        String packageId = entity.getStickerPackageId();
        String stickerId = entity.getId();
        if (Strings.isNullOrEmpty(stickerId)) {
            if (callBack != null) {
                callBack.error("ERROR");
            }
            postDownloads(context, entityQueues, type, callBack);
            return;
        }

        String tempPath = context.getCacheDir() + "/sticker/" + packageId + type.getPath();
        File stickerFile = new File(tempPath + stickerId);
        if (stickerFile.exists()) {
            callBack.complete("", RefreshSource.LOCAL);
        } else {
            ApiManager.doStickerDownload(context, false, packageId, stickerId, type, new ApiListener<Drawable>() {
                @Override
                public void onSuccess(Drawable drawable) {
                    postDownloads(context, entityQueues, type, callBack);
                }

                @Override
                public void onFailed(String errorMessage) {
                    postDownloads(context, entityQueues, type, callBack);
                }
            });
        }
    }

    public static synchronized void postPackageIcons(Context context, Queue<StickerPackageEntity> entityQueues, Map<String, Drawable> data, @Nullable ServiceCallBack<Map<String, Drawable>, RefreshSource> callBack) {
        if (entityQueues.isEmpty()) {
            if (callBack != null) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(data, RefreshSource.REMOTE));
            }
            return;
        }

        StickerPackageEntity entity = entityQueues.remove();
        String packageIconUrl = entity.getIconUrl();
        String packageId = entity.getId();

        if (Strings.isNullOrEmpty(packageIconUrl)) {
            postPackageIcons(context, entityQueues, data, callBack);
            return;
        }

        if (packageIconUrl.startsWith("drawable:")) {
            Drawable drawable = ImagesHelper.setDrawableByName(context, DefType.DRAWABLE, entity.getIconId(), R.drawable.custom_default_avatar);
            data.put(packageId, drawable);
            postPackageIcons(context, entityQueues, data, callBack);
            return;
        }

        if (packageIconUrl.startsWith("http")) {
            String tempPath = context.getCacheDir() + "/sticker/" + packageId + "/";
            File temp = new File(tempPath);
            if (!temp.exists()) {
                temp.mkdir();
            }
            File stickerFile = new File(tempPath + packageId);
            if (stickerFile.exists()) {
                try {
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(stickerFile));
                    b.setDensity(Bitmap.DENSITY_NONE);
                    Drawable drawable = new BitmapDrawable(b);
                    if (callBack != null) {
                        data.put(packageId, drawable);
                    }
                } catch (Exception e) {
                    CELog.e(e.getMessage());
                } finally {
                    postPackageIcons(context, entityQueues, data, callBack);
                }
                return;
            }
            ClientsHelper.post(true).execute(packageIconUrl, new StickerFileCallBack(tempPath, packageId) {
                @Override
                public void onSuccess(File file) {
                    try {
                        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file));
                        b.setDensity(Bitmap.DENSITY_NONE);
                        Drawable drawable = new BitmapDrawable(b);
                        data.put(packageId, drawable);
                    } catch (Exception e) {
                        CELog.e(e.getMessage());
                    }
                    postPackageIcons(context, entityQueues, data, callBack);
                }

                @Override
                protected void onFailure(String errorMessage, File file) {
                    postPackageIcons(context, entityQueues, data, callBack);
                    if (file != null) {
                        file.delete();
                    }
                }
            });
        }
    }

    public abstract static class StickerFileCallBack extends FileCallBack {

        public StickerFileCallBack(String destFileDir, String destFileName) {
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


    private static final String[][] emojiData = {
        {"emoji_0x1f604.png", fromCodePoint(0x1f604)},
        {"emoji_0x1f603.png", fromCodePoint(0x1f603)},
        {"emoji_0x1f60a.png", fromCodePoint(0x1f60a)},
        {"emoji_0x1f609.png", fromCodePoint(0x1f609)},
        {"emoji_0x1f60d.png", fromCodePoint(0x1f60d)},
        {"emoji_0x1f618.png", fromCodePoint(0x1f618)},
        {"emoji_0x1f61a.png", fromCodePoint(0x1f61a)},
        {"emoji_0x1f61c.png", fromCodePoint(0x1f61c)},
        {"emoji_0x1f61d.png", fromCodePoint(0x1f61d)},
        {"emoji_0x1f633.png", fromCodePoint(0x1f633)},
        {"emoji_0x1f601.png", fromCodePoint(0x1f601)},
        {"emoji_0x1f614.png", fromCodePoint(0x1f614)},
        {"emoji_0x1f60c.png", fromCodePoint(0x1f60c)},
        {"emoji_0x1f612.png", fromCodePoint(0x1f612)},
        {"emoji_0x1f61e.png", fromCodePoint(0x1f61e)},
        {"emoji_0x1f623.png", fromCodePoint(0x1f623)},
        {"emoji_0x1f622.png", fromCodePoint(0x1f622)},
        {"emoji_0x1f602.png", fromCodePoint(0x1f602)},
        {"emoji_0x1f62d.png", fromCodePoint(0x1f62d)},
        {"emoji_0x1f62a.png", fromCodePoint(0x1f62a)},
        {"emoji_0x1f625.png", fromCodePoint(0x1f625)},
        {"emoji_0x1f630.png", fromCodePoint(0x1f630)},
        {"emoji_0x1f613.png", fromCodePoint(0x1f613)},
        {"emoji_0x1f628.png", fromCodePoint(0x1f628)},
        {"emoji_0x1f631.png", fromCodePoint(0x1f631)},
        {"emoji_0x1f620.png", fromCodePoint(0x1f620)},
        {"emoji_0x1f621.png", fromCodePoint(0x1f621)},
        {"emoji_0x1f616.png", fromCodePoint(0x1f616)},
        {"emoji_0x1f637.png", fromCodePoint(0x1f637)},
        {"emoji_0x1f632.png", fromCodePoint(0x1f632)},
        {"emoji_0x1f47f.png", fromCodePoint(0x1f47f)},
        {"emoji_0x1f60f.png", fromCodePoint(0x1f60f)},
        {"emoji_0x1f466.png", fromCodePoint(0x1f466)},
        {"emoji_0x1f467.png", fromCodePoint(0x1f467)},
        {"emoji_0x1f468.png", fromCodePoint(0x1f468)},
        {"emoji_0x1f469.png", fromCodePoint(0x1f469)},
        {"emoji_0x1f31f.png", fromCodePoint(0x1f31f)},
        {"emoji_0x1f444.png", fromCodePoint(0x1f444)},
        {"emoji_0x1f44d.png", fromCodePoint(0x1f44d)},
        {"emoji_0x1f44e.png", fromCodePoint(0x1f44e)},
        {"emoji_0x1f44c.png", fromCodePoint(0x1f44c)},
        {"emoji_0x1f44a.png", fromCodePoint(0x1f44a)},
        {"emoji_0x270a.png", Character.toString((char) 0x270a)},
        {"emoji_0x270c.png", Character.toString((char) 0x270c)},
        {"emoji_0x1f446.png", fromCodePoint(0x1f446)},
        {"emoji_0x1f447.png", fromCodePoint(0x1f447)},
        {"emoji_0x1f449.png", fromCodePoint(0x1f449)},
        {"emoji_0x1f448.png", fromCodePoint(0x1f448)},
        {"emoji_0x1f64f.png", fromCodePoint(0x1f64f)},
        {"emoji_0x1f44f.png", fromCodePoint(0x1f44f)},
        {"emoji_0x1f4aa.png", fromCodePoint(0x1f4aa)},
        {"emoji_0x1f457.png", fromCodePoint(0x1f457)},
        {"emoji_0x1f380.png", fromCodePoint(0x1f380)},
        {"emoji_0x2764.png", Character.toString((char) 0x2764)},
        {"emoji_0x1f494.png", fromCodePoint(0x1f494)},
        {"emoji_0x1f48e.png", fromCodePoint(0x1f48e)},
        {"emoji_0x1f436.png", fromCodePoint(0x1f436)},
        {"emoji_0x1f431.png", fromCodePoint(0x1f431)},
        {"emoji_0x1f339.png", fromCodePoint(0x1f339)},
        {"emoji_0x1f33b.png", fromCodePoint(0x1f33b)},
        {"emoji_0x1f341.png", fromCodePoint(0x1f341)},
        {"emoji_0x1f343.png", fromCodePoint(0x1f343)},
        {"emoji_0x1f319.png", fromCodePoint(0x1f319)},
        {"emoji_0x2600.png", Character.toString((char) 0x2600)},
        {"emoji_0x2601.png", Character.toString((char) 0x2601)},
        {"emoji_0x26a1.png", Character.toString((char) 0x26a1)},
        {"emoji_0x2614.png", Character.toString((char) 0x2614)},
        {"emoji_0x1f47b.png", fromCodePoint(0x1f47b)},
        {"emoji_0x1f385.png", fromCodePoint(0x1f385)},
        {"emoji_0x1f381.png", fromCodePoint(0x1f381)},
        {"emoji_0x1f4f1.png", fromCodePoint(0x1f4f1)},
        {"emoji_0x1f50d.png", fromCodePoint(0x1f50d)},
        {"emoji_0x1f4a3.png", fromCodePoint(0x1f4a3)},
        {"emoji_0x26bd.png", Character.toString((char) 0x26bd)},
        {"emoji_0x2615.png", Character.toString((char) 0x2615)},
        {"emoji_0x1f37a.png", fromCodePoint(0x1f37a)},
        {"emoji_0x1f382.png", fromCodePoint(0x1f382)},
        {"emoji_0x1f3e0.png", fromCodePoint(0x1f3e0)},
        {"emoji_0x1f697.png", fromCodePoint(0x1f697)},
        {"emoji_0x1f559.png", fromCodePoint(0x1f559)}
    };

    public static String fromCodePoint(int codePoint) {
        return newString(codePoint);
    }

    public static String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }

    public static String fromChar(char ch) {
        return Character.toString(ch);
    }
}
