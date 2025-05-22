package tw.com.chainsea.chat.lib;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import tw.com.chainsea.chat.util.DaVinci;
import tw.com.chainsea.android.common.file.FileHelper;
import tw.com.chainsea.android.common.image.BitmapHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.chat.util.DownloadUtil;

/**
 * Created by sunhui on 2018/7/13.
 */

public class PictureParse {
    final static int BIG_PIC_SIZE = 600;
    final static int SMALL_PIC_SIZE = 400;

    public static BitmapBean parseGifPath(Context context, String path) {
        BitmapBean bitmapBean = new BitmapBean();
        try {
            Bitmap big_pic;
            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            //do not save photo to memory, but calculate the size
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(uri), null, op);
            op.inSampleSize = Tools.calculateInSampleSize(op, BIG_PIC_SIZE, BIG_PIC_SIZE);
            //get the photo
            op.inJustDecodeBounds = false;
            big_pic = BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(uri), null, op);

            big_pic = applyOrientation(big_pic, resolveBitmapOrientation(getRealPathFromUri(context, uri)));

            String name = "android_chat_uid_photo_" + System.currentTimeMillis() + ".gif";
            String key_small = "small" + name;

            byte[] bytes = FileHelper.file2byte(path);
            DaVinci.with(context).getImageLoader().putImage(name, bytes);

            ByteArrayOutputStream baoSmall = new ByteArrayOutputStream();
            zoomImage(big_pic, SMALL_PIC_SIZE).compress(Bitmap.CompressFormat.JPEG, 50, baoSmall);
            DaVinci.with(context).getImageLoader().putImage(key_small, baoSmall.toByteArray());

            bitmapBean.width = big_pic.getWidth();
            bitmapBean.height = big_pic.getHeight();
            bitmapBean.thumbnailUrl = key_small;
            bitmapBean.url = name;
        } catch (Exception e) {
            CELog.e("bitmapOptionFailed" + e);
        }

//        zoomGif(name);
        return bitmapBean;
    }


    public static String[] parseUri(Context context, Uri uri) throws IOException {

        Bitmap.CompressFormat format = FileHelper.getFileTypeToCompressFormat(uri);
        CELog.e(format.name());
//        Bitmap original2 = MediaStore.Images.Media.getBitmap(context.getContentResolver() ,uri);

        String[] paths = new String[2];

//        Bitmap big_pic;
        BitmapFactory.Options op = new BitmapFactory.Options();
//        op.inJustDecodeBounds = true;
//        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, op);
//        op.inSampleSize = Tools.calculateInSampleSize(op, BIG_PIC_SIZE, BIG_PIC_SIZE);

//        //get the photo
//        op.inJustDecodeBounds = false;
//        big_pic = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, op);

        Bitmap original = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
//        original = MediaStore.Images.Media.getBitmap(context.getContentResolver() ,uri);
        original = applyOrientation(original, resolveBitmapOrientation(getRealPathFromUri(context, uri)));

        String key = Tools.createName("uid_photo", MessageType.IMAGE);
        String key_small = "small" + key;
        /*compress the pic to stream*/
//        ByteArrayOutputStream baoBig = new ByteArrayOutputStream();
//        /*try {
//            big_pic = applyOrientation(big_pic, resolveBitmapOrientation(picTempFile));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }*/
//        /*if (big_pic.getRowBytes() * big_pic.getHeight() / 1024 < 600) {
//        } else {
//            big_pic.compress(Bitmap.CompressFormat.JPEG, 80, baoBig);
//        }*/
//        big_pic.compress(Bitmap.CompressFormat.JPEG, 100, baoBig);
        String path = DownloadUtil.INSTANCE.getDownloadFileDir() + "temp";

        Bitmap standardQuality = BitmapHelper.martixCompression(path, original, format, 1.0f, 15, 512L, 50L);

        ByteArrayOutputStream baosStandardQuality = new ByteArrayOutputStream();
        standardQuality.compress(format, 100, baosStandardQuality);
        /*before uploading, cache the pic*/

        DaVinci.with(context).getImageLoader().putImage(key, baosStandardQuality.toByteArray());

        ByteArrayOutputStream baoSmall = new ByteArrayOutputStream();
        zoomImage(standardQuality, SMALL_PIC_SIZE).compress(format, 50, baoSmall);
        DaVinci.with(context).getImageLoader().putImage(key_small, baoSmall.toByteArray());

        paths[0] = key;
        paths[1] = key_small;
        return paths;
    }


    public static void parsePath(Context context, String path, boolean isOriginal, ServiceCallBack<String[], Enum> callBack) {

        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            try {
                File file = new File(path);
                Uri uri = Uri.fromFile(file);

                Bitmap.CompressFormat format = FileHelper.getFileTypeToCompressFormat(uri);
                CELog.e(format.name());

                String[] paths = new String[2];
                BitmapFactory.Options op = new BitmapFactory.Options();

                Bitmap original = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                original = applyOrientation(original, resolveBitmapOrientation(getRealPathFromUri(context, uri)));

                String key = Tools.createName("uid_photo", MessageType.IMAGE);
                String key_small = "small" + key;

                Bitmap standardQuality = null;
                if (isOriginal) {
                    standardQuality = original;
                } else {
                    standardQuality = BitmapHelper.martixCompression(DownloadUtil.INSTANCE.getDownloadFileDir() + "temp", original, format, 1.0f, 15, 525L, 50L);
                }
//                Bitmap standardQuality = BitmapHelper.martixCompression(original, format, 1.0f, 15, 525L, 50L);
                ByteArrayOutputStream baosStandardQuality = new ByteArrayOutputStream();
                standardQuality.compress(format, 100, baosStandardQuality);
                DaVinci.with(context).getImageLoader().putImage(key, baosStandardQuality.toByteArray());
                ByteArrayOutputStream baoSmall = new ByteArrayOutputStream();
                zoomImage(standardQuality, SMALL_PIC_SIZE).compress(format, 50, baoSmall);
                DaVinci.with(context).getImageLoader().putImage(key_small, baoSmall.toByteArray());

                paths[0] = key;
                paths[1] = key_small;
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(paths, null));
            } catch (Exception e) {
                CELog.e("bitmapOptionFailed" + e);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.error(e.getMessage()));
            }
        });
    }

    public static String[] parsePath(Context context, String path) {
        String[] paths = new String[2];

        try {
            Bitmap big_pic;
            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            return parseUri(context, uri);
        } catch (Exception e) {
            CELog.e("bitmapOptionFailed" + e);
        }
        return paths;
    }

    public static Bitmap zoomImage(Bitmap bitmap, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();

        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
    }

    private static int resolveBitmapOrientation(String absolutePath) throws IOException {
        ExifInterface exif;
        if (absolutePath != null) {
            exif = new ExifInterface(absolutePath);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } else {
            return 0;
        }
    }

    /**
     * 将图片旋转为正确的方向
     *
     * @param bitmap      bitmap
     * @param orientation orientation
     * @return bitmap
     */
    private static Bitmap applyOrientation(Bitmap bitmap, int orientation) {
        int rotate;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            default:
                return bitmap;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, mtx, true);
    }

    /**
     * 根据图片的Uri获取图片的绝对路径(已经适配多种API)
     *
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < 11) {
            // SDK < Api11
            return getRealPathFromUri_BelowApi11(context, uri);
        }
        if (sdkVersion < 19) {
            // SDK > 11 && SDK < 19
            return getRealPathFromUri_Api11To18(context, uri);
        }
        // SDK > 19
        return getRealFilePath(context, uri);
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 适配api11-api18,根据uri获取图片的绝对路径
     */
    @SuppressLint("Range")
    private static String getRealPathFromUri_Api11To18(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        CursorLoader loader = new CursorLoader(context, uri, projection, null,
            null, null);
        Cursor cursor = loader.loadInBackground();

        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * 适配api11以下(不包括api11),根据uri获取图片的绝对路径
     */
    @SuppressLint("Range")
    private static String getRealPathFromUri_BelowApi11(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection,
            null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }
}
