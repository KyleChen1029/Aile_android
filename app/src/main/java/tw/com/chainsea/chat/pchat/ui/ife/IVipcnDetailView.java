package tw.com.chainsea.chat.pchat.ui.ife;


import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;

/**
 * Created by jerry.yang on 2018/3/19.
 * desc:
 */
public interface IVipcnDetailView {
    void refreashResult(ServiceNumberEntity entity);

    void refreashSubscribeResult(String roomId);

    void showIsTop(boolean isTop);

    void showIsSubscribe(boolean isSubscribe);

    void finish();

    void showLoadingView();

    void hideLoadingView();

    void subscribe();

    void unSubscribe();
}
