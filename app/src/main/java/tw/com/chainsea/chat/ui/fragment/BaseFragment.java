package tw.com.chainsea.chat.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;

/**
 * Created by sunhui on 2017/5/11.
 */

public class BaseFragment<E extends Enum, D> extends Fragment {

    protected OnActionListener onActionListener;
    private int statusHeight = 0;

    @Override
    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int resourceId = requireContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            this.statusHeight = requireContext().getResources().getDimensionPixelSize(resourceId);
        } else {
            this.statusHeight = UiHelper.dip2px(requireContext(), 24);
        }
    }

    public String getUserId() {
        return TokenPref.getInstance(this.getContext()).getUserId();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void refresh() {
    }

    public E getType() {
        return null;
    }

    public void setData(D d) {
    }

    public void setData(List<D> list) {
    }

    public void setKeyword(String keyword) {
    }

    public int getDataCount() {
        return 0;
    }

    public int getStatus() {
        return -1;
    }


    public void setOnActionListener(OnActionListener<D> onActionListener) {
        this.onActionListener = onActionListener;
    }

    public interface OnActionListener<D> {
        void action(String action, D d);
    }

    protected int getStatusHeight() {
        return this.statusHeight;
    }
}
