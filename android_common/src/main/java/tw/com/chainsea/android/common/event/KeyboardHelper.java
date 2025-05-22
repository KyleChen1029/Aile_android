package tw.com.chainsea.android.common.event;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardHelper {

    public static void hide(View v) {
        try {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }

    public static void postHide(View v) {
        v.post(() -> {
            try {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            } catch (Exception e) {
                Log.e("TAG", e.getMessage());
            }
        });
    }


    public static void open(View v) {
        InputMethodManager inputManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        // Lets soft keyboard trigger only if no physical keyboard present
//        inputManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


    public static void postOpen(View v) {
        v.post(() -> {
            InputMethodManager inputManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(v, 0);
        });
    }


//    public static boolean isSoftInputShow(Activity activity) {
//
//        // The virtual keyboard is hidden to determine whether the view is empty
//        View view = activity.getWindow().peekDecorView();
//        if (view != null) {
//            // 隐藏虚拟键盘
//            InputMethodManager inputmanger = (InputMethodManager) activity
//                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
////       inputmanger.hideSoftInputFromWindow(view.getWindowToken(),0);
//
//            return inputmanger.isActive() && activity.getWindow().getCurrentFocus() != null;
//        }
//        return false;
//    }


//    public static int getVisibleHeight(View rootView) {
//        // Get the size of the current root view displayed on the screen
//        Rect r = new Rect();
//        // Get the root View in the visible area of ​​the form
//        rootView.getWindowVisibleDisplayFrame(r);
//        int visibleHeight = r.height();
//        return visibleHeight;
////        int rootViewVisibleHeight = 0;
////        if (rootViewVisibleHeight == 0) {
////            rootViewVisibleHeight = visibleHeight;
////            return;
////        }
////
////        // The display height of the root view has not changed, which can be regarded as the soft keyboard display/hide status has not changed
////        if (rootViewVisibleHeight == visibleHeight) {
////            return;
////        }
////
////        // The root view display height becomes smaller than 200, which can be seen as a soft keyboard display
////        if (rootViewVisibleHeight - visibleHeight > 200) {
////            if (onSoftKeyBoardChangeListener != null) {
////                onSoftKeyBoardChangeListener.keyBoardShow(rootViewVisibleHeight - visibleHeight);
////            }
////            rootViewVisibleHeight = visibleHeight;
////            return;
////        }
////
////        // The root view display height becomes larger than 200, which can be seen as the soft keyboard hidden
////        if (visibleHeight - rootViewVisibleHeight > 200) {
////            if (onSoftKeyBoardChangeListener != null) {
////                onSoftKeyBoardChangeListener.keyBoardHide(visibleHeight - rootViewVisibleHeight);
////            }
////            rootViewVisibleHeight = visibleHeight;
////            return;
////        }
//    }
}
