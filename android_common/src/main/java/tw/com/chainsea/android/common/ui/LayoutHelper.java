package tw.com.chainsea.android.common.ui;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * current by evan on 2020-08-12
 *
 * @author Evan Wang
 * @date 2020-08-12
 */
public class LayoutHelper {

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }
}
