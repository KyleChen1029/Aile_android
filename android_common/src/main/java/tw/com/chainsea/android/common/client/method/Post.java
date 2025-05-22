package tw.com.chainsea.android.common.client.method;

import java.io.File;
import java.util.Map;
import okhttp3.MediaType;
import tw.com.chainsea.android.common.client.ClientsManager;
import tw.com.chainsea.android.common.client.abs.APost;
import tw.com.chainsea.android.common.client.callback.intf.ICallBack;
import tw.com.chainsea.android.common.client.intf.IHttpClientsPost;

public class Post extends APost implements IHttpClientsPost {
    private static Post INSTANCE = new Post();

    public static Post getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Post();
        }
        return INSTANCE;
    }

    @Override
    public void execute(String url, ICallBack callBack) {
        getInstance().doPost(url, null, null, "", callBack);
    }

//    @Override
//    public final void execute(String url, MediaType mediaType, ICallBack callBack) {
//        getInstance().doPost(url, mediaType, null, "", callBack);
//    }

//    @Override
//    public final void execute(String url, String reqData, ICallBack callBack) {
//        getInstance().doPost(url, null, null, reqData, callBack);
//    }

//    @Override
//    public final void execute(String url, Map<String, String> reqData, ICallBack callBack) {
//        getInstance().doPost(url, null, null, reqData, callBack);
//    }

//    @Override
//    public final void execute(URL url, String reqData, ICallBack callBack) {
//        getInstance().doPost(url.getAuthority(), null, null, reqData, callBack);
//    }

//    @Override
//    public final void execute(URL url, Map<String, String> reqData, ICallBack callBack) {
//        getInstance().doPost(url.getAuthority(), null, null, reqData, callBack);
//    }

    @Override
    public final void execute(String url, MediaType mediaType, String reqData, ICallBack callBack) {
        getInstance().doPost(url, mediaType, null, reqData, callBack);
    }

//    @Override
//    public final void execute(String url, MediaType mediaType, Map<String, String> reqData, ICallBack callBack) {
//        getInstance().doPost(url, mediaType, null, reqData, callBack);
//    }

//    @Override
//    public final void execute(URL url, MediaType mediaType, String reqData, ICallBack callBack) {
//        getInstance().doPost(url.getAuthority(), mediaType, null, reqData, callBack);
//    }

//    @Override
//    public final void execute(URL url, MediaType mediaType, Map<String, String> reqData, ICallBack callBack) {
//        getInstance().doPost(url.getAuthority(), mediaType, null, reqData, callBack);
//    }

    @Override
    public final void execute(String url, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doPost(url, null, headers, reqData, callBack);
    }

//    @Override
//    public final void execute(String url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
//        getInstance().doPost(url, null, headers, reqData, callBack);
//    }

//    @Override
//    public final void execute(URL url, Map<String, String> headers, String reqData, ICallBack callBack) {
//        getInstance().doPost(url.getAuthority(), null, headers, reqData, callBack);
//    }

//    @Override
//    public final void execute(URL url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
//        getInstance().doPost(url.getAuthority(), null, headers, reqData, callBack);
//    }

    @Override
    public final void execute(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doPost(url, mediaType, headers, reqData, callBack);
    }

//    @Override
//    public final void execute(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
//        getInstance().doPost(url, mediaType, headers, reqData, callBack);
//    }

//    @Override
//    public final void execute(URL url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack) {
//        getInstance().doPost(url.getAuthority(), mediaType, headers, reqData, callBack);
//    }

//    @Override
//    public final void execute(URL url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
//        getInstance().doPost(url.getAuthority(), mediaType, headers, reqData, callBack);
//    }

    @Override
    public final void execute(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, String reqKey, File file, final ICallBack callBack) {
        getInstance().doPostFile(url, mediaType, headers, formDataPart, reqKey, file, callBack);
    }

    @Override
    public void execute(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, String fileName, ICallBack callBack) {
        getInstance().doPostFileName(url, mediaType, headers, formDataPart, reqKey, file, fileName, callBack);
    }

//    @Override
//    public final void execute(final URL url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, String reqKey, File file, final ICallBack callBack) {
//        getInstance().doPostFile(url.getAuthority(), mediaType, headers, formDataPart, reqKey, file, callBack);
//    }

//    @Override
//    public void execute(URL url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, String fileName, ICallBack callBack) {
//        getInstance().doPostFileName(url.getAuthority(), mediaType, headers, formDataPart, reqKey, file, fileName, callBack);
//    }

//    @Override
//    public final void execute(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, Map<String, String> fileData, File file, final ICallBack callBack) {
//        getInstance().doPostFileName(url, mediaType, headers, formDataPart, fileData, file, callBack);
//    }

//    @Override
//    public final void execute(final URL url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, Map<String, File> fileData, final ICallBack callBack) {
//        getInstance().doPostFile(url.getAuthority(), mediaType, headers, formDataPart, fileData, callBack);
//    }

//    @Override
//    public void doPost(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
//        String data = ParamsHelper.buildParamsToJsonString(reqData);
//        doPost(url, mediaType, headers, data, callBack);
//    }

    @Override
    public void doPost(final String url, final MediaType mediaType, final Map<String, String> headers, final String reqData, final ICallBack callBack) {
        ClientsManager.asyncPost(null, url, mediaType, headers, reqData, callBack);
//        ClientsManager.post(url, mediaType, headers, reqData, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                callBack.response(null);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                callBack.response(response);
//            }
//        });
    }

    @Override
    public void doPostFile(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, final String reqKey, final File file, final ICallBack callBack) {
        ClientsManager.asyncPostFile(null, url, mediaType, headers, formDataPart, reqKey, file, callBack);
    }

//    public void doPostFileName(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, Map<String, String> fileData, File file, final ICallBack callBack) {
//        if (fileData == null || fileData.isEmpty() || fileData.size() != 1) {
//            callBack.response(null);
//            return;
//        }
//        String reqKsy = null;
//        String fileName = null;
//        for (Map.Entry<String, String> fd : fileData.entrySet()) {
//            reqKsy = fd.getKey();
//            fileName = fd.getValue();
//        }
//        doPostFileName(url, mediaType, headers, formDataPart, reqKsy, file, fileName, callBack);
//    }

//    @Override
//    public void doPostFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, Map<String, File> fileData, ICallBack callBack) {
//        if (fileData == null || fileData.isEmpty() || fileData.size() != 1) {
//            callBack.response(null);
//            return;
//        }
//        String reqKsy = null;
//        File file = null;
//        for (Map.Entry<String, File> fd : fileData.entrySet()) {
//            reqKsy = fd.getKey();
//            file = fd.getValue();
//        }
//        doPostFile(url, mediaType, headers, formDataPart, reqKsy, file, callBack);
//    }

    @Override
    public void doPostFileName(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, String reqKey, File file, String fileName, ICallBack callBack) {
        ClientsManager.asyncPostFile(null, url, headers, formDataPart, reqKey, file, fileName, callBack);
    }

}
