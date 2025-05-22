package tw.com.chainsea.ce.sdk.http.cp.base;

public interface CpApiListener<E> {
    void onSuccess(E e);
    void onFailed(String errorCode, String errorMessage);
}
