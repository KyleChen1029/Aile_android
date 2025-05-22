package tw.com.chainsea.android.common.file;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * FileHelper
 *
 * @author hexiaoming
 */
public class FileHelper {

    public static File updateDir = null;
    public static File updateFile = null;
    public static boolean isCreateFileSucess;


    public static byte[] read(File file) throws IOException {
//        File.MAX_FILE_SIZE
//        if (file.length() > MAX_FILE_SIZE) {
//
//            throw new RuntimeException(file.getPath());
//        }
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException ignored) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException ignored) {
            }
        }
        return ous.toByteArray();
    }


    public static void mkdirs(String... paths) {
        for (String path : paths) {
            File tmpFile = new File(path);
            if (!tmpFile.exists()) {
                boolean isMk = tmpFile.mkdirs();
            }
        }
    }

    /**
     * createFile
     *
     * @see FileHelper
     */
    public static void createFile(String downloadPath) {

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            isCreateFileSucess = true;
            updateDir = new File(downloadPath);

            updateFile = new File(updateDir + "/" + "CEChat" + ".apk");

            if (!updateDir.exists()) {
                boolean isMk = updateDir.mkdirs();
            }
            if (!updateFile.exists()) {
                try {
                    boolean isCnf = updateFile.createNewFile();
                } catch (IOException e) {
                    isCreateFileSucess = false;
                }
            } else {
                boolean isDeleted = updateFile.delete();
                try {
                    boolean isCnf = updateFile.createNewFile();
                } catch (IOException ignored) {
                    isCreateFileSucess = false;
                }
            }
        } else {
            isCreateFileSucess = false;
        }
    }

    public static byte[] file2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException ignored) {
        }
        return buffer;
    }

    public static void copy(String source, String target, boolean isFolder) throws Exception {
        if (isFolder) {
            boolean isMk = (new File(target)).mkdirs();
            File a = new File(source);
            String[] file = a.list();
            File temp;
            for (int i = 0; i < Objects.requireNonNull(file).length; i++) {
                if (source.endsWith(File.separator)) {
                    temp = new File(source + file[i]);
                } else {
                    temp = new File(source + File.separator + file[i]);
                }
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(target + "/" + (temp.getName()));
                    byte[] b = new byte[1024];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {
                    copy(source + "/" + file[i], target + "/" + file[i], true);
                }
            }
        } else {
            int byteread;
            File oldfile = new File(source);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(source);
                File file = new File(target);
                boolean isMk = Objects.requireNonNull(file.getParentFile()).mkdirs();
                boolean isCnf = file.createNewFile();
                FileOutputStream fs = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        }
    }


    public static String getFileName(String a_Path) {
        int l_Index = a_Path.lastIndexOf('/');
        return a_Path.substring(l_Index + 1);
    }

    public static String getFileTyle(String path) {
        int startIndex = path.lastIndexOf(46) + 1;
        int endIndex = path.length();
        return path.substring(startIndex, endIndex);
    }

    public static Bitmap.CompressFormat getFileTypeToCompressFormat(Uri uri) {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
//        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());

        if ("png".equals(fileExtension)) {
            return Bitmap.CompressFormat.PNG;
        }
        return Bitmap.CompressFormat.JPEG;
    }

    public static String getFileName(Context context , Uri uri) {
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            if (returnCursor != null && returnCursor.moveToFirst()) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String fileName = returnCursor.getString(nameIndex);
                returnCursor.close();
                return fileName;
            }
        }
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            File file = new File(Objects.requireNonNull(uri.getPath()));
            return file.getName();
        }

        return "";
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;
        //Check uri format to avoid null
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(Objects.requireNonNull(uri.getPath()))).toString());
        }
        return extension;
    }

}
