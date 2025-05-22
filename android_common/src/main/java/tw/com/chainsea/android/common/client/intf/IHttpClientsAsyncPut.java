package tw.com.chainsea.android.common.client.intf;

import tw.com.chainsea.android.common.client.callback.intf.ICallBack;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;


/**
 * IHttpClientsPut Http PUT request tool interface
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public interface IHttpClientsAsyncPut {

    void doPut(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

    void doPut(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack);

    void doPutFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, ICallBack callBack);

    void doPutFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, Map<String, File> fileData, ICallBack callBack);
}
