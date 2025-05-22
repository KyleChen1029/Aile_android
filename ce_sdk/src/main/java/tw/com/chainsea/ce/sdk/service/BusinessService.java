package tw.com.chainsea.ce.sdk.service;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.common.base.Strings;

import java.util.List;

import tw.com.chainsea.android.common.datetime.DateTimeHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.reference.BusinessReference;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.ce.sdk.treatment.VersionTreatments;

/**
 * current by evan on 2020-04-30
 *
 * @author Evan Wang
 * date 2020-04-30
 */
public class BusinessService {


    public static void getBusinessItem(Context context, String businessId, @Nullable ServiceCallBack<BusinessEntity, RefreshSource> callBack) {
        if (callBack != null) {
            BusinessEntity entity = BusinessReference.find(null, businessId);

            if (entity != null) {
                callBack.complete(entity, RefreshSource.LOCAL);
            } else {
                callBack.error("id Null");
            }
        }
    }

    /**
     * Everything I own
     */
    public static void getBusinessListMe(Context context, RefreshSource source, @Nullable ServiceCallBack<List<BusinessEntity>, RefreshSource> callBack) {
        if (!UserPref.getInstance(context).hasBusinessSystem()) {
            if (callBack != null) {
                callBack.error("no business system");
            }
            return;
        }

        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                List<BusinessEntity> localEntities = BusinessReference.findAll(null);

                // 洗乾淨
                for (BusinessEntity b : localEntities) {
                    if (b.getEndTimestamp() <= 0 && !Strings.isNullOrEmpty(b.getEndTime())) {
                        long endTimestamp = DateTimeHelper.tryParseToMillis(b.getEndTime(), "yyyy-MM-dd", "yyyy/MM/dd");
                        if (endTimestamp > 0) {
                            CELog.i("update business entity end timestamp by --> " + b.getId() + ", endTime -->" + b.getEndTime() + ", endTimestamp -->" + b.getEndTimestamp() + " ~~>" + endTimestamp);
                            b.setEndTimestamp(endTimestamp);
                            // update
                            BusinessReference.updateEndTimestampById(null, b.getId(), b.getEndTimestamp());
                        }
                    }
                }

                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(localEntities, RefreshSource.LOCAL));
                }
                VersionTreatments.treatments_1_14_0(context, RefreshSource.LOCAL.name(), localEntities);
            });
        }
    }
}
