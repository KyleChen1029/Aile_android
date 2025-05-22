package tw.com.chainsea.android.common.client.intf;

import tw.com.chainsea.android.common.client.callback.intf.ICallBack;

import java.util.Map;

import okhttp3.HttpUrl;


/**
 * IHttpClientsGet Http GET request tool interface
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public interface IHttpClientsGet {

    void doGet(HttpUrl.Builder httpBuilder, Map<String, String> headers, ICallBack callBack);

    void doGet(String url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

    void doGet(String url, Map<String, String> headers, String reqData, ICallBack callBack);
}
