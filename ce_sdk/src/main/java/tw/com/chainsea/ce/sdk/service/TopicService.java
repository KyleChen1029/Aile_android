package tw.com.chainsea.ce.sdk.service;

import android.content.Context;
import androidx.annotation.Nullable;

import java.util.List;

import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.reference.TopicReference;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-08-20
 *
 * @author Evan Wang
 * @date 2020-08-20
 */
public class TopicService {

    public static void getTopicEntities(Context context, @Nullable ServiceCallBack<List<TopicEntity>, RefreshSource> callBack) {
        ApiManager.doTopicList(context, new ApiListener<List<TopicEntity>>() {
            @Override
            public void onSuccess(List<TopicEntity> topicEntities) {
                boolean status = TopicReference.save(null, topicEntities);
                CELog.d("save TopicEntity status::" + status);
                if (callBack != null) {
                    callBack.complete(topicEntities, RefreshSource.REMOTE);
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e(errorMessage);
                if (callBack != null) {
                    callBack.error(errorMessage);
                }
            }
        });
    }
}
