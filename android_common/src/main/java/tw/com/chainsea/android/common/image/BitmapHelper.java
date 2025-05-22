package tw.com.chainsea.android.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.google.common.collect.Range;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.Objects;


/**
 * BitmapHelper Bitmap tools
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public class BitmapHelper {
    private static final String TAG = BitmapHelper.class.getSimpleName();

    /**
     * Assemble multiple Bitmap
     *
     * @param bitmaps
     * @param canvasSize
     * @return
     */
    public synchronized static Bitmap addBitmap(LinkedList<Bitmap> bitmaps, int canvasSize) {
        // Create canvas Bitmap
        int num = 8;
        Bitmap result = Bitmap.createBitmap(canvasSize + num, canvasSize + num, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(0xFFFFFFFF);
        int count = bitmaps.size();
        for (int i = 0; i < count; i++) {
            Bitmap b = bitmaps.get(i);
            switch (count) {
                case 1:
                    drawOneToPosition(canvas, b, i, canvasSize / 2, num);
//                    result = b;
                    break;
                case 2:
                    drawTwoToPosition(canvas, b, i, canvasSize / 2, num);
                    break;
                case 3:
                    drawThreeToPosition(canvas, b, i, canvasSize / 2, num);
                    break;
                case 4:
                    drawFourToPosition(canvas, b, i, canvasSize / 2, num);
                    break;
            }
        }

        return Bitmap.createBitmap(result, num / 2, num / 2, canvasSize, canvasSize);
    }

    private static void drawOneToPosition(Canvas canvas, Bitmap bitmap, int position, int size, int num) {
        Matrix matrix = new Matrix();
        if (bitmap.getHeight() == size * 2) {
            matrix.postScale(1f, 1f);
        } else {
            matrix.postScale(2f, 2f);
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Bitmap bitmap2 = addLine(bitmap, num);
        canvas.drawBitmap(bitmap2, 0, 0, null);
    }

    private static void drawTwoToPosition(Canvas canvas, Bitmap bitmap, int position, int size, int num) {
        Matrix matrix = new Matrix();
        matrix.postScale(2f, 2f);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap = scaleBitmap(bitmap, 2f, 4f);


        Bitmap bitmap2 = addLine(bitmap, num);
        canvas.drawBitmap(bitmap2, position * size, 0, null);
    }

    private static void drawThreeToPosition(Canvas canvas, Bitmap bitmap, int position, int size, int num) {
        int top = position % 3 / 2;
        int left = 3 - position > 2 ? 0 : 1;
        if (position == 0) {
            Matrix matrix = new Matrix();
            matrix.postScale(2f, 2f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = scaleBitmap(bitmap, 2f, 4f);
        }

        Bitmap bitmap2 = addLine(bitmap, num);
        canvas.drawBitmap(bitmap2, left * size, top * size, null);
    }

    private static void drawFourToPosition(Canvas canvas, Bitmap bitmap, int position, int size, int num) {
        int top = 4 - position > 2 ? 0 : 1;
        int left = position % 2;
        Bitmap bitmap2 = addLine(bitmap, num);
        canvas.drawBitmap(bitmap2, left * size, top * size, null);
    }

    private static Bitmap addLine(Bitmap bitmap, int num) {
//        int size = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
//        int num = 4;
//        int sizebig = size + num;
        // Back image
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth() + num, bitmap.getHeight() + num, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        // white
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(bitmap, num / 2f, num / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        // Drawn square
        canvas.drawRect(0, 0, bitmap.getWidth() + num, bitmap.getHeight() + num, paint);
        bitmap.recycle();
        return newBitmap;
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, float w, float h) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        float x = 0, y = 0, scaleWidth = width, scaleHeight = height;
        Bitmap newbmp;
        //Log.e("gacmy","width:"+width+" height:"+height);
        if (w > h) { // When the proportional width is greater than the height
            float scale = w / h;
            float tempH = width / scale;
            if (height > tempH) {//
                x = 0;
                y = (height - tempH) / 2;
                scaleWidth = width;
                scaleHeight = tempH;
            } else {
                scaleWidth = height * scale;
                x = (width - scaleWidth) / 2;
                y = 0;
            }
        } else if (w < h) { // When the proportional width is smaller than the height
            float scale = h / w;
            float tempW = height / scale;
            if (width > tempW) {
                y = 0;
                x = (width - tempW) / 2;
                scaleWidth = tempW;
                scaleHeight = height;
            } else {
                scaleHeight = width * scale;
                y = (height - scaleHeight) / 2;
                x = 0;
                scaleWidth = width;
            }
        } else { // When the proportions of width and height are equal
            if (width > height) {
                x = (width - height) / 2;
                y = 0;
                scaleHeight = height;
                scaleWidth = height;
            } else {
                y = (height - width) / 2;
                x = 0;
                scaleHeight = width;
                scaleWidth = width;
            }
        }
        try {
            newbmp = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) scaleWidth, (int) scaleHeight, null, false);// createBitmap()方法中定义的参数x+width要小于或等于bitmap.getWidth()，y+height要小于或等于bitmap.getHeight()
            //bitmap.recycle();
        } catch (Exception ignored) {
            return null;
        }
        return newbmp;
    }

    /**
     * Read the picture from the local, get the bitmap through the path
     *
     * @param pathName Picture path
     * @return Bitmap
     */
    public synchronized static Bitmap getBitmapFromLocal(String pathName) {
        return BitmapFactory.decodeFile(pathName);
    }

    protected static int sizeOf(Bitmap data, float density) {
        Bitmap.Config config = data.getConfig();
        switch (config) {
            case ALPHA_8:// size=wh
                return data.getWidth() * data.getHeight();
            case RGB_565:
            case ARGB_4444: // size=wh2
                return data.getWidth() * data.getHeight() * 2;
            case ARGB_8888: // size=wh4
                return data.getWidth() * data.getHeight() * 4;
            case RGBA_F16:// size=wh8
                return data.getWidth() * data.getHeight() * 8;
            case HARDWARE:
            default:
                return data.getAllocationByteCount();
        }
    }

    private static long bitmapToDiskSize(Bitmap bitmap, Bitmap.CompressFormat format, String tmpPath, String name) throws Exception {
        //create a file to write bitmap data
        File f = new File(tmpPath, name);
        f.createNewFile();

        //Convert bitmap to byte array
//        Bitmap bitmap = your bitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(format, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return f.length();
    }

    /**
     * @param bitmap
     * @param format
     * @param scale
     * @param executableCount
     * @param standard
     * @param deviation
     * @return
     */
    public static synchronized Bitmap martixCompression(String path, Bitmap bitmap, Bitmap.CompressFormat format, float scale, int executableCount, long standard, long deviation) {
        Range<Long> allowable = Range.closed(standard - deviation, standard + deviation);
        try {
            File temp = new File(path + "/fileSize/");
            if (!temp.exists()) {
                temp.mkdir();
            }
            long size = bitmapToDiskSize(bitmap, format, path, "fileSize") / 1024;
            if (allowable.contains(size) || size <= allowable.lowerEndpoint()) {
                return bitmap;
            }

            float[] scales = new float[]{scale / 2, scale};
            Bitmap bbm = null;

            int count = 0;
            while (!allowable.contains(size) && count != executableCount) {
                count++;
                Matrix matrix = new Matrix();
                matrix.setScale(scales[0], scales[0]);
                bbm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                size = bitmapToDiskSize(bbm, format, temp.getPath(), "fileSize") / 1024;
                long lower = allowable.lowerEndpoint(); // 450
                long upper = allowable.upperEndpoint(); // 550
                float distance = Math.abs((scales[1] - scales[0]) / 2);
                if (size < lower) {
                    scales = new float[]{scales[0] + distance, scales[0]};
                } else if (size > upper) {
                    scales = new float[]{scales[0] - distance, scales[0]};
                }
            }
            return bbm;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return bitmap;
        }
    }

    public static File bitmapToFile(Bitmap bitmap, String tmpPath, String name) throws Exception {
        //create a file to write bitmap data
        File f = new File(tmpPath, name);

        if (!Objects.requireNonNull(f.getParentFile()).exists()) {
            f.getParentFile().mkdirs();
        }

        f.createNewFile();

        //Convert bitmap to byte array
//        Bitmap bitmap = your bitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return f;
    }


    /**
     * Convert to Bitmap after obtaining Drawable
     *
     * @param ctx
     * @param drawableId
     * @return
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Bitmap drawableToBitmap(Context ctx, int drawableId) {
        Drawable drawable = ResourcesCompat.getDrawable(ctx.getResources(), drawableId, null);
        return drawableToBitmap(drawable);
    }

    /**
     * Drawable Convert to Bitmap
     *
     * @param drawable
     * @return
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
            drawable.getIntrinsicWidth(),
            drawable.getIntrinsicHeight(),
            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    public static Bitmap decodeResourceScaledBitmap(Context context, int resId, int dstWidth, int dstHeight, boolean filter) {
//        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), resId), dstWidth, dstHeight, filter);
        return Bitmap.createScaledBitmap(drawableToBitmap(context, resId), dstWidth, dstHeight, filter);
    }
}
