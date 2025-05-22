package tw.com.chainsea.ce.sdk.service.listener;

/**
 * current by evan on 12/30/20
 *
 * @author Evan Wang
 * @date 12/30/20
 */
public abstract class AServiceCallBack<T, E extends Enum> implements ServiceCallBack<T, E> {

    public abstract void onProgress(float progress, long total);


    @Override
    public void error(String message) {
    }
}
