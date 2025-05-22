package tw.com.chainsea.ce.sdk.service;

import android.content.Context;


import com.google.common.collect.Iterables;

import java.util.Set;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;

/**
 * current by evan on 2020-11-12
 *
 * @author Evan Wang
 * @date 2020-11-12
 */
public class AddressBookService {


    public static void addContact(Context context, String accountId, String alias, ServiceCallBack<String, RefreshSource> callBack) {
        ApiManager.doAddressbookAdd(context, accountId, alias, new ApiListener<Set<String>>() {
            @Override
            public void onSuccess(Set<String> roomIds) {
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.RECOMMEND_REFRESH_FILTER,accountId));
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        callBack.complete(Iterables.getFirst(roomIds, ""), RefreshSource.REMOTE);
                    });
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
