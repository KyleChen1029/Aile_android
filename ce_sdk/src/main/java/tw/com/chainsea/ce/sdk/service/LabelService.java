package tw.com.chainsea.ce.sdk.service;

import android.content.Context;

import com.google.common.collect.Sets;

import java.util.Map;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.reference.FriendsLabelRelReference;
import tw.com.chainsea.ce.sdk.reference.LabelReference;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;

/**
 * current by evan on 2020-03-24
 *
 * @author Evan Wang
 * @date 2020-03-24
 */
public class LabelService {

    /**
     * Add Favorites
     */
    public static void addFavourite(Context context, String userId, ServiceCallBack<Label, Enum> callBack) {
        ApiManager.doAddFavourite(context, userId, new ApiListener<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> data) {
                String labelId = data.get("labelId");
                String userId = data.get("userId");
                Label label = LabelReference.findById(null, labelId);
                if (label != null) {
                    FriendsLabelRelReference.saveByLabel(null, label.getId(), Sets.newHashSet(userId));
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        callBack.complete(label, null);
                    });
                } else {
                    ApiManager.doLabelItem(context, labelId, new ApiListener<Label>() {
                        @Override
                        public void onSuccess(Label label) {
                            LabelReference.save(null, label, Sets.newHashSet(userId));
                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                                callBack.complete(label, null);
                            });
                        }

                        @Override
                        public void onFailed(String errorMessage) {
                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                                callBack.error(errorMessage);
                            });
                        }
                    });
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    callBack.error(errorMessage);
                });
            }
        });
    }


    /**
     * delete Favorites
     */
    public static void removeFavourite(Context context, String userId, ServiceCallBack<String, Enum> callBack) {
        ApiManager.doDeleteFavourite(context, userId, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                String labelId = UserPref.getInstance(context).getLoveLabelId();
                boolean status = FriendsLabelRelReference.deleteByLabelIdAndUserId(null, labelId, userId);
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete("" + status, null));
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        callBack.error(errorMessage);
                    });
                }
            }
        });

    }
}
