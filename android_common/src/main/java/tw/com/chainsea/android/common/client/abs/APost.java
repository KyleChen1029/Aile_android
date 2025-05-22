package tw.com.chainsea.android.common.client.abs;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import tw.com.chainsea.android.common.client.callback.intf.ICallBack;

/**
 * current by evan on 12/10/20
 *
 * @author Evan Wang
 * @date 12/10/20
 */
public abstract class APost {

    public abstract void execute(String url, ICallBack callBack);

//    public abstract void execute(String url, MediaType mediaType, ICallBack callBack);

//    public abstract void execute(String url, String reqData, ICallBack callBack);

//    public abstract void execute(String url, Map<String, String> reqData, ICallBack callBack);

//    public abstract void execute(URL url, String reqData, ICallBack callBack);

//    public abstract void execute(URL url, Map<String, String> reqData, ICallBack callBack);

    public abstract void execute(String url, MediaType mediaType, String reqData, ICallBack callBack);

//    public abstract void execute(String url, MediaType mediaType, Map<String, String> reqData, ICallBack callBack);

//    public abstract void execute(URL url, MediaType mediaType, String reqData, ICallBack callBack);

//    public abstract void execute(URL url, MediaType mediaType, Map<String, String> reqData, ICallBack callBack);

    public abstract void execute(String url, Map<String, String> headers, String reqData, ICallBack callBack);

//    public abstract void execute(String url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

//    public abstract void execute(URL url, Map<String, String> headers, String reqData, ICallBack callBack);

//    public abstract void execute(URL url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

    public abstract void execute(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack);

//    public abstract void execute(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

//    public abstract void execute(URL url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack);

//    public abstract void execute(URL url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack);

    public abstract void execute(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, String reqKey, File file, final ICallBack callBack);

    public abstract void execute(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, String reqKey, File file, final String fileName, final ICallBack callBack);

//    public abstract void execute(final URL url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, String reqKey, File file, final ICallBack callBack);

//    public abstract void execute(final URL url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, String reqKey, File file, final String fileName, final ICallBack callBack);

//    public abstract void execute(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, Map<String, String> fileData, File file, final ICallBack callBack);

//    public abstract void execute(final URL url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, Map<String, File> fileData, final ICallBack callBack);


}
