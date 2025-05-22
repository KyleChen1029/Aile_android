package tw.com.chainsea.ce.sdk.treatment;

import android.content.Context;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.chainsea.android.common.version.VersionHelper;
import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.reference.BusinessReference;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-11-09
 *
 * @author Evan Wang
 * date 2020-11-09
 */
public class VersionTreatments {
    enum Version {
        V_1_14_0("1.14.0");

        String name;

        Version(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static void treatments_1_14_0(Context context, String roomSource, String source, Set<ChatRoomEntity> roomEntities, boolean status) {
        CELog.w("exe v1.14.0 binding business executor info to chat room " + roomSource + " " + source);
        boolean checkingPoint = VersionHelper.isDayuOldVersion(context, Version.V_1_14_0.name);
        boolean hasTreatments = UserPref.getInstance(context).hasTreatments_1_14_0_BusinessExecutor(roomSource + "_" + source + "room");
        if (checkingPoint && !hasTreatments) {
            CELog.w("start exe v1.14.0 binding business executor info to chat room " + roomSource + " " + source);
            Set<String> ids = Sets.newHashSet();
            for (ChatRoomEntity r : roomEntities) {
                ids.add(r.getBusinessId());
            }
            Map<String, String> data = BusinessReference.findExecutorIdMappingByIds(null, ids);

            for (ChatRoomEntity r : roomEntities) {
                String executorId = data.get(r.getBusinessId());
                if (!Strings.isNullOrEmpty(executorId)) {
                    r.setBusinessExecutorId(executorId);
                }
            }
            ChatRoomReference.getInstance().save(Lists.newArrayList(roomEntities));
            UserPref.getInstance(context).setTreatments_1_14_0_BusinessExecutor(roomSource + "_" + source + "room", status);
        }
        CELog.w("end exe v1.14.0 binding business executor info to chat room " + roomSource + " " + source);

    }


    public static void treatments_1_14_0(Context context, String source, List<BusinessEntity> entities) {
        CELog.w("exe v1.14.0 update business executor info to chat room " + source);
        boolean checkingPoint = VersionHelper.isDayuOldVersion(context, Version.V_1_14_0.name);
        boolean hasTreatments = UserPref.getInstance(context).hasTreatments_1_14_0_BusinessExecutor(source);
        if (checkingPoint && !hasTreatments) {
            CELog.w("start exe v1.14.0 update business executor info to chat room " + source);
            for (BusinessEntity b : entities) {
                if (!Strings.isNullOrEmpty(b.getExecutorId())) {
                    ChatRoomReference.getInstance().updateBusinessExecutorIdByBusinessId(b.getId(), b.getExecutorId());
                }
            }
            UserPref.getInstance(context).setTreatments_1_14_0_BusinessExecutor(source, true);
        }
        CELog.w("end exe v1.14.0 update business executor info to chat room " + source);
    }
}
