package tw.com.chainsea.ce.sdk.service;

import android.content.Context;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-08-07
 *
 * @author Evan Wang
 * @date 2020-08-07
 */
public class RepairMessageService {
    static Map<String, Queue<String>> roomQueue = Maps.newConcurrentMap();
    static ExecutorService service = Executors.newFixedThreadPool(1);


    public static synchronized void handlePrevious(Context context, String roomId, List<MessageEntity> entities, boolean isCheckPrevious) {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            Set<String> noPreviousMessageIdSet = Sets.newHashSet();
            Set<String> mIds = Sets.newHashSet();
            Set<String> pmIds = Sets.newHashSet();
            Iterator<MessageEntity> iterator = entities.iterator();
            while (iterator.hasNext()) {
                MessageEntity m = iterator.next();
                String id = m.getId();
                String pId = StringHelper.getString(m.getPreviousMessageId(), "").toString();
                if (pmIds.contains(pId) || Strings.isNullOrEmpty(pId)) {
                    noPreviousMessageIdSet.add(id);
                }
                mIds.add(id);
                pmIds.add(pId);
                if (Strings.isNullOrEmpty(pId)) {
                    noPreviousMessageIdSet.add(id);
                }
            }

            pmIds.removeAll(mIds);
            pmIds.remove("");
            pmIds.addAll(noPreviousMessageIdSet);

            if (!pmIds.isEmpty()) {
                ThreadExecutorHelper.getHandlerExecutor().execute(() -> setQueue(context, roomId, pmIds, isCheckPrevious), 2000L);
            }
        });

    }

    /**
     * Process the supplementary training MessageIds as needed and put them in the queue
     *
     * @param context
     * @param roomId
     * @param messageIds
     */
    public static void setQueue(Context context, String roomId, Set<String> messageIds, boolean isCheckPrevious) {
        Queue<String> messageQueue = Queues.newLinkedBlockingDeque(messageIds);
        if (!isCheckPrevious) {
            executeNotChecking(context, roomId, messageQueue);
            return;
        }
        if (roomQueue.get(roomId) == null) {
            roomQueue.put(roomId, messageQueue);
        } else {
            Queue<String> queues = roomQueue.get(roomId);
            messageIds.addAll(queues);
            roomQueue.remove(roomId);
            roomQueue.put(roomId, Queues.newLinkedBlockingDeque(messageIds));
        }

        if (roomQueue.isEmpty()) {
            return;
        }

        String topMessageId = MessageReference.findTopMessageIdByRoomId(null, roomId);
        if (Strings.isNullOrEmpty(topMessageId)) {
            removeQueue(context, roomId, messageQueue);
        } else {
            execute(context, roomId, messageQueue);
        }
    }


    public static synchronized void executeNotChecking(Context context, String roomId, Queue<String> messageQueue) {
        ApiManager.doMessageItem(context, messageQueue.remove(), new ApiListener<MessageEntity>() {
            @Override
            public void onSuccess(MessageEntity entity) {
                if (roomId.equals(entity.getRoomId())) {
                    MessageReference.save(roomId, entity);
                    String currentRoomId = UserPref.getInstance(context).getCurrentRoomId();
                    if (currentRoomId.equals(entity.getRoomId())) {
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_MESSAGE, JsonHelper.getInstance().toJson(entity)));
                    }
                }
                if (!messageQueue.isEmpty()) {
                    executeNotChecking(context, roomId, messageQueue);
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e(errorMessage);
            }
        });
    }


    /**
     * Delete processing queue data
     *
     * @param context
     * @param roomId
     */
    private static void removeQueue(Context context, String roomId, Queue<String> messageQueue) {
        try {
            messageQueue.clear();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }

        try {
            roomQueue.remove(roomId);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    public static void stop(Context context, String roomId) {
        service.execute(() -> {
            try {
                if (roomQueue.get(roomId) != null) {
                    roomQueue.get(roomId).clear();
                    roomQueue.remove(roomId);
                }
            } catch (Exception e) {
                CELog.e(e.getMessage());
            }
        });
    }

    /**
     * execute
     *
     * @param context
     * @param roomId
     */
    private static synchronized void execute(Context context, String roomId, Queue<String> messageQueue) {
        if (messageQueue.isEmpty()) {
            removeQueue(context, roomId, messageQueue);
            return;
        }

        String qId = messageQueue.remove();
        if (Strings.isNullOrEmpty(qId)) {
            execute(context, roomId, messageQueue);
            return;
        }


        String topMessageId = MessageReference.findTopMessageIdByRoomId(null, roomId);
        if (topMessageId.equals(qId)) {
            execute(context, roomId, messageQueue);
            return;
        }

        ApiManager.doMessageItem(context, qId, new ApiListener<MessageEntity>() {
            @Override
            public void onSuccess(MessageEntity entity) {
                String rId = entity.getRoomId();
                String messageId = entity.getId();
                String previousMessageId = entity.getPreviousMessageId();

                if (!messageId.equals(previousMessageId) && rId.equals(roomId)) { // if not this chat room message
                    if (!previousMessageId.equals(topMessageId)) { // if previous is top message
                        boolean hasPreviousData = MessageReference.hasLocalData(null, roomId, previousMessageId);
                        if (!hasPreviousData) { // if local not exist previous entity， else apply Queue
                            messageQueue.add(previousMessageId);
                        }
                    } else {
                        CELog.e("previousMessage id is top id");
                    }
                }

                if (!Strings.isNullOrEmpty(rId) && rId.equals(roomId)) {
                    MessageReference.save(roomId, entity);
                }
                execute(context, roomId, messageQueue);
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e(errorMessage);
                execute(context, roomId, messageQueue);
            }
        });
    }
}
