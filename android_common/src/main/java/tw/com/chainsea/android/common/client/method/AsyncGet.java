//package tw.com.chainsea.android.common.client.method;
//
//import android.annotation.SuppressLint;
//
//import java.net.URL;
//import java.util.Map;
//
//import okhttp3.HttpUrl;
//import tw.com.chainsea.android.common.client.ClientsManager;
//import tw.com.chainsea.android.common.client.callback.intf.ICallBack;
//import tw.com.chainsea.android.common.client.helper.ParamsHelper;
//import tw.com.chainsea.android.common.client.intf.IHttpClientsAsyncGet;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//
//public class AsyncGet implements IHttpClientsAsyncGet {
//    private static final String TAG = AsyncGet.class.getSimpleName();
//
//    private static AsyncGet INSTANCE = new AsyncGet();
//
//    public static AsyncGet getInstance() {
//        if (INSTANCE == null) {
//            INSTANCE = new AsyncGet();
//        }
//        return INSTANCE;
//    }
//
//    public static AsyncGet newInstance() {
//        return new AsyncGet();
//    }
//
////    public void execute(String url, ICallBack callBack) {
////        getInstance().doGet(url, null, "", callBack);
////    }
//
////    public void execute(String url, String reqData, ICallBack callBack) {
////        getInstance().doGet(url, null, reqData, callBack);
////    }
//
////    public void execute(String url, Map<String, String> reqData, ICallBack callBack) {
////        getInstance().doGet(url, null, reqData, callBack);
////    }
//
////    public void execute(URL url, String reqData, ICallBack callBack) {
////        getInstance().doGet(url.getAuthority(), null, reqData, callBack);
////    }
//
////    public void execute(URL url, Map<String, String> reqData, ICallBack callBack) {
////        getInstance().doGet(url.getAuthority(), null, reqData, callBack);
////    }
//
////    public void execute(String url, Map<String, String> headers, String reqData, ICallBack callBack) {
////        getInstance().doGet(url, headers, reqData, callBack);
////    }
//
////    public void execute(String url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
////        getInstance().doGet(url, headers, reqData, callBack);
////    }
//
////    public void execute(URL url, Map<String, String> headers, String reqData, ICallBack callBack) {
////        getInstance().doGet(url.getAuthority(), headers, reqData, callBack);
////    }
//
////    public void execute(URL url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
////        getInstance().doGet(url.getAuthority(), headers, reqData, callBack);
////    }
//
//    @Override
//    public void doGet(String url, Map<String, String> headers, Map<String, String> reqData, ICallBack callBack) {
//        doGet(ParamsHelper.buildParams(url, reqData), headers, callBack);
//    }
//
//    @Override
//    public void doGet(final String url, final Map<String, String> headers, final String reqData, final ICallBack callBack) {
//        doGet(ParamsHelper.buildParams(url + reqData, null), headers, callBack);
//    }
//
//    @SuppressLint("StaticFieldLeak")
//    @Override
//    public void doGet(final HttpUrl.Builder httpBuilder, final Map<String, String> headers, final ICallBack callBack) {
//        ThreadExecutorHelper.getApiExecutor().execute(() -> {
//            ClientsManager.asyncGet(null, httpBuilder, headers, callBack);
//        });
//
////        ClientsManager.getExecutor().execute(() -> {
//////            callBack.response(ClientsManager.asyncGet(null, httpBuilder, headers));
////            ClientsManager.asyncGet(null, httpBuilder, headers, callBack);
////        });
////        ClientsManager.getExecutor().shutdown();
////        new AsyncTask<Void, Void, Response>() {
////            @Override
////            protected Response doInBackground(Void... voids) {
////                return ClientsManager.asyncGet(null, httpBuilder, headers);
////            }
////
////            @Override
////            protected void onPostExecute(Response resp) {
////                super.onPostExecute(resp);
////                callBack.response(resp);
////
////            }
////        }.execute();
//    }
//
//}
