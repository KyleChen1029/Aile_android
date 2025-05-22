package tw.com.chainsea.android.common.client.intf;

import tw.com.chainsea.android.common.client.callback.intf.ICallBack;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;


/**
 * IHttpClientsPost Http POST request tool interface
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public interface IHttpClientsPost {

//    void doPost(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

    void doPost(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack);

    void doPostFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, ICallBack callBack);

//    void doPostFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, Map<String, File> fileData, ICallBack callBack);

    void doPostFileName(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, String fileName, ICallBack callBack);

}
