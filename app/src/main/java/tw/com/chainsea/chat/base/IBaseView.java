package tw.com.chainsea.chat.base;

import android.content.Context;
import androidx.annotation.StringRes;

/**
 * IBaseView
 * Created by Fleming on 2017/1/12.
 */
public interface IBaseView {
    void initListener();

//    void showLoadingView(String tip);

    void showLoadingView(@StringRes int  resId);

    void hideLoadingView();

    Context getCtx();

    void showToast(int resId);
}
