//package tw.com.chainsea.ce.sdk.http.ce.request;
//
//import android.content.Context;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
//import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.android.common.log.CELog;
//import tw.com.chainsea.ce.sdk.lib.ErrCode;
//import tw.com.chainsea.ce.sdk.lib.ParseUtils;
//import tw.com.chainsea.ce.sdk.bean.Recommend;
//
///**
// * ContactDeleteRequest to delete a contact
// * Created by Fleming on 2016/6/13.
// */
//public class AddressbookRecommendRequest extends NewRequestBase {
//    private ApiListener<List<Recommend>> listener;
//
//    public AddressbookRecommendRequest(Context ctx, ApiListener<List<Recommend>> listener) {
//        super(ctx, ApiPath.addressBookRecommend);
//        this.listener = listener;
//    }
//
//    @Override
//    protected void success(JSONObject jsonObject, String s) throws JSONException {
//        List<Recommend> recommends = new ArrayList<>();
//        JSONArray users = jsonObject.getJSONArray("users");
//        for (int i = 0; i < users.length(); i++) {
//            Recommend recommend = ParseUtils.parseRecommend(users.getJSONObject(i));
//            recommends.add(recommend);
//        }
//        if (this.listener != null) {
//            this.listener.onSuccess(recommends);
//        }
//    }
//
//    @Override
//    protected void failed(ErrCode code, String errorMessage) {
//        CELog.e(errorMessage);
//        if (this.listener != null) {
//            this.listener.onFailed(errorMessage);
//        }
//    }
//
//}
