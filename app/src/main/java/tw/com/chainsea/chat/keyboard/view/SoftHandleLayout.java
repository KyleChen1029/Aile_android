package tw.com.chainsea.chat.keyboard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.keyboard.utils.Utils;


public class SoftHandleLayout extends SoftListenLayout {

    protected Context mContext;
    int keyboardHeight;
    protected int mAutoHeightLayoutId;
    protected LinearLayout mAutoHeightLayoutView;
    private int changeHeight = -1;

    private boolean isBottomOpen;

    public void setBottomOpen(boolean bottomOpen) {
        this.isBottomOpen = bottomOpen;
    }

    public SoftHandleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
//        int keyboardHeight = Utils.getDefKeyboardHeight(mContext);
//        Log.d("SoftHandleLayout", "keyboardHeight = " + keyboardHeight);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        int childSum = getChildCount();
        if (childSum > 1) {
            throw new IllegalStateException("can host only one direct child");
        }
        super.addView(child, index, params);

        if (childSum == 0) {
            mAutoHeightLayoutId = child.getId();
            if (mAutoHeightLayoutId < 0) {
                child.setId(R.id.main_view_id);
                mAutoHeightLayoutId = R.id.main_view_id;
            }
            LayoutParams paramsChild = (LayoutParams) child.getLayoutParams();
            paramsChild.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            child.setLayoutParams(paramsChild);
        } else if (childSum == 1) {
            LayoutParams paramsChild = (LayoutParams) child.getLayoutParams();
            paramsChild.addRule(RelativeLayout.ABOVE, mAutoHeightLayoutId);
            child.setLayoutParams(paramsChild);
        }
    }

    public void setAutoHeightLayoutView(LinearLayout view) {
        mAutoHeightLayoutView = view;
    }

    public void setAutoViewHeight(final int height) {
        if (height == 0) {
            mAutoHeightLayoutView.setVisibility(GONE);
        } else {
            mAutoHeightLayoutView.setVisibility(VISIBLE);

        }
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mAutoHeightLayoutView.getLayoutParams();
        params.height = height;
        mAutoHeightLayoutView.setLayoutParams(params);
        mAutoHeightLayoutView.requestLayout();
    }

    public void hideAutoView() {
        post(() -> setAutoViewHeight(0));
    }

    protected void showAutoView(int height) {
        post(() -> setAutoViewHeight(height));
        this.changeHeight = -1;
    }

    @Override
    public void OnSoftKeyboardPop(int height) {
//        if (height > 0 && height != keyboardHeight ) {
        if (height > 0 && height != keyboardHeight) {
            keyboardHeight = height;
            Utils.setDefKeyboardHeight(mContext, keyboardHeight);
        }
        showAutoView(this.isBottomOpen ? 0 : keyboardHeight);

//        CELog.d("鍵盤變化高度:: " + height );
//        if (keyboardHeight == 0 && height != keyboardHeight) {
//            keyboardHeight = height;
//            Utils.setDefKeyboardHeight(mContext, keyboardHeight);
//        }
//
//        if (this.changeHeight > 0) {
//            showAutoView(changeHeight);
//            return;
//        }
//
//        if (height == keyboardHeight) {
//            showAutoView(height);
//        } else if (height < keyboardHeight) {
//        } else if (height > keyboardHeight) {
//            showAutoView(keyboardHeight);
//        } else {
//            if (isKeyboardOpen && height < keyboardHeight) {
//                showAutoView(keyboardHeight);
//            } else {
//                showAutoView(isKeyboardOpen ? keyboardHeight : 0);
//            }
//        }
//        isKeyboardOpen = false;
    }

    @Override
    public void OnSoftKeyboardClose() {
        if (this.changeHeight == -1) {
            hideAutoView();
        }
    }

    protected void changeHeight(int changeHeight) {
        this.changeHeight = changeHeight;
    }

    /**
     * display soft keyboard
     */
    protected void openSoftKeyboard(EditText et) {
        InputMethodManager inputManager = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et, 0);
    }

    /**
     * close soft keyboard
     */
    protected void closeSoftKeyboard(EditText et) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && et != null && et.getWindowToken() != null) {
            inputMethodManager.hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
