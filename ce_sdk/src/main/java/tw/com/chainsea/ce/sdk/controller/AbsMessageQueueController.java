package tw.com.chainsea.ce.sdk.controller;

import android.content.Context;

import com.google.common.base.Strings;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.service.RepairMessageService;

/**
 * current by evan on 11/17/20
 *
 * @author Evan Wang
 * @date 11/17/20
 */
public abstract class AbsMessageQueueController {

    //    protected static Context mContext;
    protected static final long DELAY_INTERVAL = 1000;
    protected static final long OFFLINE_DELAY_INTERVAL = 2000L;
    protected static final long TIME_OUT_INTERVAL = 10000L;

    /**
     * 取得 所有 非自己的 MessageEntities 之 SenderId
     *
     * @param selfId
     * @param entitiesTree
     * @return
     */
    protected Set<String> getSendsIds(String selfId, TreeSet<MessageEntity> entitiesTree) {
        if (entitiesTree == null || entitiesTree.isEmpty()) {
            return Sets.newHashSet();
        }

        Set<String> ids = Sets.newHashSet();

        Iterator<MessageEntity> iterator = entitiesTree.iterator();
        while (iterator.hasNext()) {
            String sendId = iterator.next().getSenderId();
            if (!sendId.equals(selfId)) {
                ids.add(sendId);
            }
        }
        return ids;
    }

    protected Set<String> getMessageIds(Collection<MessageEntity> collection) {
        if (collection == null || collection.isEmpty()) {
            return Sets.newHashSet();
        }
        Set<String> ids = Sets.newHashSet();
        Iterator<MessageEntity> iterator = collection.iterator();
        while (iterator.hasNext()) {
            ids.add(iterator.next().getId());
        }
        return ids;
    }

    protected int getUnreadNumber(long localLastSendTime, TreeSet<MessageEntity> entitiesTree) {
        MessageEntity last = entitiesTree.last();
        if (last.getSendTime() > localLastSendTime) {
            return last.getUnReadNum();
        } else {
            return -1;
        }
    }

    protected String getRingName(TreeSet<MessageEntity> entitiesTree) {
        String ringName = "";
        Iterator<MessageEntity> iterator = entitiesTree.iterator();
        while (iterator.hasNext()) {
            if (Strings.isNullOrEmpty(ringName)) {
                ringName = iterator.next().getRingName();
            }
        }
        return ringName;
    }


    protected synchronized void handlingMessageByRemote(Context context, String roomId, Set<String> messageIds) {
        Set<String> notExistIds = MessageReference.findDoesNotExistIdsByIds(null, roomId, Sets.newHashSet(messageIds));
        if (notExistIds != null && !notExistIds.isEmpty()) {
            RepairMessageService.executeNotChecking(context, roomId, Queues.newLinkedBlockingDeque(notExistIds));
        }
    }

}
