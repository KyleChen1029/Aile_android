package tw.com.chainsea.android.common.client.method;

import android.annotation.SuppressLint;

import java.net.URL;
import java.util.Map;

import okhttp3.MediaType;
import tw.com.chainsea.android.common.client.ClientsManager;
import tw.com.chainsea.android.common.client.callback.intf.ICallBack;
import tw.com.chainsea.android.common.client.helper.ParamsHelper;
import tw.com.chainsea.android.common.client.intf.IHttpClientsAsyncDelete;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

public class AsyncDelete implements IHttpClientsAsyncDelete {
    private static final String TAG = AsyncDelete.class.getSimpleName();

    private static AsyncDelete INSTANCE = new AsyncDelete();

    public static AsyncDelete getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AsyncDelete();
        }
        return INSTANCE;
    }

    public void execute(String url, ICallBack callBack) {
        getInstance().doDelete(url, null, null, "", callBack);
    }

    public void execute(String url, String reqData, ICallBack callBack) {
        getInstance().doDelete(url, null, null, reqData, callBack);
    }

    public void execute(String url, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doDelete(url, null, null, reqData, callBack);
    }

    public void execute(URL url, String reqData, ICallBack callBack) {
        getInstance().doDelete(url.getAuthority(), null, null, reqData, callBack);
    }

    public void execute(URL url, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doDelete(url.getAuthority(), null, null, reqData, callBack);
    }

    public void execute(String url, MediaType mediaType, String reqData, ICallBack callBack) {
        getInstance().doDelete(url, mediaType, null, reqData, callBack);
    }

    public void execute(String url, MediaType mediaType, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doDelete(url, mediaType, null, reqData, callBack);
    }

    public void execute(URL url, MediaType mediaType, String reqData, ICallBack callBack) {
        getInstance().doDelete(url.getAuthority(), mediaType, null, reqData, callBack);
    }

    public void execute(URL url, MediaType mediaType, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doDelete(url.getAuthority(), mediaType, null, reqData, callBack);
    }


    public void execute(String url, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doDelete(url, null, headers, reqData, callBack);
    }

    public void execute(String url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doDelete(url, null, headers, reqData, callBack);
    }

    public void execute(URL url, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doDelete(url.getAuthority(), null, headers, reqData, callBack);
    }

    public void execute(URL url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doDelete(url.getAuthority(), null, headers, reqData, callBack);
    }


    public void execute(String url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doDelete(url, mediaType, headers, reqData, callBack);
    }

    public void execute(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doDelete(url, mediaType, headers, reqData, callBack);
    }

    public void execute(URL url, MediaType mediaType, Map<String, String> headers, String reqData, ICallBack callBack) {
        getInstance().doDelete(url.getAuthority(), mediaType, headers, reqData, callBack);
    }

    public void execute(URL url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        getInstance().doDelete(url.getAuthority(), mediaType, headers, reqData, callBack);
    }


    @Override
    public void doDelete(String url, MediaType mediaType, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
        String data = ParamsHelper.buildParamsToJsonString(reqData);
        doDelete(url, mediaType, headers, data, callBack);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void doDelete(final String url, final MediaType mediaType, final Map<String, String> headers, final String reqData, final ICallBack callBack) {
        ThreadExecutorHelper.getApiExecutor().execute(() -> {
//            callBack.response(ClientsManager.delete(url, mediaType, headers, reqData));
            ClientsManager.delete(url, mediaType, headers, reqData, callBack);
        });
//        new AsyncTask<Void, Void, Response>() {
//
//            @Override
//            protected Response doInBackground(Void... voids) {
//                return ClientsManager.delete(url, mediaType, headers, reqData);
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

}
