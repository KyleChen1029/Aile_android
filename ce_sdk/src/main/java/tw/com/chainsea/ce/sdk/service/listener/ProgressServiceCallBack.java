package tw.com.chainsea.ce.sdk.service.listener;

/**
 * Create by evan on 3/3/21
 *
 * @author Evan Wang
 * @date 3/3/21
 */
public interface ProgressServiceCallBack<T, E extends  Enum> extends ServiceCallBack<T, E>{

    void progress(float progress, long total);
}
