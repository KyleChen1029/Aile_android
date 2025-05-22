package tw.com.chainsea.ce.sdk.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.MessageReadingStateRequest;
import tw.com.chainsea.ce.sdk.reference.MessageReference;

/**
 * current by evan on 12/8/20
 *
 * @author Evan Wang
 * @date 12/8/20
 */
public class SyncReadBatchUpdateController {
    private static Multimap<String, String> batchData = ArrayListMultimap.create();
    private static final int MESSAGE_TAG = 0;

    private static WeakReference<Context> context;
    static long delayMillis = 5000L;
    static int limitMax = 10;


    private static final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String roomId = String.valueOf(msg.obj);
            CELog.i(String.format(" new handler :::%s, size :::%s", new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(System.currentTimeMillis()), batchData.get(roomId).size()));
            execute(roomId);
            handler.removeMessages(MESSAGE_TAG);
        }
    };


    public static void handler(Context ctx, String roomId, String messageId) {
        if (context == null) {
            SyncReadBatchUpdateController.context = new WeakReference<>(ctx);
        }
        if (batchData.isEmpty()) {
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REFRESH_FILTER));
        }

        batchData.put(roomId, messageId);
        Message message = new Message();
        message.what = MESSAGE_TAG;
        message.obj = roomId;
        if (batchData.get(roomId).size() >= limitMax) { // If more than the quantity is processed
            handler.sendMessage(message);
        } else { // Delay processing
            handler.sendMessageDelayed(message, delayMillis);
        }
    }


    public static void execute(String roomId) {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            try {
                Set<String> messageIds = Sets.newHashSet(batchData.removeAll(roomId));
                List<MessageEntity> entities = MessageReference.findByIdsAndRoomId(null, messageIds.toArray(new String[0]), roomId);

                messageIds.clear();
                for (MessageEntity entity : entities) {
                    if (entity.getSendNum() > 0 && entity.getSendNum() != entity.getReadedNum()) {
                        messageIds.add(entity.getId());
                    }
                }

                ApiManager.doMessageReadingState(context.get(), roomId, Lists.newArrayList(messageIds), new ApiListener<List<MessageReadingStateRequest.Resp.Item>>() {
                    @Override
                    public void onSuccess(List<MessageReadingStateRequest.Resp.Item> result) {
                        Iterator<MessageEntity> iterator = entities.iterator();
                        Iterator<MessageReadingStateRequest.Resp.Item> rIterator = result.iterator();

                        while (iterator.hasNext()) {
                            MessageEntity entity = iterator.next();
                            while (rIterator.hasNext()) {
                                MessageReadingStateRequest.Resp.Item item = rIterator.next();
                                if (item.getRoomId().equals(entity.getRoomId()) && item.getId().equals(entity.getId())) {
                                    entity.setSendNum(item.getSendNum());
                                    entity.setReadedNum(item.getReadedNum());
                                    entity.setReceivedNum(item.getReceivedNum());
                                } else {
                                    messageIds.remove(item.getId());
                                }
                            }
                        }

                        boolean status = MessageReference.saveByRoomId(roomId, entities);
                        String currentRoomId = UserPref.getInstance(context.get()).getCurrentRoomId();
                        if (roomId.equals(currentRoomId)) {
                            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_APPEND_OFFLINE_MESSAGE_IDS, JsonHelper.getInstance().toJson(messageIds)));
                        }
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                    }
                });
            } catch (Exception e) {
                CELog.e(e.getMessage());
            }
        });

    }

}
