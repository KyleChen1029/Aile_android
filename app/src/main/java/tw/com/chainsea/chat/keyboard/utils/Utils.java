package tw.com.chainsea.chat.keyboard.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class Utils {
    private static final String EXTRA_DEF_KEYBOARDHEIGHT = "DEF_KEYBOARDHEIGHT";
    private static int sDefKeyboardHeight = 0;

    public static int getDefKeyboardHeight(Context context) {
        if (sDefKeyboardHeight == 0) {   //evaluate keyboard height
            sDefKeyboardHeight = getDisplayHeightPixels(context) * 3 / 7;
        }
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int height = settings.getInt(EXTRA_DEF_KEYBOARDHEIGHT, 0);
        if (height > 0 && sDefKeyboardHeight != height) {
            Utils.setDefKeyboardHeight(context, height);
        }
        return sDefKeyboardHeight;
    }

    public static void setDefKeyboardHeight(Context context, int height) {
        if (height > sDefKeyboardHeight) {
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            settings.edit().putInt(EXTRA_DEF_KEYBOARDHEIGHT, height).apply();
        }
        Utils.sDefKeyboardHeight = height;
    }

    private static int DisplayWidthPixels = 0;
    private static int DisplayHeightPixels = 0;

    private static void getDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.
            getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        DisplayWidthPixels = dm.widthPixels;
        DisplayHeightPixels = dm.heightPixels;
    }

    public static int getDisplayHeightPixels(Context context) {
        if (context == null) {
            return -1;
        }
        if (DisplayHeightPixels == 0) {
            getDisplayMetrics(context);
        }
        return DisplayHeightPixels;
    }

    public static int getDisplayWidthPixels(Context context) {
        if (context == null) {
            return -1;
        }
        if (DisplayWidthPixels == 0) {
            getDisplayMetrics(context);
        }
        return DisplayWidthPixels;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
