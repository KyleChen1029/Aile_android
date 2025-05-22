package tw.com.chainsea.ce.sdk.service.listener;

public interface ApiCallback<T>{
    void error(String message);
    void complete(T t);
    void finish();
}
