package tw.com.chainsea.android.common.client.callback.impl;

import okhttp3.ResponseBody;

/**
 * current by evan on 2020-08-20
 *
 * @author Evan Wang
 * @date 2020-08-20
 */
public abstract class EntitiesCallBack<T> extends ACallBack {

    public EntitiesCallBack() {
    }

    public EntitiesCallBack(boolean mainThreadEnable) {
        super(mainThreadEnable);
    }

    /**
     * Custom response success function
     *
     * @param t
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    abstract public void onSuccess(T t);

    public abstract void onFailure(Exception e, String errorMsg);

    @Override
    public void onSuccess(ResponseBody body, boolean mainThreadEnable) {

    }
}
