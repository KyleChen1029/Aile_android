package tw.com.chainsea.android.common.client.callback.impl;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import okhttp3.ResponseBody;
import tw.com.chainsea.android.common.client.exception.ClientRequestException;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

/**
 * current by evan on 2020-08-20
 *
 * @author Evan Wang
 * @date 2020-08-20
 */
public abstract class EntityCallBack<T> extends ACallBack {

    public EntityCallBack(boolean mainThreadEnable) {
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

    /**
     * Custom response failure function
     *
     * @param e
     * @param errorMsg
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    abstract public void onFailure(Exception e, String errorMsg);

    @Override
    public void onSuccess(ResponseBody body, boolean mainThreadEnable) {
        Class<T> tClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        try {
            String respData = body.string();
            if (respData == null) {
                onFailure(new ClientRequestException("response data == null"), "response data == null");
                return;
            }
            if (mainThreadEnable) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onSuccess(JsonHelper.getInstance().from(respData, tClass)));
            } else {
                onSuccess(JsonHelper.getInstance().from(respData, tClass));
            }
        } catch (IOException e) {
            onFailure(e, e.getMessage());
        }
    }

}
