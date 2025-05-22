package tw.com.chainsea.chat.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * reference: https://www.itread01.com/article/1501396158.html
 */
public class BitmapKit {
    /**
     * 圖片質量壓縮
     * size 圖片大小（kb）
     */
    public static Bitmap compress(Bitmap image, int size, String imageType) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (imageType.equalsIgnoreCase("png")) {
                image.compress(Bitmap.CompressFormat.PNG, 100, baos);
            } else {
                // 質量壓縮方法，這裡100表示不壓縮，把壓縮後的資料存放到baos中
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            }
            int options = 100;
            // 迴圈判斷如果壓縮後圖片是否大於100kb,大於繼續壓縮
            while (baos.toByteArray().length / 1024 > size) {
                baos.reset(); // 重置baos即清空baos
                if (imageType.equalsIgnoreCase("png")) {
                    image.compress(Bitmap.CompressFormat.PNG, options, baos);
                } else {
                    // 這裡壓縮options%，把壓縮後的資料存放到baos中
                    image.compress(Bitmap.CompressFormat.JPEG, options, baos);
                }
                options -= 10; // 每次都減少10
            }
            FileOutputStream out = new FileOutputStream("thumb.jpg");
            image.compress(Bitmap.CompressFormat.JPEG, options, out);
            // 把壓縮後的資料baos存放到ByteArrayInputStream中
            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
            // 把ByteArrayInputStream資料生成圖片
            return BitmapFactory.decodeStream(isBm, null, null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 讀取圖片屬性：旋轉的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            String orientString = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException ignored) {
        }
        return degree;
    }

    /**
     * 旋轉圖片
     */
    public static Bitmap rotationImage(int angle, Bitmap bitmap) {
        if (bitmap == null)
            return null;
        // 旋轉圖片 動作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 建立新的圖片
        return Bitmap.createBitmap(bitmap, 0, 0,
            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
