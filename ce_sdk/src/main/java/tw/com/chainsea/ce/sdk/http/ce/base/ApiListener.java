package tw.com.chainsea.ce.sdk.http.ce.base;

/**
 * current by evan on 2019-12-16
 */
public interface ApiListener<E> {
    void onSuccess(E e);
    void onFailed(String errorMessage);
}
