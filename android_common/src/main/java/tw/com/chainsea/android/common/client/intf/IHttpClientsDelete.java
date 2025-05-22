package tw.com.chainsea.android.common.client.intf;

import tw.com.chainsea.android.common.client.callback.intf.ICallBack;

import java.util.Map;

import okhttp3.MediaType;

/**
 * IHttpClientsDelete Http DELETE request tool interface
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public interface IHttpClientsDelete {

    void doDelete(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

    void doDelete(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack);
}
