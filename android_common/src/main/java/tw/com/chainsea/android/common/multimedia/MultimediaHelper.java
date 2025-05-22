package tw.com.chainsea.android.common.multimedia;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * current by evan on 2020-06-04
 *
 * @author Evan Wang
 * date 2020-06-04
 * MediaBean
 */
public class MultimediaHelper {
    private static final String TAG = MultimediaHelper.class.getSimpleName();

    public enum Type {
        VIDEO,
        AUDIO,
        IMAGE,
        FILE;

        public static Set<Type> IMAGE_or_VIDEO() {
            return Sets.newHashSet(IMAGE, VIDEO);
        }

        public static Set<Type> All() {
            return Sets.newHashSet(values());
        }
    }

    public static ContentResolver getContentResolver(Context context) {
        return context.getApplicationContext().getContentResolver();
    }

    public enum FileType {
        IMAGE("", Lists.newArrayList(".png", ".jpg", ".jpeg", ".gif", ".bmp")),
        AUDIO("", Lists.newArrayList(".mp3", ".wav", ".ogg", ".midi")),
        VIDEO("", Lists.newArrayList(".mp4", ".rmvb", ".avi", ".flv", ".3gp", ".mov")),
        WEB("", Lists.newArrayList(".jsp", ".html", ".htm", ".js", ".php")),
        TEXT("", Lists.newArrayList(".txt", ".c", ".cpp", ".xml", ".py", ".json", ".log")),
        EXCEL("Excel", Lists.newArrayList(".xls", ".xlsx")),
        WORD("Word", Lists.newArrayList(".doc", ".docx")),
        PPT("", Lists.newArrayList(".ppt", ".pptx")),
        PDF("PDF", Lists.newArrayList(".pdf")),
        FILE("", Lists.newArrayList(".jar", ".zip", ".rar", ".gz", ".apk")),
        NONE("", Lists.newArrayList());

        private String name;
        private List<String> types;

        FileType(String name, List<String> types) {
            this.name = name;
            this.types = types;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }

        public static FileType of(String type) {
            for (FileType ft : values()) {
                for (String t : ft.types) {
                    if (t.toUpperCase().equals(type.toUpperCase())) {
                        return ft;
                    }
                }
            }
            return NONE;
        }

        //        public static Set<FileType> TEXT_or_EXCEL_or_PPT_or_PDF = Sets.newHashSet(TEXT, EXCEL, PPT, PDF);
        public static Set<FileType> TEXT_or_EXCEL_or_PPT_or_PDF_or_VIDEO = Sets.newHashSet(TEXT, EXCEL, PPT, PDF, VIDEO);

        public static List<String> merge(Set<FileType> typeSet) {
            Set<String> types = Sets.newHashSet();
            for (FileType type : typeSet) {
                types.addAll(type.getTypes());
            }
            return Lists.newArrayList(types);
        }


        public static FileType check(String filePath) {
            filePath = filePath.toLowerCase();
            for (FileType type : values()) {
                for (String t : type.getTypes()) {
                    if (filePath.endsWith(t)) {
                        return type;
                    }
                }
            }
            return FileType.NONE;
        }
    }

    public static String getType(String path) {
        return FileType.check(path).name();
    }

    public static List<IMediaBean> query(Context context, Set<Type> types) {
        List<IMediaBean> medias = Lists.newArrayList();
        for (Type t : types) {
            switch (t) {
                case VIDEO:
                    medias.addAll(video(context));
                    break;
                case FILE:
                    medias.addAll(files(context, FileType.merge(FileType.TEXT_or_EXCEL_or_PPT_or_PDF_or_VIDEO)));
                    break;
                case IMAGE:
                    medias.addAll(images(context));
                    break;
                case AUDIO:
                    break;
            }
        }

        return medias;
    }

    public static List<AMediaBean> query(Context context, Type type) {
        long useTime = System.currentTimeMillis();
        switch (type) {
            case VIDEO:
                return video(context);
            case FILE:
                Log.d(TAG, String.format("MultimediaHelper:: query local Media video use time::: %s /s ", (System.currentTimeMillis() - useTime) / 1000.0d));
                return files(context, FileType.merge(FileType.TEXT_or_EXCEL_or_PPT_or_PDF_or_VIDEO));
            case IMAGE:
                Log.d(TAG, String.format("MultimediaHelper:: query local Media image use time::: %s /s ", (System.currentTimeMillis() - useTime) / 1000.0d));
                return images(context);
            case AUDIO:
            default:
                return Lists.newArrayList();
        }
    }

    public static List<AMediaBean> query(Context context, Type type, long updateTime) {
        switch (type) {
            case VIDEO:
                return video(context);
            case FILE:
                return files(context, FileType.merge(FileType.TEXT_or_EXCEL_or_PPT_or_PDF_or_VIDEO));
            case IMAGE:
                return images(context);
            case AUDIO:
            default:
                return Lists.newArrayList();
        }
    }

    public static List<AMediaBean> query(Context context, Type type, int limit) {
        switch (type) {
            case VIDEO:
                return video(context, limit);
            case FILE:
                return files(context, FileType.merge(FileType.TEXT_or_EXCEL_or_PPT_or_PDF_or_VIDEO), limit);
            case IMAGE:
                try {
                    return images(context, limit);
                } catch (Exception ignored) {

                }
            case AUDIO:
            default:
                return Lists.newArrayList();
        }
    }

    @SuppressLint("Range")
    private static synchronized List<AMediaBean> images(Context context) {
        List<AMediaBean> list = Lists.newLinkedList();
        String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE
        };

        Cursor cursor = getContentResolver(context).query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if (cursor == null) {
            cursor.close();
            return list;
        }

        while (cursor.moveToNext()) {
            try {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)); // 改为直接读取路径
                File file = new File(path);
                if (file != null) {
                    String folderName = "unknown";
                    if (file.getParentFile() != null) {
                        folderName = file.getParentFile().getName();
                    }
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
                    String type = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE));
                    long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED));

                    ImageBean bean = new ImageBean(id, title, type, size);
                    bean.setFolderName(folderName);
                    bean.setPath(path);
                    bean.setDate(date);

                    list.add(bean);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        cursor.close();
        Collections.sort(list);
        return list;
    }


    @SuppressLint("Range")
    private static synchronized List<AMediaBean> images(Context context, int limit) throws Exception {
        List<AMediaBean> list = Lists.newLinkedList();
        String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE
        };

        Cursor cursor = getContentResolver(context).query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, "_id desc limit " + limit);
        if (cursor == null) {
            cursor.close();
            return list;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        while (cursor.moveToNext()) {
            try {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE));
                byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                String path = new String(data, 0, data.length - 1);
                File file = new File(path);
                String folderName = "unknown";
                if (file.getParentFile() != null) {
                    folderName = file.getParentFile().getName();
                }
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
                String type = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE));
                long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED));
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);

                ImageBean bean = new ImageBean(id, title, type, size);

                bean.setFolderName(folderName);
                bean.setPath(path);
                bean.setDate(date);

                Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(context), id, MediaStore.Images.Thumbnails.MINI_KIND, options);
                if (bitmap != null) {
                    bean.setThumbnailBitmap(bitmap);
                }
                list.add(bean);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        cursor.close();
        Collections.sort(list);
        return list;
    }

    @SuppressLint("Range")
    private static synchronized List<AMediaBean> video(Context context) {
        List<AMediaBean> list = Lists.newLinkedList();
        String[] projection = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE
        };

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Cursor cursor = getContentResolver(context).query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.Media.DATE_MODIFIED + " desc");
        if (cursor == null) {
            cursor.close();
            return list;
        }

        while (cursor.moveToNext()) {
            try {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                File file = new File(path);
                if (file != null) {
                    String folderName = "unknown";
                    if (file.getParentFile() != null) {
                        folderName = file.getParentFile().getName();
                    }
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
                    String type = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));
                    long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED));

                    VideoBean bean = new VideoBean(id, title, type, size, date, path, folderName);

                    // 只在需要时加载缩略图
//                    if (shouldLoadThumbnails) {
//                    Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(context), id, MediaStore.Video.Thumbnails.MINI_KIND, options);
//                    if (bitmap != null) {
//                        bean.setThumbnailBitmap(bitmap);
//                    }
//                    }

                    bean.setFolderName(folderName);
                    bean.setPath(path);
                    bean.setDate(date);
                    list.add(bean);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        cursor.close();
        Collections.sort(list);
        return list;
    }

    @SuppressLint("Range")
    private static synchronized List<AMediaBean> video(Context context, int limit) {
        List<AMediaBean> list = Lists.newLinkedList();
        String[] projection = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE
        };

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Cursor cursor = getContentResolver(context).query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, " _id limit " + limit);
        if (cursor == null) {
            cursor.close();
            return list;
        }

        while (cursor.moveToNext()) {
            try {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE));
                byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                String path = new String(data, 0, data.length - 1);
                File file = new File(path);
                if (file != null) {
                    String folderName = "unknown";
                    if (file.getParentFile() != null) {
                        folderName = file.getParentFile().getName();
                    }
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
                    String type = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE));
                    long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED));

                    if (!Strings.isNullOrEmpty(title)) {
                        VideoBean bean = new VideoBean(
                            id,
                            title,
                            type,
                            size,
                            date,
                            path,
                            folderName
                        );
                        Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(context), id, MediaStore.Video.Thumbnails.MINI_KIND, options);
                        if (bitmap != null) {
                            bean.setThumbnailBitmap(bitmap);
                        }
                        list.add(bean);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        cursor.close();
        Collections.sort(list);
        return list;
    }

    @SuppressLint("Range")
    private static List<AMediaBean> files(Context context, List<String> types) {
        List<AMediaBean> files = Lists.newArrayList();

        String[] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE
        };

        Cursor cursor = null;
        try {
            cursor = getContentResolver(context).query(MediaStore.Files.getContentUri("external"), projection, null, null, null);
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                if (allowFileType(path, types)) {
                    if (!isExists(path)) {
                        continue;
                    }
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                    long modifiedDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));
                    long addedDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED));
                    if (!Strings.isNullOrEmpty(name)) {
                        FileBean bean = new FileBean(id, name, size, addedDate, modifiedDate);
                        bean.setPath(path);
                        files.add(bean);
                    }
                }
            }

        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return files;
    }

    @SuppressLint("Range")
    private static List<AMediaBean> files(Context context, List<String> types, int limit) {
        List<AMediaBean> files = Lists.newArrayList();

        String[] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE
        };

        Cursor cursor = null;
        try {
            cursor = getContentResolver(context).query(MediaStore.Files.getContentUri("external"), projection, null, null, "_id desc limit " + limit);
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                if (allowFileType(path, types)) {
                    if (!isExists(path)) {
                        continue;
                    }
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                    long modifiedDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));
                    long addedDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED));
                    if (!Strings.isNullOrEmpty(name)) {
                        FileBean bean = new FileBean(id, name, size, addedDate, modifiedDate);
                        bean.setPath(path);
                        files.add(bean);
                    }
                }
            }

        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return files;
    }

    /**
     * 检查档案是否存在
     */
    public static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * 允許的type
     */
    public static boolean allowFileType(String path, List<String> types) {
        path = path.toLowerCase();
        for (String type : types) {
            if (path.endsWith(type)) {
                return true;
            }
        }
        return false;
    }

}
