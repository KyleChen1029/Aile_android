//package tw.com.chainsea.ce.sdk.http.ce.request;
//
//import android.content.Context;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import tw.com.chainsea.android.common.log.CELog;
//import tw.com.chainsea.ce.sdk.http.ce.base.RequestBase;
//import tw.com.chainsea.ce.sdk.lib.ErrCode;
//
///**
// * ContactDeleteRequest to delete a contact
// * Created by Fleming on 2016/6/13.
// */
//public class RecommendDeleteRequest extends RequestBase {
//    private final static String PATH = "/openapi/addressbook/recommend/delete";
//    Listener listener;
//
//    public RecommendDeleteRequest(Context ctx, Listener listener) {
//        super(ctx, PATH);
//        this.listener = listener;
//    }
//
//    @Override
//    protected void success(JSONObject jsonObject, String s) throws JSONException {
//        listener.onRecommendDeleteSuccess();
//    }
//
//    @Override
//    protected void failed(ErrCode code, String errorMessage) {
//        CELog.e("contact delete failed: " + code.getValue());
//        listener.onRecommendDeleteFailed(errorMessage);
//    }
//
//    public interface Listener {
//        void onRecommendDeleteSuccess();
//
//        void onRecommendDeleteFailed(String errorMessage);
//    }
//}
