package tw.com.chainsea.android.common.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

/**
 * ImagesHelper Image tools
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public class ImagesHelper {

    /*
     * According to the resource name, set imageView Resource, if the resource is found, you can set the default resource
     *
     * @param ctx
     * @param view
     * @param defType
     * @param name
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
//    public static void setResourceByName(Context ctx, ImageView view, DefType defType, String name, String defName) {
//        if (name != null && !"".equals(name)) {
//            int id = ctx.getResources().getIdentifier(name, defType.getType(), ctx.getPackageName());
//            if (0 == id) {
//                int defId = ctx.getResources().getIdentifier(defName, defType.getType(), ctx.getPackageName());
//                view.setImageResource(defId);
//            } else {
//                view.setImageResource(id);
//            }
//        }
//    }
    @SuppressLint("DiscouragedApi")
    public static Drawable setDrawableByName(Context ctx, DefType defType, String name, int defId) {
        if (name != null && !name.isEmpty()) {
            int id = ctx.getResources().getIdentifier(name, defType.getType(), ctx.getPackageName());
            if (0 != id) {
                return ResourcesCompat.getDrawable(ctx.getResources(), id, null);
            }
        }
        return ResourcesCompat.getDrawable(ctx.getResources(), defId, null);
    }

    /**
     * Avoid Null, get ImageView width
     *
     * @param view
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getWidth(ImageView view) {
        try {
            return imageView2Bitmap(view).getWidth();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Avoid Null, get ImageView height
     *
     * @param view
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getHeight(ImageView view) {
        try {
            return imageView2Bitmap(view).getHeight();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get ImageView to Bitmap
     *
     * @param view
     * @return Bitmap
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static Bitmap imageView2Bitmap(ImageView view) {
        if (view == null) {
            return null;
        }
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        return b;
    }

    /**
     * Get the image resource width according to resId
     *
     * @param ctx
     * @param resId
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getWidth(Context ctx, int resId) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            BitmapFactory.decodeResource(ctx.getResources(), resId, bounds);
            bounds.inJustDecodeBounds = true;
            return bounds.outWidth;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get image resource height according to resId
     *
     * @param ctx
     * @param resId
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getHeight(Context ctx, int resId) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            BitmapFactory.decodeResource(ctx.getResources(), resId, bounds);
            bounds.inJustDecodeBounds = true;
            return bounds.outHeight;
        } catch (Exception e) {
            return 0;
        }
    }


}
