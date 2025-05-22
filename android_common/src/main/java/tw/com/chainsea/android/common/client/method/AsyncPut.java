package tw.com.chainsea.android.common.client.method;

import android.annotation.SuppressLint;

import java.io.File;
import java.net.URL;
import java.util.Map;

import okhttp3.MediaType;
import tw.com.chainsea.android.common.client.ClientsManager;
import tw.com.chainsea.android.common.client.callback.intf.ICallBack;
import tw.com.chainsea.android.common.client.helper.ParamsHelper;
import tw.com.chainsea.android.common.client.intf.IHttpClientsAsyncPut;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

public class AsyncPut implements IHttpClientsAsyncPut {
    private static final String TAG = AsyncPut.class.getSimpleName();

    private static AsyncPut INSTANCE = new AsyncPut();

    public static AsyncPut getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AsyncPut();
        }
        return INSTANCE;
    }

    public void execute(String url, ICallBack callBack) {
        getInstance().doPut(url, null, null, "", callBack);
    }

    public void execute(String url, String reqData, ICallBack callBack) {
        getInstance().doPut(url, null, null, reqData, callBack);
    }

    public void execute(String url, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doPut(url, null, null, reqData, callBack);
    }

    public void execute(URL url, String reqData, ICallBack callBack) {
        getInstance().doPut(url.getAuthority(), null, null, reqData, callBack);
    }

    public void execute(URL url, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doPut(url.getAuthority(), null, null, reqData, callBack);
    }

    public void execute(String url, MediaType mediaType, String reqData, ICallBack callBack) {
        getInstance().doPut(url, mediaType, null, reqData, callBack);
    }

    public void execute(String url, MediaType mediaType, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doPut(url, mediaType, null, reqData, callBack);
    }

    public void execute(URL url, MediaType mediaType, String reqData, ICallBack callBack) {
        getInstance().doPut(url.getAuthority(), mediaType, null, reqData, callBack);
    }

    public void execute(URL url, MediaType mediaType, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doPut(url.getAuthority(), mediaType, null, reqData, callBack);
    }

    public void execute(String url, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doPut(url, null, headers, reqData, callBack);
    }

    public void execute(String url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doPut(url, null, headers, reqData, callBack);
    }

    public void execute(URL url, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doPut(url.getAuthority(), null, headers, reqData, callBack);
    }

    public void execute(URL url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doPut(url.getAuthority(), null, headers, reqData, callBack);
    }

    public void execute(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doPut(url, mediaType, headers, reqData, callBack);
    }

    public void execute(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doPut(url, mediaType, headers, reqData, callBack);
    }

    public void execute(URL url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doPut(url.getAuthority(), mediaType, headers, reqData, callBack);
    }

    public void execute(URL url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doPut(url.getAuthority(), mediaType, headers, reqData, callBack);
    }

    public void execute(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, String reqKey, File file, final ICallBack callBack) {
        getInstance().doPutFile(url, mediaType, headers, formDataPart, reqKey, file, callBack);
    }

    public void execute(final URL url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, String reqKey, File file, final ICallBack callBack) {
        getInstance().doPutFile(url.getAuthority(), mediaType, headers, formDataPart, reqKey, file, callBack);
    }

    public void execute(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, Map<String, File> fileData, final ICallBack callBack) {
        getInstance().doPutFile(url, mediaType, headers, formDataPart, fileData, callBack);
    }

    public void execute(final URL url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, Map<String, File> fileData, final ICallBack callBack) {
        getInstance().doPutFile(url.getAuthority(), mediaType, headers, formDataPart, fileData, callBack);
    }

    @Override
    public synchronized void doPut(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        String data = ParamsHelper.buildParamsToJsonString(reqData);
        doPut(url, mediaType, headers, data, callBack);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public synchronized void doPut(final String url, final MediaType mediaType, final Map<String, String> headers, final String reqData, final ICallBack callBack) {
        ThreadExecutorHelper.getApiExecutor().execute(() -> {
//            callBack.response(ClientsManager.put(url, mediaType, headers, reqData));
            ClientsManager.put(url, mediaType, headers, reqData, callBack);
        });
//        new AsyncTask<Void, Void, Response>() {
//
//            @Override
//            protected Response doInBackground(Void... voids) {
//                return ClientsManager.put(url, mediaType, headers, reqData);
//            }
//
//            @Override
//            protected void onPostExecute(Response resp) {
//                super.onPostExecute(resp);
//                callBack.response(resp);
//
//            }
//        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public synchronized void doPutFile(final String url, final MediaType mediaType, final Map<String, String> headers, final Map<String, String> formDataPart, final String reqKey, final File file, final ICallBack callBack) {
        ThreadExecutorHelper.getApiExecutor().execute(() -> {
            ClientsManager.asyncPostFile(null, url, mediaType, headers, formDataPart, reqKey, file, callBack);
//            callBack.response(ClientsManager.asyncPostFile(null, url, mediaType, headers, formDataPart, reqKey, file));
        });
//        new AsyncTask<Void, Void, Response>() {
//
//            @Override
//            protected Response doInBackground(Void... voids) {
//                return ClientsManager.asyncPostFile(null, url, mediaType, headers, formDataPart, reqKey, file);
//            }
//
//            @Override
//            protected void onPostExecute(Response resp) {
//                super.onPostExecute(resp);
//                callBack.response(resp);
//
//            }
//        }.execute();
    }

    @Override
    public synchronized void doPutFile(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> formDataPart, Map<String, File> fileData, ICallBack callBack) {
        if (fileData == null || fileData.isEmpty() || fileData.size() != 1) {
            callBack.response(null);
            return;
        }

        String reqKsy = null;
        File file = null;
        for (Map.Entry<String, File> fd : fileData.entrySet()) {
            reqKsy = fd.getKey();
            file = fd.getValue();
        }
        doPutFile(url, mediaType, headers, formDataPart, reqKsy, file, callBack);
    }

}
