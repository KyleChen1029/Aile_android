package tw.com.chainsea.chat.widget.popup;

import android.view.View;
import android.widget.PopupWindow;

/**
 * Created by sunhui on 2018/4/28.
 */

public class MyPopupWindow extends PopupWindow {

    public MyPopupWindow(View contentView, int matchParent, int wrapContent) {
        super(contentView, matchParent, wrapContent);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mDismissListenner != null) {
            mDismissListenner.onDismiss();
        }
    }

    private DismissListenner mDismissListenner;

    public void setDismissListenner(DismissListenner dismissListenner) {
        mDismissListenner = dismissListenner;
    }

    public interface DismissListenner {
        void onDismiss();
    }
}
