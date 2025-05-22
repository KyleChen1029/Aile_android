//package tw.com.chainsea.ce.sdk.service;
//
//import android.content.Context;
//import androidx.annotation.Nullable;
//
//import java.util.List;
//
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
//import tw.com.chainsea.ce.sdk.bean.Recommend;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
//import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
//import tw.com.chainsea.android.common.log.CELog;
//
///**
// * current by evan on 2020-03-24
// *
// * @author Evan Wang
// * @date 2020-03-24
// */
//public class RecommendService {
//    static long useTime = System.currentTimeMillis();
//
//    public static void getRecommends(Context context, RefreshSource source, @Nullable ServiceCallBack<List<Recommend>, RefreshSource> callBack) {
//        useTime = System.currentTimeMillis();
//        if (RefreshSource.LOCAL.equals(source)) {
//            List<Recommend> localEntities = DBManager.getInstance().queryAllRecommend();
//            if (localEntities != null) {
//                callBack.complete(localEntities, source);
//            }
//            CELog.w(String.format("ContactPerson2Fragment:: getRecommends use time::: %s /s", ((System.currentTimeMillis() - useTime) / 1000.0d)));
//        } else {
//            useTime = System.currentTimeMillis();
//            ApiManager.doAddressbookRecommend(context, new ApiListener<List<Recommend>>() {
//                @Override
//                public void onSuccess(List<Recommend> recommends) {
//                    DBManager.getInstance().cleanRecommends();
//                    for (Recommend recommend : recommends) {
//                        DBManager.getInstance().insertRecommend(recommend);
//                    }
//                    List<Recommend> localEntities = DBManager.getInstance().queryAllRecommend();
//                    if (localEntities != null) {
//                        callBack.complete(localEntities, RefreshSource.REMOTE);
//                    }
//                    CELog.d(String.format("ContactPerson2Fragment:: getRecommends use time::: %s /s", ((System.currentTimeMillis() - useTime) / 1000.0d)));
//                }
//
//                @Override
//                public void onFailed(String errorMessage) {
//
//                }
//            });
//        }
//    }
//}
