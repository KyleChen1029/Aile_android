package tw.com.chainsea.ce.sdk.controller;

/**
 * current by evan on 12/8/20
 *
 * @author Evan Wang
 * @date 12/8/20
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;

/**
 * current by evan on 2020-03-16
 *
 * <p>
 * 避免大量Jocket Message.New 導致UI繪畫IO壅塞
 * </p>
 *
 * @versiom 1.10.0
 */
public class ChatRoomBatchUpdateController {
    private static final String TAG = ChatRoomBatchUpdateController.class.getSimpleName();

    private static Multimap<String, String> batchData = ArrayListMultimap.create();

    //    private static final Message MESSAGE = new Message();
    private static final int MESSAGE_TAG = 0;
    //    private static final int MAX_MESSAGE = 0;
//    private static final int INTERVAL_MESSAGE = 1;
    private static WeakReference<Context> context;

    static long delayMillis = 2000L;
    static int limitMax = 1;

    private static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        @SuppressLint("SimpleDateFormat")
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String roomId = msg.getData().getString("roomId");
            Object obj = msg.obj;
            CELog.i(String.format(" new handler :::%s, size :::%s", new SimpleDateFormat("HH:mm:ss.SSS").format(System.currentTimeMillis()), batchData.get(roomId).size()));
            send(roomId);
            batchData.removeAll(roomId);
            handler.removeMessages(MESSAGE_TAG);
//            switch (msg.what) {
//                case MAX_MESSAGE:
//                    send(roomId);
//                    batchData.removeAll(roomId);
//                    break;
//                case INTERVAL_MESSAGE:
//                    send(roomId);
//                    batchData.removeAll(roomId);
//                    break;
//                default:
//                    break;
//            }
        }
    };

    @SuppressLint("SimpleDateFormat")
    public static void handler(Context c, String roomId, String messageId) {
        context = new WeakReference<>(c);
        CELog.d(String.format(" new handler :::%s, size :::%s", new SimpleDateFormat("HH:mm:ss.SSS").format(System.currentTimeMillis()), batchData.get(roomId).size()));
        batchData.put(roomId, messageId);
        Bundle bundle = new Bundle();
        bundle.putString("roomId", roomId);
        Message message = new Message();
        message.what = MESSAGE_TAG;
        message.setData(bundle);
        message.obj = roomId;
        if (batchData.get(roomId).size() >= limitMax) { // 如果大於數量處理
//            MESSAGE.what = MAX_MESSAGE;
            handler.sendMessage(message);
        } else { // 延遲處處理
//            handler.removeMessages(INTERVAL_MESSAGE);
//            MESSAGE.what = INTERVAL_MESSAGE;
            handler.sendMessageDelayed(message, delayMillis);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static void send(String roomId) {
        CELog.w(String.format(" new handler :::%s, size :::%s", new SimpleDateFormat("HH:mm:ss.SSS").format(System.currentTimeMillis()), batchData.get(roomId).size()));
        String userId = TokenPref.getInstance(context.get()).getUserId();
        ChatRoomEntity entity = ChatRoomReference.getInstance().findById2(userId, roomId, true, true, true, true, true);
//        boolean hasRoom = ChatRoomReference.getInstance().hasLocalData(null , roomId);
//        if (hasRoom) {
//
//        }
        if (entity != null) {
            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, entity.getId()));
        }
    }
}
