package tw.com.chainsea.ce.sdk.service.listener;

/**
 * current by evan on 2020-02-18
 */

public interface ServiceCallBack<T, E extends  Enum>{

    void error(String message);

    void complete(T t, E e);
}
