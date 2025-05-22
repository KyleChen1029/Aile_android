package tw.com.chainsea.ce.sdk.cache;

import androidx.annotation.NonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;

/**
 * current by evan on 12/15/20
 *
 * @author Evan Wang
 * @date 12/15/20
 */
public class ChatMemberCacheService {
    public static int retryCount = 0;
    static LoadingCache<String, List<UserProfileEntity>> cache = CacheBuilder.newBuilder()
        .build(new CacheLoader<String, List<UserProfileEntity>>() {
            @Override
            @NonNull
            public List<UserProfileEntity> load(@NonNull String roomId) throws Exception {
                return UserProfileReference.findUserProfilesByRoomId(null, roomId);
            }
        });


    public static List<UserProfileEntity> getChatMember(String roomId) {
        try {
            if (roomId != null) {
                List<UserProfileEntity> members = cache.get(roomId);
                members.remove(UserProfileEntity.newAddCell("", ""));
                return Lists.newArrayList(members);
            }
        } catch (Exception e) {
            CELog.e(e.getMessage());
            CELog.e("Error occurs, at Room Id:::" + roomId);
            retryCount++;
            if (retryCount > 30) {// retryCount 是決定多少次出錯之後清除 DB 可以視情況調整
                retryCount = 0;
                CELog.e("Start to clean DB and reload data. retryCount::" + retryCount);
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.CLEAN_DB_AND_RELOAD, null));
            }
            return Lists.newArrayList();
        }
        return Lists.newArrayList();
    }

    public static void all() {
        cache.asMap().entrySet();
    }

    public static void refresh(String roomId) {
        cache.refresh(roomId);
    }

    public static void clearCache(String roomId) {
        cache.invalidate(roomId);
    }

    public static void clearAllCache() {
        cache.cleanUp();
    }

}
