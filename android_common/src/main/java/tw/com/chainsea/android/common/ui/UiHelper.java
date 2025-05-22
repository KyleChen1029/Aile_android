package tw.com.chainsea.android.common.ui;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.recyclerview.widget.RecyclerView;

public class UiHelper {

    /**
     * Recalculate ListView height
     *
     * @param listView
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static void changeListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null) return;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = (totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)));
        listView.setLayoutParams(params);
    }

    /**
     * Recalculate GridView height
     *
     * @param gridView
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static void changeGridViewHeightBasedOnChildren(GridView gridView, int numColumns) {
        if (gridView == null) return;
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) return;
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount() / numColumns; i++) {
            View listItem = listAdapter.getView(i, null, gridView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight + gridView.getMeasuredHeight() * (listAdapter.getCount());
        gridView.setLayoutParams(params);
    }

    public static int dip2px(Context context, float dip, float offset) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + offset);
    }

    public static int dip2px(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale);
    }

    /**
     * Convert from px (pixel) unit to dp according to the resolution of the phone
     *
     * @param context
     * @param pxValue
     */
    public static int px2dip(Context context, float pxValue, float offset) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + offset);
    }

    /**
     * Convert from px (pixel) unit to dp according to the resolution of the phone
     *
     * @param context
     * @param pxValue
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale);
    }

    /**
     * Convert sp value to px value
     */
    public static int sp2px(Context context, float sp, float offset) {
        return (int) (sp * context.getResources().getDisplayMetrics().scaledDensity + offset);
    }

    /**
     * Convert the px value to sp value to ensure that the text size remains unchanged
     */
    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * Convert the px value to sp value to ensure that the text size remains unchanged
     */
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * Covert dp to px
     *
     * @param ctx
     * @param dp
     * @return pixel
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static float dp2px(Context ctx, float dp) {
        return dp * getDisplayDensity(ctx);
    }

    /**
     * Covert px 轉 dp
     *
     * @param ctx
     * @param px
     * @return dp
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static float px2dp(Context ctx, float px) {
        return px / getDisplayDensity(ctx);
    }

    private static DisplayMetrics getDisplayMetrics(Context ctx) {
        return ctx.getResources().getDisplayMetrics();
    }

    /**
     * Get screen density
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     *
     * @param ctx
     * @return float
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static float getDisplayDensity(Context ctx) {
        return getDisplayMetrics(ctx).density;
    }

    /**
     * Get screen density
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     *
     * @param activity
     * @return float
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static float getDisplayDensity(Activity activity) {
        return getDisplayMetrics(activity).density;
    }

    /**
     * Get screen Dots Per Inch
     *
     * @param ctx
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public int getDensityDpi(Context ctx) {
        return getDisplayMetrics(ctx).densityDpi;
    }

    /**
     * Get screen Dots Per Inch
     *
     * @param activity
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDensityDpi(Activity activity) {
        return getDisplayMetrics(activity).densityDpi;
    }

    /**
     * Get screen width
     *
     * @param ctx
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDisplayWidth(Context ctx) {
        return getDisplayMetrics(ctx).widthPixels;
    }

    /**
     * Get screen width
     *
     * @param activity
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDisplayWidth(Activity activity) {
        return getDisplayMetrics(activity).widthPixels;
    }

    /**
     * Get screen height
     *
     * @param ctx
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDisplayHeight(Context ctx) {
        return getDisplayMetrics(ctx).heightPixels;
    }

    /**
     * Get screen height
     *
     * @param activity
     * @return int
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static int getDisplayHeight(Activity activity) {
        return getDisplayMetrics(activity).heightPixels;
    }

    private static DisplayMetrics getDisplayMetrics(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    public static boolean canScrollBottom(RecyclerView recyclerView) {
        return recyclerView.canScrollVertically(1);
    }
    private static long lastClickTime = 0;
    private static final int MIN_CLICK_DELAY_TIME = 500;
    public static boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            return false;
        } else {
            return true;
        }
    }
//    public static boolean canScrollBottom(RecyclerView recyclerView, int lastVisibleItemPosition){
//        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//        int visibleItemCount = layoutManager.getChildCount();
//        //当前RecyclerView的所有子项个数
//        int totalItemCount = layoutManager.getItemCount();
//        //RecyclerView的滑动状态
//        int state = recyclerView.getScrollState();
//        if(visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1 && state == recyclerView.SCROLL_STATE_IDLE){
//            return true;
//        }else {
//            return false;
//        }
//    }

}
