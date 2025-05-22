package tw.com.chainsea.android.common.client.intf;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import tw.com.chainsea.android.common.client.callback.intf.ICallBack;


public interface IHttpClientsAsyncPost {

//    void doThreadPost(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

    void doThreadPost(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack);

    void doThreadPostFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, ICallBack callBack);

//    void doThreadPostFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, Map<String, File> fileData, ICallBack callBack);

    void doThreadPostFileName(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, String fileName, ICallBack callBack);

}
