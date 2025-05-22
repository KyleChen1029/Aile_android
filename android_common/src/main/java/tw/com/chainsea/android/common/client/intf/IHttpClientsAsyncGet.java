package tw.com.chainsea.android.common.client.intf;

import tw.com.chainsea.android.common.client.callback.intf.ICallBack;

import java.util.Map;

import okhttp3.HttpUrl;

public interface IHttpClientsAsyncGet {

    void doGet(HttpUrl.Builder httpBuilder, Map<String, String> headers, ICallBack callBack);

    void doGet(String url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

    void doGet(String url, Map<String, String> headers, String reqData, ICallBack callBack);
}
